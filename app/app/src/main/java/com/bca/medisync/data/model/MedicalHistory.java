package com.bca.medisync.data.model;

import java.util.List;

public class MedicalHistory {
    private final String currentMedication, medicationFrequency;
    private final String latestTestResult, testResultValue;
    private final List<MedicalHistoryEntry> timeline;

    public MedicalHistory(String currentMedication, String medicationFrequency, String latestTestResult, String testResultValue, List<MedicalHistoryEntry> timeline) {
        this.currentMedication = currentMedication;
        this.medicationFrequency = medicationFrequency;
        this.latestTestResult = latestTestResult;
        this.testResultValue = testResultValue;
        this.timeline = timeline;
    }

    public String getCurrentMedication() { return currentMedication; }
    public String getMedicationFrequency() { return medicationFrequency; }
    public String getLatestTestResult() { return latestTestResult; }
    public String getTestResultValue() { return testResultValue; }
    public List<MedicalHistoryEntry> getTimeline() { return timeline; }
    
    public String getLatestRxName() { return currentMedication; }
    public String getLatestRxDesc() { return medicationFrequency; }
    public String getLatestLabTitle() { return latestTestResult; }
    public String getLatestLabDesc() { return testResultValue; }
}
