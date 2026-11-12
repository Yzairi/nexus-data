package fr.nexus.hadoop.technical;

import fr.nexus.hadoop.common.NexusCounters;
import fr.nexus.hadoop.common.TechnicalLog;
import fr.nexus.hadoop.common.TechnicalLogParser;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public final class LogSessionMapper extends Mapper<LongWritable, Text, Text, Text> {
    private final Text outputKey = new Text();
    private static final Text INCIDENT = new Text("L\u00011");

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        context.getCounter(NexusCounters.INPUT_LINES).increment(1);
        try {
            TechnicalLog log = TechnicalLogParser.parse(value.toString());
            if (!log.isIncident()) {
                return;
            }
            if (log.getSessionId() == null || log.getSessionId().trim().isEmpty()) {
                context.getCounter(NexusCounters.MISSING_SESSION_ID).increment(1);
                return;
            }
            outputKey.set(log.getSessionId());
            context.write(outputKey, INCIDENT);
        } catch (IOException exception) {
            context.getCounter(NexusCounters.INVALID_LOG).increment(1);
        }
    }
}
