package fr.nexus.hadoop.activity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.nexus.hadoop.common.NexusCounters;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public final class PlayerReferenceMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Text OUTPUT_KEY = new Text();
    private static final Text PLAYER_MARKER = new Text("P");

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        context.getCounter(NexusCounters.INPUT_LINES).increment(1);
        try {
            JsonNode root = MAPPER.readTree(value.toString());
            JsonNode id = root.get("_id");
            if (id == null || id.isNull() || id.asText().trim().isEmpty()) {
                context.getCounter(NexusCounters.MISSING_PLAYER_ID).increment(1);
                return;
            }
            OUTPUT_KEY.set(id.asText());
            context.write(OUTPUT_KEY, PLAYER_MARKER);
            context.getCounter(NexusCounters.KNOWN_PLAYERS).increment(1);
        } catch (IOException exception) {
            context.getCounter(NexusCounters.INVALID_JSON).increment(1);
        }
    }
}
