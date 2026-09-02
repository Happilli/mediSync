package com.bca.medisync.data.remote.dto.doctor;

import lombok.Getter;

@Getter
public class DoctorProfileResponse extends DoctorResponse {
  private int patients_this_month;
  private int total_patients;
  private boolean has_security_answer;
  private int upcoming_followups;
}
