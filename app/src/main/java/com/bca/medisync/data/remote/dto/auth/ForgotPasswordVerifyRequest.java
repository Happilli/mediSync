package com.bca.medisync.data.remote.dto.auth;

public class ForgotPasswordVerifyRequest {
  private String email;
  private String security_answer;
  private String new_password;

  public ForgotPasswordVerifyRequest(String email, String securityAnswer, String newPassword) {
    this.email = email;
    this.security_answer = securityAnswer;
    this.new_password = newPassword;
  }
}
