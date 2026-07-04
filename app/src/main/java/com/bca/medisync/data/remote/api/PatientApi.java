package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.patient.PatientResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PatientApi {
  @GET("/api/v1/patients/me")
  Call<PatientResponse> getMyProfile();

  @Multipart
  @POST("/api/v1/patients/request-verification")
  Call<PatientResponse> requestVerification(
      @Part("citizenship_number") RequestBody citizenshipNumber, @Part MultipartBody.Part file);
}
