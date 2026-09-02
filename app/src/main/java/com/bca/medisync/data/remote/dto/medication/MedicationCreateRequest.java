package com.bca.medisync.data.remote.dto.medication;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MedicationCreateRequest {
  private String name;
  private String dosage;
  private String instruction;
  private int duration_days;
  private List<MedicationTimeCreateRequest> dosage_times;
}
