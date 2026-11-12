package fr.nexus.hadoop.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TechnicalLogParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern PREFIX = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.*)$");
    private static final Pattern KEY_VALUE = Pattern.compile("(\\w+)=(\\\"[^\\\"]*\\\"|\\S+)");

    private TechnicalLogParser() {}

    public static TechnicalLog parse(String line) throws IOException {
        String trimmed = line.trim();
        if (trimmed.startsWith("{")) {
            JsonNode root = MAPPER.readTree(trimmed);
            return new TechnicalLog(
                    text(root, "level"),
                    text(root, "service"),
                    text(root, "code"),
                    first(root, "player_id", "player"),
                    first(root, "session_id", "session"));
        }

        Matcher prefix = PREFIX.matcher(trimmed);
        if (!prefix.matches()) {
            throw new IOException("Unsupported log format");
        }
        Map<String, String> fields = new HashMap<>();
        Matcher matcher = KEY_VALUE.matcher(prefix.group(4));
        while (matcher.find()) {
            fields.put(matcher.group(1), matcher.group(2).replaceAll("^\\\"|\\\"$", ""));
        }
        return new TechnicalLog(
                prefix.group(2),
                prefix.group(3),
                fields.get("code"),
                first(fields, "player_id", "player"),
                first(fields, "session_id", "session"));
    }

    private static String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String first(JsonNode root, String first, String second) {
        String value = text(root, first);
        return value == null ? text(root, second) : value;
    }

    private static String first(Map<String, String> values, String first, String second) {
        String value = values.get(first);
        return value == null ? values.get(second) : value;
    }
}
