package com.bca.medisync.data.remote.dto.medication;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MedicationTimeCreateRequest {
  private String dosage_time;
  private String label;
}
