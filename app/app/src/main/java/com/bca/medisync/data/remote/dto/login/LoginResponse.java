package com.bca.medisync.data.remote.dto.login;

public class LoginResponse {
  private String access_token;
  private String token_type;
  private String role;
  private String email;

  public String getAccess_token() {
    return access_token;
  }

  public String getToken_type() {
    return token_type;
  }

  public String getRole() {
    return role;
  }

  public String getEmail() {
    return email;
  }
}
