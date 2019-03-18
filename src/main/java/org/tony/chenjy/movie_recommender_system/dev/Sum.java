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
 * Created by Michelle on 11/12/16.
 */
public class Sum {

    public static class SumMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input_small : userId:movieId rating

            String[] userMovieRating = value.toString().trim().split("\t");
            double rating = Double.parseDouble(userMovieRating[1]);
            context.write(new Text(userMovieRating[0]), new DoubleWritable(rating));
        }
    }

    public static class SumReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {
            // input_small : userId:movieId rating
            // calculate the sum

            double sum = 0;
            for (DoubleWritable value : values) {
                sum += value.get();
            }
            context.write(key, new DoubleWritable((double)Math.round(sum * 10) / 10));
        }
    }

    public static void main(String[] args) throws Exception {
//        args = new String[]{"src/main/resources/recommender_system/output/multiplicationUnit",
//                            "src/main/resources/recommender_system/output/sum"};

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);

        job.setJarByClass(Sum.class);

        job.setMapperClass(SumMapper.class);
        job.setReducerClass(SumReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
