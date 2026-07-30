package com.bca.medisync.data.remote.remote.api;

import com.bca.medisync.data.remote.dto.patient.DoctorPatientResponse;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;

import java.util.List;

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

  @GET("/api/v1/patients/doctor")
  Call<List<DoctorPatientResponse>> getDoctorPatients();

  @Multipart
  @POST("/api/v1/patients/request-verification")
  Call<PatientResponse> requestVerification(
          @Part("citizenship_number") RequestBody citizenshipNumber,
          @Part MultipartBody.Part file
  );
}