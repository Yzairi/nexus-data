package fr.nexus.hadoop.common;

import com.fasterxml.jackson.databind.JsonNode;

public final class TelemetryEvent {
    private final String eventId;
    private final String eventType;
    private final String utcDay;
    private final String playerId;
    private final String sessionId;
    private final String gameVersion;
    private final String region;
    private final JsonNode payload;

    public TelemetryEvent(
            String eventId,
            String eventType,
            String utcDay,
            String playerId,
            String sessionId,
            String gameVersion,
            String region,
            JsonNode payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.utcDay = utcDay;
        this.playerId = playerId;
        this.sessionId = sessionId;
        this.gameVersion = gameVersion;
        this.region = region;
        this.payload = payload;
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getUtcDay() { return utcDay; }
    public String getPlayerId() { return playerId; }
    public String getSessionId() { return sessionId; }
    public String getGameVersion() { return gameVersion; }
    public String getRegion() { return region; }

    public String payloadText(String field) {
        if (payload == null || payload.isNull()) {
            return null;
        }
        JsonNode value = payload.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
