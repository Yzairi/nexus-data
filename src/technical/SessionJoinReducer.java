package fr.nexus.hadoop.technical;

import fr.nexus.hadoop.common.NexusCounters;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public final class SessionJoinReducer extends Reducer<Text, Text, Text, SessionMetricWritable> {
    private final Text outputKey = new Text();

    @Override
    protected void reduce(Text sessionId, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        boolean started = false;
        boolean incident = false;
        boolean abandoned = false;
        String version = "UNKNOWN";
        String region = "UNKNOWN";

        for (Text value : values) {
            String[] fields = value.toString().split("\u0001", -1);
            if (fields.length == 0) {
                continue;
            }
            if ("L".equals(fields[0])) {
                incident = true;
            } else if ("T".equals(fields[0]) && fields.length >= 6) {
                started |= "1".equals(fields[1]);
                incident |= "1".equals(fields[2]);
                abandoned |= "1".equals(fields[3]);
                if (!"UNKNOWN".equals(fields[4])) {
                    version = fields[4];
                }
                if (!"UNKNOWN".equals(fields[5])) {
                    region = fields[5];
                }
            }
        }
        if (!started) {
            return;
        }

        context.getCounter(NexusCounters.JOINED_SESSIONS).increment(1);
        if (incident) {
            context.getCounter(NexusCounters.INCIDENT_SESSIONS).increment(1);
        }
        if (abandoned) {
            context.getCounter(NexusCounters.ABANDONED_SESSIONS).increment(1);
        }

        SessionMetricWritable metric = new SessionMetricWritable(
                1,
                incident ? 1 : 0,
                abandoned ? 1 : 0,
                incident && abandoned ? 1 : 0);
        write(context, "OVERALL", metric);
        write(context, "VERSION=" + version, metric);
        write(context, "REGION=" + region, metric);
    }

    private void write(Context context, String group, SessionMetricWritable metric)
            throws IOException, InterruptedException {
        outputKey.set(group);
        context.write(outputKey, metric);
    }
}
