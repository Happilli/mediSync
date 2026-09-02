package com.bca.medisync.data.remote.dto.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ForgotPasswordVerifyRequest {
  private String email;
  private String security_answer;
  private String new_password;
}
