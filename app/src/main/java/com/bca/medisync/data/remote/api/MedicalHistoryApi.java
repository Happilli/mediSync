package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.model.MedicalHistory;
import com.bca.medisync.data.remote.dto.medicalhistory.MedicalHistoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MedicalHistoryApi {
  @GET("api/v1/medical-history/me")
  Call<List<MedicalHistoryResponse>> getMyMedicalHistory();

  @GET("api/v1/medical-history/patient/{patient_id}")
  Call<List<MedicalHistoryResponse>> getPatientHistory(@Path("patient_id") int patientId);
}
