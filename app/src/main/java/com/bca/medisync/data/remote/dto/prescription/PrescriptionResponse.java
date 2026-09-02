package com.bca.medisync.data.remote.dto.prescription;

import com.bca.medisync.data.remote.dto.medication.MedicationResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class PrescriptionResponse {
  private int id;
  private int doctor_id;
  private int appointment_id;
  private int patient_id;
  private String diagnosis;
  private String instructions;
  private String created_at;
  private String follow_up_date;
  private List<MedicationResponse> medications;
}
