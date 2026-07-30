package com.bca.medisync.data.model;

public class TimeSlot {
    private final String time;
    private final boolean isAvailable;

    public TimeSlot(String time, boolean isAvailable) {
        this.time = time;
        this.isAvailable = isAvailable;
    }

    public String getTime() { return time; }
    public boolean isAvailable() { return isAvailable; }
}
