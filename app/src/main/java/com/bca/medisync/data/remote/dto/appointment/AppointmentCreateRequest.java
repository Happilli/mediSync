package com.bca.medisync.data.remote.dto.appointment;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AppointmentCreateRequest {
  private int timeslot_id;
  private String notes;
}
