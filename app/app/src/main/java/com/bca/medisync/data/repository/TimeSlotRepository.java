package com.bca.medisync.data.repository;

import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.TimeSlotApi;
import com.bca.medisync.data.remote.dto.timeslot.TimeSlotRequest;
import com.bca.medisync.data.remote.dto.timeslot.TimeSlotResponse;

import java.util.List;

import retrofit2.Call;

public class TimeSlotRepository {

    private final TimeSlotApi api;

    public TimeSlotRepository() {
        api = ApiClient.getRetrofit().create(TimeSlotApi.class);
    }

    public Call<TimeSlotResponse> createTimeSlot(String appointmentAt) {
        return api.createTimeSlot(new TimeSlotRequest(appointmentAt));
    }

    public Call<List<TimeSlotResponse>> getDoctorTimeSlots(int doctorId) {
        return api.getDoctorTimeSlots(doctorId);
    }

    public Call<Void> deleteTimeSlot(int timeslotId) {
        return api.deleteTimeSlot(timeslotId);
    }

    public Call<TimeSlotResponse> updateTimeSlot(int timeslotId, String appointmentAt) {
        return api.updateTimeSlot(timeslotId, new TimeSlotRequest(appointmentAt));
    }
}