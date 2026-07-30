package com.bca.medisync.data.model;

public class TimeSlot {
    private final int id;
    private final String isoDateTime;
    private final String time;
    private final boolean available;

    public TimeSlot(int id, String isoDateTime, String time, boolean available) {
        this.id = id;
        this.isoDateTime = isoDateTime;
        this.time = time;
        this.available = available;
    }

    public TimeSlot(String time, boolean available) {
        this(0, "", time, available);
    }

    public int getId() {
        return id;
    }

    public String getIsoDateTime() {
        return isoDateTime;
    }

    public String getTime() {
        return time;
    }

    public boolean isAvailable() {
        return available;
    }
}
