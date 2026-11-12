package fr.nexus.hadoop.technical;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class SessionMetricWritable implements Writable {
    private long sessions;
    private long incidents;
    private long abandons;
    private long incidentAndAbandon;

    public SessionMetricWritable() {}

    public SessionMetricWritable(long sessions, long incidents, long abandons, long incidentAndAbandon) {
        this.sessions = sessions;
        this.incidents = incidents;
        this.abandons = abandons;
        this.incidentAndAbandon = incidentAndAbandon;
    }

    public long getSessions() { return sessions; }
    public long getIncidents() { return incidents; }
    public long getAbandons() { return abandons; }
    public long getIncidentAndAbandon() { return incidentAndAbandon; }

    public void add(SessionMetricWritable other) {
        sessions += other.sessions;
        incidents += other.incidents;
        abandons += other.abandons;
        incidentAndAbandon += other.incidentAndAbandon;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeLong(sessions);
        output.writeLong(incidents);
        output.writeLong(abandons);
        output.writeLong(incidentAndAbandon);
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        sessions = input.readLong();
        incidents = input.readLong();
        abandons = input.readLong();
        incidentAndAbandon = input.readLong();
    }
}
