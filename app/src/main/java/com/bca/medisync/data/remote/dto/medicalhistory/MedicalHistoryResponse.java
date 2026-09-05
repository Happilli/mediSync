package com.bca.medisync.data.remote.dto.medicalhistory;

import lombok.Getter;

@Getter
public class MedicalHistoryResponse {
  private int id;
  private int doctor_id;
  private int patient_id;
  private Integer appointment_id;
  private String title;
  private String description;
  private String date;
}
