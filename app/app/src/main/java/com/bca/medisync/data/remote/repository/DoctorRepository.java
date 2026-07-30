package com.bca.medisync.data.remote.repository;

import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;

import retrofit2.Call;

public class DoctorRepository {

    private final DoctorApi doctorApi;

    public DoctorRepository() {
        doctorApi = ApiClient.getRetrofit().create(DoctorApi.class);
    }

    public Call<DoctorResponse> getMyProfile() {
        return doctorApi.getMyProfile();
    }
}