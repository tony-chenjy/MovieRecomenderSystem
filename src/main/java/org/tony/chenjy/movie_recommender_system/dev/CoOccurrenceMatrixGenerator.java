package org.tony.chenjy.movie_recommender_system.dev;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class CoOccurrenceMatrixGenerator {
	public static class MatrixGeneratorMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// input_small : userId \t movieId1=rating,movieId2=rating,...
			// outputKey : movieId1:movieId2
			// outputValue : 1

			String[] userRatingList = value.toString().trim().split("\t");
			String[] movieRatingList = userRatingList[1].trim().split(",");

			for (String movieRating : movieRatingList) {
				String movieA = movieRating.trim().split("=")[0];
				for (String movieRelated : movieRatingList) {
					String movieB = movieRelated.trim().split("=")[0];
					context.write(new Text(movieA + ":" + movieB), new IntWritable(1));
				}
			}
		}
	}

	public static class MatrixGeneratorReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		// reduce method
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			// input_small : movie1:movie2 <1, 1, 1>
			// calculate each two movies have been watched by how many people

			int sum = 0;
			for (IntWritable value : values) {
				sum += value.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}
	
	public static void main(String[] args) throws Exception{
//		args = new String[]{"src/main/resources/recommender_system/output/userRatingList",
//							"src/main/resources/recommender_system/output/coOccurrenceMatrix"};
		
		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(conf);

		job.setJarByClass(CoOccurrenceMatrixGenerator.class);

		job.setMapperClass(MatrixGeneratorMapper.class);
		job.setReducerClass(MatrixGeneratorReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.waitForCompletion(true);
		
	}
}
