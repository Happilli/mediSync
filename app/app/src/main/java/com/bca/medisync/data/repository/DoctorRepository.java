package com.bca.medisync.data.repository;

import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;

import java.util.List;

import retrofit2.Call;

public class DoctorRepository {

    private final DoctorApi api;

    public DoctorRepository() {
        api = ApiClient.getRetrofit().create(DoctorApi.class);
    }

    public Call<DoctorProfileResponse> getProfile() {
        return api.getMyProfile();
    }

    public Call<List<AppointmentResponse>> getAppointments() {
        return api.getMyAppointments();
    }
}