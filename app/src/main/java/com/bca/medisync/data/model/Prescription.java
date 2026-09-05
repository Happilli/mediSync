package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
public class Prescription {
  private final int id;
  private final String doctor_name;
  private final String diagnosis;
  private final String instructions;
  private final String createdAt;
  private final String followUpDate;
  private final String dispenseStatus;
  private final List<Medication> medications;
}
