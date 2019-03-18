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
import java.util.HashMap;
import java.util.Map;

public class Normalize {

    public static class NormalizeMapper extends Mapper<LongWritable, Text, Text, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input_small : movieA:movieB \t relation
            // outputKey : movieA
            // outputValue : movieB=relation

            String[] movieRelation = value.toString().trim().split("\t");
            String[] movies = movieRelation[0].trim().split(":");
            String relation = movieRelation[1];
            context.write(new Text(movies[0]), new Text(movies[1] + "=" + relation));
        }
    }

    public static class NormalizeReducer extends Reducer<Text, Text, Text, Text> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // input_small : movieA <movieB=relation, movieC=relation, ...>
            // normalize each unit of co-occurrence matrix & transpose
            // outputKey : movieB
            // outputValue : movieA=relation/sum

            double sum = 0;
            Map<String, Double> map = new HashMap<String, Double>();
            for (Text value : values) {
                String[] movieRelation = value.toString().trim().split("=");
                double relation = Double.parseDouble(movieRelation[1]);
                map.put(movieRelation[0], relation);
                sum += relation;
            }

            for (Map.Entry<String, Double> entry : map.entrySet()) {
                String outputKey = entry.getKey();
                double normalizedValue = entry.getValue() / sum;
                String outputValue = key.toString().trim() + "=" + normalizedValue;
                context.write(new Text(outputKey), new Text(outputValue));
            }
        }
    }

    public static void main(String[] args) throws Exception {
//        args = new String[]{"src/main/resources/recommender_system/output/coOccurrenceMatrix",
//                            "src/main/resources/recommender_system/output/normalizeMatrix"};

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);

        job.setJarByClass(Normalize.class);

        job.setMapperClass(NormalizeMapper.class);
        job.setReducerClass(NormalizeReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
