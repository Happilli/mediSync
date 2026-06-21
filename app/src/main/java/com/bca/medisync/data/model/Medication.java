package com.bca.medisync.data.model;

public class Medication {
    private  final  String id;
    private final String name;
    private final String dosage;
    private final  String frequency;
    private final String time;
    private final String duration;
    private final boolean taken;

    public  Medication(String id, String name, String dosage, String frequency, String time, String duration, boolean taken){
        this.id = id;
        this.name =name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.time = time;
        this.duration = duration;
        this.taken = taken;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }

    public boolean isTaken() {
        return taken;
    }
}
