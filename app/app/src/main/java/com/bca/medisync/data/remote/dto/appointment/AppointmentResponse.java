package com.bca.medisync.data.remote.dto.appointment;

import android.util.Log;

import com.bca.medisync.data.model.Appointment;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AppointmentResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("doctor_id")
    private int doctorId;

    @SerializedName("patient_id")
    private int patientId;

    @SerializedName("hospital_id")
    private int hospitalId;

    @SerializedName("doctor_name")
    private String doctorName;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("patient_phone")
    private String patientPhone;

    @SerializedName("patient_gender")
    private String patientGender;

    @SerializedName("patient_blood_group")
    private String patientBloodGroup;

    @SerializedName("patient_profile_pic_url")
    private String patientProfilePicUrl;

    @SerializedName("department")
    private String department;

    @SerializedName("speciality")
    private String speciality;

    @SerializedName("appointment_at")
    private String appointmentAt;

    @SerializedName("status")
    private String status;

    @SerializedName("notes")
    private String notes;

    public int getId() {
        return id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public String getPatientBloodGroup() {
        return patientBloodGroup;
    }

    public String getPatientProfilePicUrl() {
        return patientProfilePicUrl;
    }

    public String getDepartment() {
        return department;
    }

    public String getSpeciality() {
        return speciality;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes == null ? "" : notes;
    }

    public Appointment toAppointment() {
        return new Appointment(
                String.valueOf(id),
                patientName,
                patientPhone,
                patientGender,
                patientBloodGroup,
                patientProfilePicUrl,
                doctorName,
                department,
                speciality,
                getAppointmentDate(),
                getAppointmentTime(),
                capitalize(status),
                notes == null ? "" : notes
        );
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty())
            return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public String getAppointmentDate() {
        Log.d("TIME", "appointmentAt: " + appointmentAt);
        if (appointmentAt == null || appointmentAt.isEmpty()) return "";
        try {
            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            input.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = input.parse(appointmentAt);

            SimpleDateFormat output =
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            output.setTimeZone(TimeZone.getTimeZone("UTC"));

            return output.format(date);

        } catch (ParseException e) {
            try {
                SimpleDateFormat inputBackup =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                inputBackup.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = inputBackup.parse(appointmentAt);
                SimpleDateFormat output =
                        new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                output.setTimeZone(TimeZone.getTimeZone("UTC"));
                return output.format(date);
            } catch (ParseException e2) {
                return "";
            }
        }
    }

    public String getAppointmentTime() {
        if (appointmentAt == null || appointmentAt.isEmpty()) return "";
        try {
            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            input.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = input.parse(appointmentAt);

            SimpleDateFormat output =
                    new SimpleDateFormat("hh:mm a", Locale.getDefault());
            output.setTimeZone(TimeZone.getTimeZone("UTC"));

            return output.format(date);

        } catch (ParseException e) {
            try {
                SimpleDateFormat inputBackup =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                inputBackup.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = inputBackup.parse(appointmentAt);
                SimpleDateFormat output =
                        new SimpleDateFormat("hh:mm a", Locale.getDefault());
                output.setTimeZone(TimeZone.getTimeZone("UTC"));
                return output.format(date);
            } catch (ParseException e2) {
                return "";
            }
        }
    }
}
