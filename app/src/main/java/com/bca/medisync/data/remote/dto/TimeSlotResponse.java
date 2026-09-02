package com.bca.medisync.data.remote.dto;

import lombok.Getter;

@Getter
public class TimeSlotResponse {
  private int id;
  private int doctor_id;
  private int hospital_id;
  private String appointment_at;
  private boolean is_available;
}
