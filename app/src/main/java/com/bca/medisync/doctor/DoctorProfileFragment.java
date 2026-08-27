package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationSocketHolder;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.remote.dto.hospital.HospitalResponse;
import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorProfileFragment extends Fragment {

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
              NotificationSocketHolder.get().disconnect();
              NotificationSocketHolder.reset();
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

    ((TextView) view.findViewById(R.id.txtDoctorName)).setText("Dr. " + p.getName());
    ((TextView) view.findViewById(R.id.txtRole)).setText(p.getSpeciality());

    TextView badge = view.findViewById(R.id.txtRegistrationBadge);
    if (p.isIs_verified()) {
      badge.setText("Verified");
      badge.setTextColor(requireContext().getColor(R.color.on_tertiary_container));
      badge.setBackgroundColor(requireContext().getColor(R.color.tertiary_container));
    } else {
      badge.setText("Pending Verification");
      badge.setTextColor(requireContext().getColor(R.color.on_error_container));
      badge.setBackgroundColor(requireContext().getColor(R.color.error_container));
    }

    TextView txtBio = view.findViewById(R.id.txtBio);
    if (p.getBio() != null && !p.getBio().trim().isEmpty()) {
      txtBio.setVisibility(View.VISIBLE);
      txtBio.setText(p.getBio());
    } else {
      txtBio.setVisibility(View.GONE);
    }

    bindProfilePic(p.getProfile_pic_url());

    ((TextView) view.findViewById(R.id.statPatientsMonthValue))
        .setText(String.valueOf(p.getPatients_this_month()));
    ((TextView) view.findViewById(R.id.statPatientsTotalValue))
        .setText(String.valueOf(p.getTotal_patients()));

    ((TextView) view.findViewById(R.id.txtSpecializationValue)).setText(p.getSpeciality());
    ((TextView) view.findViewById(R.id.txtExperienceValue))
        .setText(years > 0 ? years + " years" : "Not specified");
    ((TextView) view.findViewById(R.id.txtPhoneValue)).setText(p.getPhone());
    ((TextView) view.findViewById(R.id.txtEmailValue)).setText(sessionManager.getEmail());
    ((TextView) view.findViewById(R.id.txtAddressValue)).setText(p.getAddress());
  }

  private void bindProfilePic(String profilePicUrl) {
    View view = getView();
    if (view == null) return;
    ImageView imgProfile = view.findViewById(R.id.imgDoctorProfile);
    if (profilePicUrl == null || profilePicUrl.isEmpty()) {
      imgProfile.setImageResource(R.drawable.ic_nav_profile);
      return;
    }
    Glide.with(this)
        .load(ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + profilePicUrl)
        .placeholder(R.drawable.ic_nav_profile)
        .error(R.drawable.ic_nav_profile)
        .centerCrop()
        .into(imgProfile);
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
                ((TextView) view.findViewById(R.id.txtHospitalValue)).setText(name);
              }

              @Override
              public void onFailure(Call<HospitalResponse> call, Throwable t) {}
            });
  }
}
