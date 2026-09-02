package com.bca.medisync.data.remote.dto.doctor;

public class DoctorProfileResponse extends DoctorResponse {
  private int patients_this_month;
  private int total_patients;
  private boolean has_security_answer;
  private int upcoming_followups;

  public int getPatients_this_month() {
    return patients_this_month;
  }

  public int getTotal_patients() {
    return total_patients;
  }

  public boolean isHas_security_answer() {
    return has_security_answer;
  }

  public int getUpcoming_followups() {
    return upcoming_followups;
  }
}
