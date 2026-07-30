package com.bca.medisync.data.remote.dto.timeslot;

import java.io.Serializable;

public class TimeSlotResponse implements Serializable {
  private int id;
  private int doctor_id;
  private int hospital_id;
  private String appointment_at;
  private boolean is_available;

  public TimeSlotResponse() {
  }

  public TimeSlotResponse(String appointment_at, boolean is_available) {
    this.appointment_at = appointment_at;
    this.is_available = is_available;
  }

  public int getId() {
    return id;
  }

  public int getDoctor_id() {
    return doctor_id;
  }

  public int getHospital_id() {
    return hospital_id;
  }

  public String getAppointment_at() {
    return appointment_at;
  }

  public boolean isIs_available() {
    return is_available;
  }
}
