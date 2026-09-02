package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class Appointment {
  private final String id;
  private final String patientName;
  private final String doctorName;
  private final String department;
  private final String speciality;
  private final String date;
  private final String time;
  private final String status;
  private final String notes;
  private final int patientId;
}
