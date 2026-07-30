package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DoctorApi {

    @GET("api/v1/doctors/me/")
    Call<DoctorResponse> getMyProfile();

}