package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.hospital.HospitalResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HospitalApi {
  @GET("/api/v1/hospitals/")
  Call<List<HospitalResponse>> getHospitals(@Query("search") String search);

  @GET("/api/v1/hospitals/{hospital_id}")
  Call<HospitalResponse> getHospitalDetail(@Path("hospital_id") int hospitalId);
}
