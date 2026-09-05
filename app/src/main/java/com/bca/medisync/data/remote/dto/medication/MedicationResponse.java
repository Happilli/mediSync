package com.bca.medisync.data.remote.dto.medication;

import lombok.Getter;

@Getter
public class MedicationResponse {
  private int schedule_id;
  private int medication_id;
  private int prescription_id;
  private int patient_id;
  private String name;
  private String dosage;
  private String instruction;
  private String dosage_time;
  private String label;
  private int frequency_per_day;
  private int duration_days;
  private String start_date;
  private String end_date;
  private boolean is_taken;
  private String taken_at;
  private boolean is_active;
  private int doctor_id;
  private String doctor_name;
  private String dispense_status;
}
