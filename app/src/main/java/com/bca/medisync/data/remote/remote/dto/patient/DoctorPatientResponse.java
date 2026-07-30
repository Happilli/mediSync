package com.bca.medisync.data.remote.remote.dto.patient;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DoctorPatientResponse implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("gender")
    private String gender;

    @SerializedName("blood_group")
    private String blood_group;

    @SerializedName("emergency_contact")
    private String emergency_contact;

    @SerializedName("profile_pic_url")
    private String profile_pic_url;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getBlood_group() {
        return blood_group;
    }

    public String getEmergency_contact() {
        return emergency_contact;
    }

    public String getProfile_pic_url() {
        return profile_pic_url;
    }
}