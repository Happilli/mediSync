package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.patient.PatientResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PatientApi {
    @GET("/api/v1/patients/me")
    Call<PatientResponse> getMyProfile();
}
