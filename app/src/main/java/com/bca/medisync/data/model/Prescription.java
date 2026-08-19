package com.bca.medisync.data.model;

import java.util.List;

public class Prescription {
  private final int id;
  private final String doctor_name;
  private final String diagnosis;
  private final String instructions;
  private final String followUpDate;
  private final List<Medication> medications;

  public Prescription(
      int id,
      String doctor_name,
      String diagnosis,
      String instructions,
      String followUpDate,
      List<Medication> medications) {
    this.id = id;
    this.doctor_name = doctor_name;
    this.diagnosis = diagnosis;
    this.instructions = instructions;
    this.followUpDate = followUpDate;
    this.medications = medications;
  }

  public int getId() {
    return id;
  }

  public String getDoctor_name() {
    return doctor_name;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public String getInstructions() {
    return instructions;
  }

  public String getFollowUpDate() {
    return followUpDate;
  }

  public List<Medication> getMedications() {
    return medications;
  }
}
