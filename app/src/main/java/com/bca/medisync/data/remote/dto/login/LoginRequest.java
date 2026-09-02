package com.bca.medisync.data.remote.dto.login;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginRequest {
  private String email;
  private String password;
}
