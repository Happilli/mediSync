package com.bca.medisync.data.remote.dto.login;

import lombok.Getter;

@Getter
public class LoginResponse {
  private String access_token;
  private String token_type;
  private String role;
  private String email;
}
