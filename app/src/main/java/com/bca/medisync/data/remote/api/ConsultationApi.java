package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.Consultation.ConsultationCreateRequest;
import com.bca.medisync.data.remote.dto.Consultation.ConsultationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ConsultationApi {

  @POST("api/v1/consultations/")
  Call<ConsultationResponse> createConsultation(@Body ConsultationCreateRequest request);
}
