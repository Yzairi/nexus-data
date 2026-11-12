package fr.nexus.hadoop.activity;

import fr.nexus.hadoop.common.NexusCounters;
import fr.nexus.hadoop.common.TelemetryEvent;
import fr.nexus.hadoop.common.TelemetryParser;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public final class TelemetryActivityMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final Text OUTPUT_KEY = new Text();
    private static final Text OUTPUT_VALUE = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        context.getCounter(NexusCounters.INPUT_LINES).increment(1);
        try {
            TelemetryEvent event = TelemetryParser.parse(value.toString());
            if (!"session_start".equals(event.getEventType())) {
                return;
            }
            if (event.getPlayerId() == null) {
                context.getCounter(NexusCounters.MISSING_PLAYER_ID).increment(1);
                return;
            }
            if (!event.getPlayerId().matches("P\\d{8}")) {
                context.getCounter(NexusCounters.INVALID_PLAYER_ID).increment(1);
                return;
            }
            OUTPUT_KEY.set(event.getPlayerId());
            OUTPUT_VALUE.set("A\t" + event.getUtcDay());
            context.write(OUTPUT_KEY, OUTPUT_VALUE);
            context.getCounter(NexusCounters.ACTIVITY_EVENTS).increment(1);
        } catch (IOException exception) {
            context.getCounter(NexusCounters.REJECTED_LINES).increment(1);
        }
    }
}
