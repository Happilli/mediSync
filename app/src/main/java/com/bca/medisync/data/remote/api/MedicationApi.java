package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.medication.MedicationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface MedicationApi {
  @GET("api/v1/medications/me")
  Call<List<MedicationResponse>> getMyMedications();

  @PATCH("api/v1/medications/{schedule_id}/taken")
  Call<MedicationResponse> markTaken(@Path("schedule_id") int scheduleId);
}
