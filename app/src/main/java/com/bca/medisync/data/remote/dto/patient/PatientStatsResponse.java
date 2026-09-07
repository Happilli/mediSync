package com.bca.medisync.data.remote.dto.patient;

import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
public class PatientStatsResponse {
  private int total_appointments;
  private Map<String, Integer> appointments_by_status;
  private List<MonthlyAppointmentCount> appointments_last_6_months;
  private int upcoming_appointments;
  private double medication_adherence_percent;
  private int doses_taken;
  private int doses_expected;
  private int total_prescriptions;
  private int total_consultations;
  private VitalsTrend vitals_trend;

  @Getter
  public static class MonthlyAppointmentCount {
    private String month;
    private int count;
  }

  @Getter
  public static class VitalsTrend {
    private List<VitalPoint> blood_pressure;
    private List<VitalPoint> heart_rate;
    private List<VitalPoint> temperature;
    private List<VitalPoint> weight;
  }

  @Getter
  public static class VitalPoint {
    private String date;
    private String value;
  }
}
