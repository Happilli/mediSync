package com.bca.medisync.data.remote.dto.prescription;

import com.bca.medisync.data.remote.dto.medication.MedicationCreateRequest;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PrescriptionCreateRequest {
  private int appointment_id;
  private String diagnosis;
  private String instructions;
  private String follow_up_date;
  private List<MedicationCreateRequest> medications;
}
