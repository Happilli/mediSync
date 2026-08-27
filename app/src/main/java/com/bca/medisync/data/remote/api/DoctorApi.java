package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.TimeSlotResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorUpdateRequest;
import com.bca.medisync.data.remote.dto.doctor.TimeSlotCreateRequest;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DoctorApi {
  @GET("/api/v1/doctors/")
  Call<List<DoctorResponse>> getDoctors(
      @Query("hospital_id") Integer hospitalId,
      @Query("department") String department,
      @Query("speciality") String speciality,
      @Query("search") String search);

  @GET("/api/v1/doctors/me")
  Call<DoctorProfileResponse> getMyProfile();

  @PATCH("/api/v1/doctors/me")
  Call<DoctorProfileResponse> updateMyProfile(@Body DoctorUpdateRequest request);

  @Multipart
  @PATCH("/api/v1/doctors/me/profile-pic")
  Call<DoctorProfileResponse> updateProfilePic(@Part MultipartBody.Part file);

  @GET("/api/v1/doctors/{doctor_id}")
  Call<DoctorResponse> getDoctorDetail(@Path("doctor_id") int doctorId);

  @GET("/api/v1/doctors/{doctor_id}/timeslots")
  Call<List<TimeSlotResponse>> getDoctorTimeslots(
      @Path("doctor_id") int doctorId, @Query("available_only") boolean availableOnly);

  @POST("/api/v1/doctors/me/timeslots")
  Call<TimeSlotResponse> addTimeSlot(@Body TimeSlotCreateRequest request);

  @PATCH("/api/v1/doctors/me/timeslots/{timeslot_id}")
  Call<TimeSlotResponse> updateTimeSlot(
      @Path("timeslot_id") int timeslotId, @Body TimeSlotCreateRequest request);

  @DELETE("/api/v1/doctors/me/timeslots/{timeslot_id}")
  Call<Void> deleteTimeSlot(@Path("timeslot_id") int timeslotId);
}
