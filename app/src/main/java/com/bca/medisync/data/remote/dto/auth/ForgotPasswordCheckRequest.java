package com.bca.medisync.data.remote.dto.auth;

public class ForgotPasswordCheckRequest {
  private String email;

  public ForgotPasswordCheckRequest(String email) {
    this.email = email;
  }
}
