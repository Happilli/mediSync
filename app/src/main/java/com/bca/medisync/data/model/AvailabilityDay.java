package com.bca.medisync.data.model;

public class AvailabilityDay {
    private  final  String day, startTime, endtime;

    public AvailabilityDay(String day, String startTime, String endtime) {
        this.day = day;
        this.startTime = startTime;
        this.endtime = endtime;
    }

    public String getDay() {
        return day;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndtime() {
        return endtime;
    }
}
