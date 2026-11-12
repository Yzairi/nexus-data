package fr.nexus.hadoop.activity;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public final class ActivityPipeline {
    private ActivityPipeline() {}

    public static int run(Configuration configuration, String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("activity <telemetry-input> <players-input> <work-output> <dau-output>");
            return 2;
        }

        Job distinct = Job.getInstance(configuration, "nexus-activity-known-player-days");
        distinct.setJarByClass(ActivityPipeline.class);
        MultipleInputs.addInputPath(distinct, new Path(args[0]), TextInputFormat.class, TelemetryActivityMapper.class);
        MultipleInputs.addInputPath(distinct, new Path(args[1]), TextInputFormat.class, PlayerReferenceMapper.class);
        distinct.setReducerClass(KnownPlayerActivityReducer.class);
        distinct.setMapOutputKeyClass(Text.class);
        distinct.setMapOutputValueClass(Text.class);
        distinct.setOutputKeyClass(Text.class);
        distinct.setOutputValueClass(NullWritable.class);
        FileOutputFormat.setOutputPath(distinct, new Path(args[2]));
        if (!distinct.waitForCompletion(true)) {
            return 1;
        }

        Job dau = Job.getInstance(configuration, "nexus-activity-dau");
        dau.setJarByClass(ActivityPipeline.class);
        dau.setMapperClass(DauCountMapper.class);
        dau.setCombinerClass(IntSumReducer.class);
        dau.setReducerClass(IntSumReducer.class);
        dau.setMapOutputKeyClass(Text.class);
        dau.setMapOutputValueClass(IntWritable.class);
        dau.setOutputKeyClass(Text.class);
        dau.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(dau, new Path(args[2]));
        FileOutputFormat.setOutputPath(dau, new Path(args[3]));
        return dau.waitForCompletion(true) ? 0 : 1;
    }
}
