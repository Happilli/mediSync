package com.bca.medisync.data.model;

public class MedicalHistoryEntry {
    private final String date;
    private final String title;
    private final String description;

    public MedicalHistoryEntry(String date, String title, String description) {
        this.date = date;
        this.title = title;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
