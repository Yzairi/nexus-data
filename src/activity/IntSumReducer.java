package fr.nexus.hadoop.activity;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public final class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable output = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable value : values) {
            sum += value.get();
        }
        output.set(sum);
        context.write(key, output);
    }
}
