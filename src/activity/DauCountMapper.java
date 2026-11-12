package fr.nexus.hadoop.activity;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public final class DauCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private static final IntWritable ONE = new IntWritable(1);
    private final Text day = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        int separator = line.indexOf('\t');
        if (separator <= 0) {
            return;
        }
        day.set(line.substring(0, separator));
        context.write(day, ONE);
    }
}
