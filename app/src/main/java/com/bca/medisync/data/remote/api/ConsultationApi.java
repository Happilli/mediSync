package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.consultation.ConsultationCreateRequest;
import com.bca.medisync.data.remote.dto.consultation.ConsultationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ConsultationApi {

  @POST("api/v1/consultations/")
  Call<ConsultationResponse> createConsultation(@Body ConsultationCreateRequest request);

  @GET("api/v1/consultations/appointment/{appointment_id}")
  Call<ConsultationResponse> getConsultationForAppointment(
      @Path("appointment_id") int appointmentId);
}
