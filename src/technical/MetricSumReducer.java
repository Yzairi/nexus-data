package fr.nexus.hadoop.technical;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Locale;

public final class MetricSumReducer extends Reducer<Text, SessionMetricWritable, Text, Text> {
    private final Text output = new Text();

    @Override
    protected void reduce(Text key, Iterable<SessionMetricWritable> values, Context context)
            throws IOException, InterruptedException {
        SessionMetricWritable total = new SessionMetricWritable();
        for (SessionMetricWritable value : values) {
            total.add(value);
        }
        long sessions = total.getSessions();
        long incidents = total.getIncidents();
        long abandons = total.getAbandons();
        long both = total.getIncidentAndAbandon();
        long withoutIncident = sessions - incidents;
        long abandonWithoutIncident = abandons - both;

        output.set(String.format(Locale.ROOT, "%d\t%d\t%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f",
                sessions,
                incidents,
                abandons,
                both,
                rate(incidents, sessions),
                rate(abandons, sessions),
                rate(both, incidents),
                rate(abandonWithoutIncident, withoutIncident)));
        context.write(key, output);
    }

    private static double rate(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : (double) numerator / denominator;
    }
}
