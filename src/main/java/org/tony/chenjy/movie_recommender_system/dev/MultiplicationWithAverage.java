package org.tony.chenjy.movie_recommender_system.dev;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MultiplicationWithAverage {

	public static class CooccurrenceMapper extends Mapper<LongWritable, Text, Text, Text> {

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// input_small : movieB \t movieA=relation

			String[] movieRelation = value.toString().trim().split("\t");
			context.write(new Text(movieRelation[0]), new Text(movieRelation[1]));
		}
	}

	public static class RatingMapper extends Mapper<LongWritable, Text, Text, Text> {

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// input_small : userId,movieId,rating
			// output : movieId userId:rating

			String[] userMovieRating = value.toString().trim().split(",");
			context.write(new Text(userMovieRating[1]), new Text(userMovieRating[0] + ":" + userMovieRating[2]));
		}
	}

	public static class MultiplicationReducer extends Reducer<Text, Text, Text, DoubleWritable> {

		public static Map<String, String> userAverageRating = new HashMap<>();

		protected void setup(Context context) throws IOException, InterruptedException {
			Configuration configuration = context.getConfiguration();
			String userAverageRatingPath = configuration.get("userAverageRating");

			FileSystem fileSystem = FileSystem.get(configuration);
			FileStatus[] fileStatuses = fileSystem.listStatus(new Path(userAverageRatingPath));
			for (FileStatus fileStatus : fileStatuses) {
				InputStream inputStream = fileSystem.open(fileStatus.getPath());
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

				// userId \t averageRating
				String line = bufferedReader.readLine();
				while (line != null) {
					String[] userRating = line.trim().split("\t");
					userAverageRating.put(userRating[0],userRating[1]);
					line = bufferedReader.readLine();
				}
			}
		}

		// reduce method
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			// input_small : movieB <movieA=relation, movieC=relation... userA:rating, userB:rating...>
			// collect the data for each movie, then do the multiplication
			// outputKey : userA:movieA, userA:movieB, ..., userB:movieA, userB:movieB, ...
			// outputValue : movieB=rating

			Map<String, String> movieRelation = new HashMap<>();
			Map<String, String> userRating = new HashMap<>();

			for (Text value : values) {
				String str = value.toString().trim();
				if (str.contains("=")) {
					movieRelation.put(str.split("=")[0], str.split("=")[1]);
				} else {
					userRating.put(str.split(":")[0], str.split(":")[1]);
				}
			}

			for (Map.Entry<String, String> entry: userAverageRating.entrySet()) {
				if (!userRating.containsKey(entry.getKey())) {
					userRating.put(entry.getKey(), entry.getValue());
				}
			}

			// all user rating to movieB
			for (Map.Entry<String, String> urEntry : userRating.entrySet()) {
				String user = urEntry.getKey();
				String rating = urEntry.getValue();
				// all movie related to movieB
				for (Map.Entry<String, String> mrEntry : movieRelation.entrySet()) {
					String movie = mrEntry.getKey();
					String relation = mrEntry.getValue();
					double outputValue = Double.parseDouble(rating) * Double.parseDouble(relation);
					context.write(new Text(user + ":" + movie), new DoubleWritable(outputValue));
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		args = new String[]{"src/main/resources/recommender_system/output/normalizeMatrix",
//							"src/main/resources/recommender_system/input_small/userRating.txt",
//							"src/main/resources/recommender_system/output/multiplicationUnitWithAverage",
//							"src/main/resources/recommender_system/output/userAverageRating"};

		Configuration conf = new Configuration();
		conf.set("userAverageRating", args[3]);

		Job job = Job.getInstance(conf);

		job.setJarByClass(MultiplicationWithAverage.class);

		job.setMapperClass(CooccurrenceMapper.class);
		job.setMapperClass(RatingMapper.class);

		job.setReducerClass(MultiplicationReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);

		MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, CooccurrenceMapper.class);
		MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, RatingMapper.class);

		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		
		job.waitForCompletion(true);
	}

}
