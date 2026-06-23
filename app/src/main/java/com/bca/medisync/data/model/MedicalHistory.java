package com.bca.medisync.data.model;

import java.util.List;

public class MedicalHistory {
    private final String latestRxName;
    private final String latestRxDesc;
    private final String latestLabTitle;
    private final String latestLabDesc;
    private final List<MedicalHistoryEntry> timeline;

    public MedicalHistory(String latestRxName, String latestRxDesc, String latestLabTitle, String latestLabDesc, List<MedicalHistoryEntry> timeline) {
        this.latestRxName = latestRxName;
        this.latestRxDesc = latestRxDesc;
        this.latestLabTitle = latestLabTitle;
        this.latestLabDesc = latestLabDesc;
        this.timeline = timeline;
    }

    public String getLatestRxName() {
        return latestRxName;
    }

    public String getLatestRxDesc() {
        return latestRxDesc;
    }

    public String getLatestLabTitle() {
        return latestLabTitle;
    }

    public String getLatestLabDesc() {
        return latestLabDesc;
    }

    public List<MedicalHistoryEntry> getTimeline() {
        return timeline;
    }
}
