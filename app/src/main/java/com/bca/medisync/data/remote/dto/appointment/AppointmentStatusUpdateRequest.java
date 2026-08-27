package com.bca.medisync.data.remote.dto.appointment;

public class AppointmentStatusUpdateRequest {
  private String status;

  public AppointmentStatusUpdateRequest(String status) {
    this.status = status;
  }
}
