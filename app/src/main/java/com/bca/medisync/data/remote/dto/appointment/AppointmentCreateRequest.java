package com.bca.medisync.data.remote.dto.appointment;

public class AppointmentCreateRequest {
  private int timeslot_id;
  private String notes;

  public AppointmentCreateRequest(int timeslot_id, String notes) {
    this.timeslot_id = timeslot_id;
    this.notes = notes;
  }
}
