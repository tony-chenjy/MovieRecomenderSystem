package org.tony.chenjy.movie_recommender_system.dev;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author tony.chenjy
 * @date 2019-03-09
 */
public class Compare {

    public static class OriginMapper extends Mapper<LongWritable, Text, Text, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input_small : user,movie,rating
            // output : user:movie rating

            String[] userMovieRating = value.toString().trim().split(",");
            context.write(new Text(userMovieRating[0] + ":" + userMovieRating[1]), new Text(userMovieRating[2]));
        }
    }

    public static class PredictionMapper extends Mapper<LongWritable, Text, Text, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input_small : user:movie prediction
            // output : user:movie prediction

            String[] userMoviePrediction = value.toString().trim().split("\t");
            context.write(new Text(userMoviePrediction[0]), new Text(userMoviePrediction[1] + "(prediction)"));
        }
    }

    public static class CompareReducer extends Reducer<Text, Text, Text, Text> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // input_small : user:movie <rating, rating(prediction)>
            // outputKey : user:movie
            // outputValue : rating vs rating(prediction)

            String originRating = null;
            String predictRating = null;
            for (Text value : values) {
                if (value.toString().contains("(prediction)")) {
                    predictRating = value.toString().trim();
                } else {
                    originRating = value.toString().trim();
                }
            }

            if (originRating == null) {
                originRating = "---";
                context.write(key, new Text(originRating + "\tvs\t" + predictRating));
            } else {
                context.write(key, new Text(originRating + "\tvs\t" + predictRating));
            }
        }
    }

    public static void main(String[] args) throws Exception {
//        args = new String[]{"src/main/resources/recommender_system/input_small/userRating.txt",
//                            "src/main/resources/recommender_system/output/sum",
//                            "src/main/resources/recommender_system/output/compare"};

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);

        job.setJarByClass(Compare.class);

        job.setMapperClass(OriginMapper.class);
        job.setMapperClass(PredictionMapper.class);

        job.setReducerClass(CompareReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, OriginMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, PredictionMapper.class);

        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);
    }

}
