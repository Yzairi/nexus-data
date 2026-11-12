package fr.nexus.hadoop.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TelemetryParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DAY_FIRST = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter SPACE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TelemetryParser() {}

    public static TelemetryEvent parse(String line) throws IOException {
        JsonNode root = MAPPER.readTree(line);
        int schema = root.path("schema_version").asInt(-1);
        if (schema != 1 && schema != 2) {
            throw new IOException("Unsupported schema_version: " + schema);
        }

        String eventType = text(root, schema == 2 ? "evt" : "event_type");
        JsonNode timestampNode = root.get(schema == 2 ? "ts" : "timestamp");
        String playerId = text(root, schema == 2 ? "pid" : "player_id");
        String sessionId = text(root, schema == 2 ? "session" : "session_id");
        String version = text(root, schema == 2 ? "ver" : "game_version");

        return new TelemetryEvent(
                text(root, "event_id"),
                eventType,
                utcDay(timestampNode),
                playerId,
                sessionId,
                version,
                text(root, "region"),
                root.path("payload"));
    }

    static String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.trim().isEmpty() ? null : text.trim();
    }

    public static String utcDay(JsonNode value) throws IOException {
        if (value == null || value.isNull()) {
            throw new IOException("Missing timestamp");
        }
        try {
            Instant instant;
            if (value.isNumber()) {
                long raw = value.asLong();
                instant = raw > 10_000_000_000L ? Instant.ofEpochMilli(raw) : Instant.ofEpochSecond(raw);
            } else {
                String text = value.asText().trim();
                if (text.matches("^-?\\d+$")) {
                    long raw = Long.parseLong(text);
                    instant = raw > 10_000_000_000L ? Instant.ofEpochMilli(raw) : Instant.ofEpochSecond(raw);
                } else {
                    instant = parseTextTimestamp(text);
                }
            }
            return instant.atZone(ZoneOffset.UTC).toLocalDate().toString();
        } catch (DateTimeParseException | NumberFormatException exception) {
            throw new IOException("Invalid timestamp: " + value.asText(), exception);
        }
    }

    private static Instant parseTextTimestamp(String text) {
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            // Try the other documented source formats below.
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // Continue.
        }
        try {
            return LocalDateTime.parse(text, DAY_FIRST).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // Continue.
        }
        try {
            return LocalDateTime.parse(text, SPACE_DATE_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // Continue.
        }
        return LocalDate.parse(text).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
