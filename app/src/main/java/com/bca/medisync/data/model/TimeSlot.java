package com.bca.medisync.data.model;

public class TimeSlot {
    private final String time;
    private final boolean available;

    public  TimeSlot(String time, boolean available){
        this.time = time;
        this.available = available;
    }
    public String getTime() {
        return time;
    }

    public boolean isAvailable() {
        return available;
    }
}
