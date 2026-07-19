package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.TimeSlotResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DoctorApi {
  @GET("/api/v1/doctors/")
  Call<List<DoctorResponse>> getDoctors(
      @Query("hospital_id") Integer hospitalId,
      @Query("department") String department,
      @Query("speciality") String speciality,
      @Query("search") String search);

  @GET("/api/v1/doctors/{doctor_id}")
  Call<DoctorResponse> getDoctorDetail(@Path("doctor_id") int doctorId);

  @GET("/api/v1/doctors/{doctor_id}/timeslots")
  Call<List<TimeSlotResponse>> getDoctorTimeslots(
      @Path("doctor_id") int doctorId, @Query("available_only") boolean availableOnly);
}
