package fr.nexus.hadoop.technical;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public final class MetricMapper extends Mapper<Text, SessionMetricWritable, Text, SessionMetricWritable> {
    @Override
    protected void map(Text key, SessionMetricWritable value, Context context)
            throws IOException, InterruptedException {
        context.write(key, value);
    }
}
