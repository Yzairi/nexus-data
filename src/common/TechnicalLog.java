package fr.nexus.hadoop.common;

public final class TechnicalLog {
    private final String level;
    private final String service;
    private final String code;
    private final String playerId;
    private final String sessionId;

    public TechnicalLog(String level, String service, String code, String playerId, String sessionId) {
        this.level = level;
        this.service = service;
        this.code = code;
        this.playerId = playerId;
        this.sessionId = sessionId;
    }

    public String getLevel() { return level; }
    public String getService() { return service; }
    public String getCode() { return code; }
    public String getPlayerId() { return playerId; }
    public String getSessionId() { return sessionId; }

    public boolean isIncident() {
        return "WARN".equalsIgnoreCase(level)
                || "ERROR".equalsIgnoreCase(level)
                || "FATAL".equalsIgnoreCase(level);
    }
}
