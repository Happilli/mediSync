package com.bca.medisync.data.remote.dto.patient;

import com.bca.medisync.data.remote.dto.MonthlyAppointmentCount;
import java.util.List;
import java.util.Map;

public record PatientStatsResponse(
    int total_appointments,
    Map<String, Integer> appointments_by_status,
    List<MonthlyAppointmentCount> appointments_last_6_months,
    int upcoming_appointments,
    double medication_adherence_percent,
    int doses_taken,
    int doses_expected,
    int total_prescriptions,
    int total_consultations,
    VitalsTrend vitals_trend) {

  public record VitalsTrend(
      List<VitalPoint> blood_pressure,
      List<VitalPoint> heart_rate,
      List<VitalPoint> temperature,
      List<VitalPoint> weight) {}

  public record VitalPoint(String date, String value) {}
}
