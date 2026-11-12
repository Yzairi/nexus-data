package fr.nexus.hadoop.technical;

import fr.nexus.hadoop.common.NexusCounters;
import fr.nexus.hadoop.common.TelemetryEvent;
import fr.nexus.hadoop.common.TelemetryParser;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public final class TelemetrySessionMapper extends Mapper<LongWritable, Text, Text, Text> {
    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        context.getCounter(NexusCounters.INPUT_LINES).increment(1);
        try {
            TelemetryEvent event = TelemetryParser.parse(value.toString());
            String type = event.getEventType();
            boolean started = "session_start".equals(type);
            boolean incident = "crash".equals(type) || "disconnect".equals(type);
            boolean abandoned = "level_abandon".equals(type)
                    || ("match_end".equals(type) && "abandon".equals(event.payloadText("outcome")));
            if (!started && !incident && !abandoned) {
                return;
            }
            if (event.getSessionId() == null) {
                context.getCounter(NexusCounters.MISSING_SESSION_ID).increment(1);
                return;
            }
            outputKey.set(event.getSessionId());
            outputValue.set(String.join("\u0001",
                    "T",
                    started ? "1" : "0",
                    incident ? "1" : "0",
                    abandoned ? "1" : "0",
                    safe(event.getGameVersion()),
                    safe(event.getRegion())));
            context.write(outputKey, outputValue);
        } catch (IOException exception) {
            context.getCounter(NexusCounters.REJECTED_LINES).increment(1);
        }
    }

    private static String safe(String value) {
        return value == null ? "UNKNOWN" : value;
    }
}
