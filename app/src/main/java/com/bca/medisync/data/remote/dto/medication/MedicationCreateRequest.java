package com.bca.medisync.data.remote.dto.medication;

import java.util.List;

public record MedicationCreateRequest(
    String name,
    String dosage,
    String instruction,
    int duration_days,
    List<MedicationTimeCreateRequest> dosage_times) {}
