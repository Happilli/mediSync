package com.bca.medisync.data.remote.dto.login;

public record LoginResponse(String access_token, String token_type, String role, String email) {}
