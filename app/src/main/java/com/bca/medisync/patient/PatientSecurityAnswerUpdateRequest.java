package com.bca.medisync.patient;

public class PatientSecurityAnswerUpdateRequest {
  private String current_password;
  private String security_answer;

  public PatientSecurityAnswerUpdateRequest(String current_password, String security_answer) {
    this.current_password = current_password;
    this.security_answer = security_answer;
  }
}
