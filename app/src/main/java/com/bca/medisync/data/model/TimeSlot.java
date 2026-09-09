package com.bca.medisync.data.model;

public record TimeSlot(int id, String appointmentAt, String displayTime, boolean available) {}
