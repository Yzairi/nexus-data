package fr.nexus.hadoop.technical;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

public final class TechnicalPipeline {
    private TechnicalPipeline() {}

    public static int run(Configuration configuration, String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("technical <telemetry-input> <logs-input> <work-output> <analytics-output>");
            return 2;
        }

        Job join = Job.getInstance(configuration, "nexus-technical-session-join");
        join.setJarByClass(TechnicalPipeline.class);
        MultipleInputs.addInputPath(join, new Path(args[0]), TextInputFormat.class, TelemetrySessionMapper.class);
        MultipleInputs.addInputPath(join, new Path(args[1]), TextInputFormat.class, LogSessionMapper.class);
        join.setReducerClass(SessionJoinReducer.class);
        join.setMapOutputKeyClass(Text.class);
        join.setMapOutputValueClass(Text.class);
        join.setOutputKeyClass(Text.class);
        join.setOutputValueClass(SessionMetricWritable.class);
        join.setOutputFormatClass(SequenceFileOutputFormat.class);
        FileOutputFormat.setOutputPath(join, new Path(args[2]));
        if (!join.waitForCompletion(true)) {
            return 1;
        }

        Job aggregate = Job.getInstance(configuration, "nexus-technical-aggregation");
        aggregate.setJarByClass(TechnicalPipeline.class);
        aggregate.setInputFormatClass(SequenceFileInputFormat.class);
        aggregate.setMapperClass(MetricMapper.class);
        aggregate.setReducerClass(MetricSumReducer.class);
        aggregate.setMapOutputKeyClass(Text.class);
        aggregate.setMapOutputValueClass(SessionMetricWritable.class);
        aggregate.setOutputKeyClass(Text.class);
        aggregate.setOutputValueClass(Text.class);
        SequenceFileInputFormat.addInputPath(aggregate, new Path(args[2]));
        FileOutputFormat.setOutputPath(aggregate, new Path(args[3]));
        return aggregate.waitForCompletion(true) ? 0 : 1;
    }
}
