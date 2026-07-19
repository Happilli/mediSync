package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.appointment.AppointmentCreateRequest;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AppointmentApi {
  @POST("api/v1/appointments/")
  Call<AppointmentResponse> createAppointment(@Body AppointmentCreateRequest request);

  @GET("api/v1/appointments/me")
  Call<List<AppointmentResponse>> getMyAppointments(
      @Query("filter_date") String filtreDate, @Query("status") String status);

  @PATCH("api/v1/appointments/{appointment_id}/cancel")
  Call<AppointmentResponse> cancelAppointment(@Path("appointment_id") int appointmentId);
}
