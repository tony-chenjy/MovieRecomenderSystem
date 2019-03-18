package org.tony.chenjy.movie_recommender_system.dev;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author tony.chenjy
 * @date 2019-03-09
 */
public class AverageRating {

    public static class AverageMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input_small : userId,movieId,rating
            // output : userId rating

            String[] userMovieRating = value.toString().trim().split(",");
            context.write(new Text(userMovieRating[0]), new DoubleWritable(Double.parseDouble(userMovieRating[2])));
        }
    }

    public static class AverageReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            // input_small : userId <rating, rating, ...>
            // calculate each user average rating: average
            // output : userId averageRating

            double sum = 0;
            double count = 0;
            for (DoubleWritable value : values) {
                double rating = value.get();
                sum += rating;
                count++;
            }
            double average = sum / count;
            context.write(key, new DoubleWritable(average));

        }
    }

    public static void main(String[] args) throws Exception {
//        args = new String[]{"src/main/resources/recommender_system/input_small/userRating.txt",
//                            "src/main/resources/recommender_system/output/userAverageRating"};

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);

        job.setJarByClass(AverageRating.class);

        job.setMapperClass(AverageMapper.class);
        job.setReducerClass(AverageReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }

}
