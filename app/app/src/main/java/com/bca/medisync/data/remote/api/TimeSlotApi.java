package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.timeslot.TimeSlotRequest;
import com.bca.medisync.data.remote.dto.timeslot.TimeSlotResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TimeSlotApi {

    @POST("/api/v1/doctors/me/timeslots")
    Call<TimeSlotResponse> createTimeSlot(
            @Body TimeSlotRequest request
    );

    @GET("/api/v1/doctors/{doctor_id}/timeslots")
    Call<List<TimeSlotResponse>> getDoctorTimeSlots(
            @Path("doctor_id") int doctorId
    );

    @DELETE("/api/v1/doctors/me/timeslots/{timeslot_id}")
    Call<Void> deleteTimeSlot(
            @Path("timeslot_id") int timeslotId
    );

    @PATCH("/api/v1/doctors/me/timeslots/{timeslot_id}")
    Call<TimeSlotResponse> updateTimeSlot(
            @Path("timeslot_id") int timeslotId,
            @Body TimeSlotRequest request
    );
}