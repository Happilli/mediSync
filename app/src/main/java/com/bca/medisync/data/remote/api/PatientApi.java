package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.patient.PatientPublicResponse;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.data.remote.dto.patient.PatientUpdateRequest;
import com.bca.medisync.patient.PatientSecurityAnswerUpdateRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface PatientApi {
  @GET("/api/v1/patients/me")
  Call<PatientResponse> getMyProfile();

  @PATCH("/api/v1/patients/me")
  Call<PatientResponse> updateMyProfile(@Body PatientUpdateRequest request);

  @Multipart
  @PATCH("/api/v1/patients/me/profile-pic")
  Call<PatientResponse> updateProfilePic(@Part MultipartBody.Part file);

  @Multipart
  @POST("/api/v1/patients/request-verification")
  Call<PatientResponse> requestVerification(
      @Part("citizenship_number") RequestBody citizenshipNumber, @Part MultipartBody.Part file);

  @PATCH("/api/v1/patients/me/security-answer")
  Call<Map<String, String>> updateSecurityAnswer(@Body PatientSecurityAnswerUpdateRequest request);

  @GET("/api/v1/patients/treated")
  Call<List<PatientPublicResponse>> getTreatedPatients();

  @GET("/api/v1/patients/doctor")
  Call<List<PatientPublicResponse>> getMyDoctorPatients();

  @GET("/api/v1/patients/{patient_id}")
  Call<PatientPublicResponse> getPatientDetail(@Path("patient_id") int patientId);
}
