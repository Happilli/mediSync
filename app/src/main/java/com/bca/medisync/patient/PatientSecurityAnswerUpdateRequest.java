package com.bca.medisync.patient;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PatientSecurityAnswerUpdateRequest {
  private String current_password;
  private String security_answer;
}
