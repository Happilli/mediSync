package com.bca.medisync.data.remote.helpers;

import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.data.remote.dto.patient.PatientPublicResponse;
import com.bca.medisync.util.DateTimeUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentEnricher {
  private static final ThreadLocal<SimpleDateFormat> DATE_FMT =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()));
  private static final ThreadLocal<SimpleDateFormat> TIME_FMT =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("hh:mm a", Locale.getDefault()));

  public static void enrichAll(
      List<AppointmentResponse> responses, ParallelEnricher.Callback1<List<Appointment>> callback) {
    DoctorApi doctorApi = ApiClient.api(DoctorApi.class);
    ParallelEnricher.run(
        responses,
        r -> doctorApi.getDoctorDetail(r.doctor_id()),
        AppointmentEnricher::mapToAppointment,
        callback);
  }

  public static void enrichOne(
      AppointmentResponse r, ParallelEnricher.Callback1<Appointment> callback) {
    DoctorApi doctorApi = ApiClient.api(DoctorApi.class);
    ParallelEnricher.run(
        Collections.singletonList(r),
        rr -> doctorApi.getDoctorDetail(rr.doctor_id()),
        AppointmentEnricher::mapToAppointment,
        list -> callback.onResult(list.get(0)));
  }

  public static void enrichForDoctor(
      List<AppointmentResponse> responses, ParallelEnricher.Callback1<List<Appointment>> callback) {
    if (responses.isEmpty()) {
      callback.onResult(new ArrayList<>());
      return;
    }
    if (responses.get(0).patient_name() != null) {
      List<Appointment> result = new ArrayList<>();
      for (AppointmentResponse r : responses) result.add(mapToAppointmentForDoctor(r, null));
      callback.onResult(result);
      return;
    }
    PatientApi patientApi = ApiClient.api(PatientApi.class);
    ParallelEnricher.run(
        responses,
        r -> patientApi.getPatientDetailForDoctor(r.patient_id()),
        AppointmentEnricher::mapToAppointmentForDoctor,
        callback);
  }

  private static Appointment build(
      AppointmentResponse r,
      String patientName,
      String doctorName,
      String department,
      String speciality) {
    Date date = parseIso(r.appointment_at());
    String dateStr = date != null ? DATE_FMT.get().format(date) : "";
    String timeStr = date != null ? TIME_FMT.get().format(date) : "";
    return new Appointment(
        String.valueOf(r.id()),
        patientName,
        doctorName,
        department,
        speciality,
        dateStr,
        timeStr,
        capitalize(r.status()),
        r.notes(),
        r.patient_id());
  }

  public static Appointment mapToAppointment(AppointmentResponse r, DoctorResponse d) {
    String doctorName = d != null ? d.name() : "Doctor #" + r.doctor_id();
    String speciality = d != null ? d.speciality() : "";
    String department = d != null ? d.department() : "";
    return build(r, "", doctorName, department, speciality);
  }

  public static Appointment mapToAppointmentForDoctor(
      AppointmentResponse r, PatientPublicResponse p) {
    String patientName = r.patient_name();
    if (patientName == null || patientName.isEmpty()) {
      patientName = p != null ? p.name() : "Patient #" + r.patient_id();
    }
    return build(
        r,
        patientName,
        r.doctor_name() != null ? r.doctor_name() : "",
        r.department() != null ? r.department() : "",
        r.speciality() != null ? r.speciality() : "");
  }

  public static Date parseIso(String iso) {
    return DateTimeUtils.parseIsoToDate(iso);
  }

  public static String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }

  public static AppointmentResponse findNextUpcoming(List<AppointmentResponse> all) {
    AppointmentResponse best = null;
    for (AppointmentResponse a : all) {
      String status = a.status();
      if (status == null) continue;
      if (!status.equalsIgnoreCase("confirmed") && !status.equalsIgnoreCase("pending")) continue;
      if (best == null || compareIso(a.appointment_at(), best.appointment_at()) < 0) best = a;
    }
    return best;
  }

  private static int compareIso(String a, String b) {
    if (a == null) return 1;
    if (b == null) return -1;
    return a.compareTo(b);
  }
}
