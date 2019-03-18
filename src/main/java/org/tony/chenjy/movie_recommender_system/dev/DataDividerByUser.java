package org.tony.chenjy.movie_recommender_system.dev;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class DataDividerByUser {
	public static class DataDividerMapper extends Mapper<LongWritable, Text, Text, Text> {

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// input_small : userId,movieId,rating
			// output : userId \t movieId=rating

			String[] userMovieRating = value.toString().trim().split(",");
			context.write(new Text(userMovieRating[0]), new Text(userMovieRating[1] + "=" + userMovieRating[2]));
		}
	}

	public static class DataDividerReducer extends Reducer<Text, Text, Text, Text> {
		// reduce method
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			// input_small : userId <movieId=rating, ...>
			//calculate each user rating list: <movieA=rating, movieB=rating>
			// output : userId movieId=rating,...

			StringBuilder movieRatingList = new StringBuilder();
			for (Text value : values) {
				movieRatingList.append(",").append(value.toString().trim());
			}
			context.write(key, new Text(movieRatingList.replace(0, 1, "").toString()));
		}
	}

	public static void main(String[] args) throws Exception {
//		args = new String[]{"src/main/resources/recommender_system/input_small/userRating.txt",
//							"src/main/resources/recommender_system/output/userRatingList"};

		Configuration conf = new Configuration();

		Job job = Job.getInstance(conf);

		job.setJarByClass(DataDividerByUser.class);

		job.setMapperClass(DataDividerMapper.class);
		job.setReducerClass(DataDividerReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}

}
