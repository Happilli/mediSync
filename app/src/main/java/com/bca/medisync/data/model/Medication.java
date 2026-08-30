package com.bca.medisync.data.model;

public class Medication {
  private final int scheduleId;
  private final int medicationId;
  private final String name;
  private final String dosage;
  private final String frequency;
  private final String time;
  private final String label;
  private final String duration;
  private final boolean taken;
  private final String instruction;
  private final String doctorName;

  public Medication(
      int scheduleId,
      int medicationId,
      String name,
      String dosage,
      String frequency,
      String time,
      String label,
      String duration,
      boolean taken,
      String instruction,
      String doctorName) {
    this.scheduleId = scheduleId;
    this.medicationId = medicationId;
    this.name = name;
    this.dosage = dosage;
    this.frequency = frequency;
    this.time = time;
    this.label = label;
    this.duration = duration;
    this.taken = taken;
    this.instruction = instruction;
    this.doctorName = doctorName;
  }

  public int getScheduleId() {
    return scheduleId;
  }

  public int getMedicationId() {
    return medicationId;
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

  public String getLabel() {
    return label;
  }

  public String getDuration() {
    return duration;
  }

  public boolean isTaken() {
    return taken;
  }

  public String getInstruction() {
    return instruction;
  }

  public String getDoctorName() {
    return doctorName;
  }
}
