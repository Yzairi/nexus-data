package fr.nexus.hadoop.activity;

import fr.nexus.hadoop.common.NexusCounters;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class KnownPlayerActivityReducer extends Reducer<Text, Text, Text, NullWritable> {
    private final Text output = new Text();

    @Override
    protected void reduce(Text playerId, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        boolean knownPlayer = false;
        Set<String> activeDays = new HashSet<>();
        for (Text value : values) {
            String text = value.toString();
            if ("P".equals(text)) {
                knownPlayer = true;
            } else if (text.startsWith("A\t")) {
                activeDays.add(text.substring(2));
            }
        }
        if (!knownPlayer) {
            context.getCounter(NexusCounters.UNKNOWN_PLAYERS).increment(1);
            return;
        }
        for (String day : activeDays) {
            output.set(day + "\t" + playerId.toString());
            context.write(output, NullWritable.get());
        }
    }
}
