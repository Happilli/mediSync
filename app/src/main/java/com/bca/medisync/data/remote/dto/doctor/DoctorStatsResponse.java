package com.bca.medisync.data.remote.dto.doctor;

import com.bca.medisync.data.remote.dto.MonthlyAppointmentCount;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class DoctorStatsResponse {
  private int total_appointments;
  private Map<String, Integer> appointments_by_status;
  private List<MonthlyAppointmentCount> appointments_last_6_months;
  private int upcoming_appointments;
  private int total_patients;
  private int patients_this_month;
  private int total_prescriptions;
  private int total_consultations;
  private int upcoming_followups;
}
