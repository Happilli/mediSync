package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.prescription.PrescriptionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PrescriptionApi {
  @GET("api/v1/prescriptions/me")
  Call<List<PrescriptionResponse>> getMyPrescriptions();

  @GET("api/v1/prescriptions/{prescription_id}")
  Call<PrescriptionResponse> getPrescriptionDetail(@Path("prescription_id") int prescriptionId);
}
