package com.bca.medisync.data.model;

public class TimeSlot {
  private final int id;
  private final String appointmentAt;
  private final String displayTime;
  private final boolean available;

  public TimeSlot(int id, String appointmentAt, String displayTime, boolean available) {
    this.id = id;
    this.appointmentAt = appointmentAt;
    this.displayTime = displayTime;
    this.available = available;
  }

  public int getId() {
    return id;
  }

  public String getAppointmentAt() {
    return appointmentAt;
  }

  public String getDisplayTime() {
    return displayTime;
  }

  public boolean isAvailable() {
    return available;
  }
}
