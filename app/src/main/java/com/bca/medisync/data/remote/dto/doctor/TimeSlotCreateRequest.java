package com.bca.medisync.data.remote.dto.doctor;

public class TimeSlotCreateRequest {
  private String appointment_at;

  public TimeSlotCreateRequest(String appointment_at) {
    this.appointment_at = appointment_at;
  }
}
