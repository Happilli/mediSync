package com.bca.medisync.data.remote.dto.prescription;

import com.bca.medisync.data.remote.dto.medication.MedicationCreateRequest;
import java.util.List;

public record PrescriptionCreateRequest(
    int appointment_id,
    String diagnosis,
    String instructions,
    String follow_up_date,
    List<MedicationCreateRequest> medications) {}
