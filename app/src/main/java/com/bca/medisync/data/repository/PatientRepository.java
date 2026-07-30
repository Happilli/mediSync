package com.bca.medisync.data.repository;

import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.DoctorPatientResponse;

import java.util.List;

import retrofit2.Callback;

public class PatientRepository {

    private final PatientApi patientApi;

    public PatientRepository() {
        this.patientApi = ApiClient.getRetrofit().create(PatientApi.class);
    }

    public void getDoctorPatients(Callback<List<DoctorPatientResponse>> callback) {
        patientApi.getDoctorPatients().enqueue(callback);
    }
}
