package com.bca.medisync.data.remote.dto.auth;

public record ForgotPasswordVerifyRequest(
    String email, String security_answer, String new_password) {}
