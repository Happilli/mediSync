package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.remote.dto.hospital.HospitalResponse;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorProfileFragment extends Fragment {

  private LinearLayout availabilityContainer;
  private TextView lblAvailability;
  private SessionManager sessionManager;

  public DoctorProfileFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_doctor_profile, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sessionManager = new SessionManager(requireContext());

    availabilityContainer = view.findViewById(R.id.availabilityContainer);
    lblAvailability = view.findViewById(R.id.lblAvailability);
    lblAvailability.setVisibility(View.GONE);
    availabilityContainer.setVisibility(View.GONE);

    setupListeners(view);
    loadProfile();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadProfile();
  }

  private void setupListeners(View view) {
    view.findViewById(R.id.btnLogoutDoctor)
        .setOnClickListener(
            v -> {
              sessionManager.clearSession();
              Intent intent = new Intent(requireContext(), MainActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
              startActivity(intent);
              requireActivity().finish();
            });
  }

  private void loadProfile() {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.getMyProfile()
        .enqueue(
            new Callback<DoctorProfileResponse>() {
              @Override
              public void onResponse(
                  Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  DoctorProfileResponse profile = response.body();
                  bindProfile(profile);
                  loadHospitalName(profile.getHospital_id());
                } else {
                  Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindProfile(DoctorProfileResponse p) {
    View view = getView();
    if (view == null) return;

    int years = p.getYears_experience() != null ? p.getYears_experience() : 0;

    ((TextView) view.findViewById(R.id.txtDoctorName)).setText(p.getName());
    ((TextView) view.findViewById(R.id.txtRole)).setText(p.getSpeciality());
    ((TextView) view.findViewById(R.id.txtRegistrationBadge))
        .setText(p.isIs_verified() ? "Verified" : "Pending");
    ((TextView) view.findViewById(R.id.txtQualification)).setText(p.getDepartment());
    ((TextView) view.findViewById(R.id.txtExperience)).setText(years + " Years Experience");
    ((TextView) view.findViewById(R.id.txtPhoneHeader)).setText(p.getPhone());
    ((TextView) view.findViewById(R.id.txtEmailHeader)).setText(sessionManager.getEmail());
    ((TextView) view.findViewById(R.id.txtHospitalRole)).setText(p.getDepartment());

    setInfoCard(view, R.id.cardSpecialization, "SPECIALIZATION", p.getSpeciality());
    setInfoCard(view, R.id.cardExperience, "EXPERIENCE", years + " yrs");
    setInfoCard(view, R.id.cardQualification, "BIO", p.getBio() != null ? p.getBio() : "—");
    setInfoCard(view, R.id.cardRegistration, "ADDRESS", p.getAddress());
    setInfoCard(view, R.id.cardEmail, "EMAIL", sessionManager.getEmail());
    setInfoCard(view, R.id.cardPhone, "PHONE", p.getPhone());

    bindStatistics(view, p);
  }

  private void setInfoCard(View root, int cardId, String title, String value) {
    MaterialCardView card = root.findViewById(cardId);
    ((TextView) card.findViewById(R.id.lblTitle)).setText(title);
    ((TextView) card.findViewById(R.id.lblValue)).setText(value);
  }

  private void bindStatistics(View view, DoctorProfileResponse p) {
    setStat(view, R.id.statPatientsMonth, p.getPatients_this_month() + "+", "patients this month");
    setStat(view, R.id.statPatientsTotal, p.getTotal_patients() + "+", "Patients Total");

    // no feedback %/rating source exists yet — hide rather than fabricate
    view.findViewById(R.id.statFeedback).setVisibility(View.GONE);
    view.findViewById(R.id.statRating).setVisibility(View.GONE);
  }

  private void setStat(View root, int cardId, String value, String label) {
    MaterialCardView card = root.findViewById(cardId);
    ((TextView) card.findViewById(R.id.statValue)).setText(value);
    ((TextView) card.findViewById(R.id.statLabel)).setText(label);
  }

  private void loadHospitalName(int hospitalId) {
    HospitalApi api = ApiClient.getRetrofit().create(HospitalApi.class);
    api.getHospitalDetail(hospitalId)
        .enqueue(
            new Callback<HospitalResponse>() {
              @Override
              public void onResponse(
                  Call<HospitalResponse> call, Response<HospitalResponse> response) {
                if (!isAdded()) return;
                View view = getView();
                if (view == null) return;
                String name =
                    response.isSuccessful() && response.body() != null
                        ? response.body().getName()
                        : "Hospital #" + hospitalId;
                ((TextView) view.findViewById(R.id.txtHospitalName)).setText(name);
              }

              @Override
              public void onFailure(Call<HospitalResponse> call, Throwable t) {}
            });
  }
}
