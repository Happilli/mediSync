package com.bca.medisync.data.repository;

import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.appointment.AppointmentStatusUpdateRequest;

import java.util.List;

import retrofit2.Callback;

public class AppointmentRepository {

    private final AppointmentApi appointmentApi;

    public AppointmentRepository() {
        appointmentApi = ApiClient.getRetrofit().create(AppointmentApi.class);
    }

    public void getDoctorAppointments(Callback<List<AppointmentResponse>> callback) {
        appointmentApi.getDoctorAppointments().enqueue(callback);
    }

    public void updateAppointmentStatus(String appointmentId, String status, Callback<AppointmentResponse> callback) {
        appointmentApi.updateAppointmentStatus(appointmentId, new AppointmentStatusUpdateRequest(status)).enqueue(callback);
    }

}