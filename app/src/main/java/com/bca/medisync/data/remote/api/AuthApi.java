package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.auth.ForgotPasswordCheckRequest;
import com.bca.medisync.data.remote.dto.auth.ForgotPasswordCheckResponse;
import com.bca.medisync.data.remote.dto.auth.ForgotPasswordVerifyRequest;
import com.bca.medisync.data.remote.dto.login.LoginRequest;
import com.bca.medisync.data.remote.dto.login.LoginResponse;
import com.bca.medisync.data.remote.dto.register.PatientRegisterRequest;
import com.bca.medisync.data.remote.dto.register.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
  @POST("api/v1/auth/login")
  Call<LoginResponse> login(@Body LoginRequest request);

  @POST("api/v1/auth/register/patient")
  Call<RegisterResponse> registerPatient(@Body PatientRegisterRequest request);

  @POST("api/v1/auth/forgot-password/check")
  Call<ForgotPasswordCheckResponse> checkForgotPassword(@Body ForgotPasswordCheckRequest request);

  @POST("api/v1/auth/forgot-password/verify")
  Call<Void> verifyForgotPassword(@Body ForgotPasswordVerifyRequest request);
}
