package com.bca.medisync.data.model;

public class Medication {
    private final String id, name, dosage, frequency, time, duration;
    private final boolean isTaken;

    public Medication(String id, String name, String dosage, String frequency, String time, String duration, boolean isTaken) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.time = time;
        this.duration = duration;
        this.isTaken = isTaken;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public String getTime() { return time; }
    public String getDuration() { return duration; }
    public boolean isTaken() { return isTaken; }
}
