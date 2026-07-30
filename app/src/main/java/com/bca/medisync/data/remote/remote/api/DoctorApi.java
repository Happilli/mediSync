package com.bca.medisync.data.remote.remote.api;

import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DoctorApi {

    @GET("/api/v1/doctors/me")
    Call<DoctorProfileResponse> getMyProfile();

    @GET("/api/v1/appointments/me/doctor")
    Call<List<AppointmentResponse>> getMyAppointments();

}