package com.bca.medisync.data.remote.remote.api;

import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.appointment.AppointmentStatusUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface AppointmentApi {

    @GET("/api/v1/appointments/me/doctor")
    Call<List<AppointmentResponse>> getDoctorAppointments();

    @PATCH("/api/v1/appointments/{appointment_id}/status")
    Call<AppointmentResponse> updateAppointmentStatus(
            @Path("appointment_id") String appointmentId,
            @Body AppointmentStatusUpdateRequest request
    );

}