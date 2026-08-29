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
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationSocketHolder;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.util.RoundedListStyler;
import com.bumptech.glide.Glide;

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
    ApiCallback.handle(
        api.getMyProfile(),
        this,
        profile -> {
          bindProfile(profile);
          loadHospitalName(profile.getHospital_id());
        },
        (code, msg) -> {
          if (code == -1) {
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void loadHospitalName(int hospitalId) {
    HospitalApi api = ApiClient.getRetrofit().create(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitalDetail(hospitalId),
        this,
        h -> {
          View view = getView();
          if (view == null) return;
          setRowValue(view.findViewById(R.id.rowHospital), h.getName());
        },
        (code, msg) -> {
          View view = getView();
          if (view == null) return;
          setRowValue(view.findViewById(R.id.rowHospital), "Hospital #" + hospitalId);
        });
  }

  private void bindRow(View rowView, int iconRes, String label, String value) {
    ((ImageView) rowView.findViewById(R.id.imgRowIcon)).setImageResource(iconRes);
    ((TextView) rowView.findViewById(R.id.txtRowLabel)).setText(label);
    ((TextView) rowView.findViewById(R.id.txtRowValue)).setText(value);
  }

  private void setRowValue(View rowView, String value) {
    ((TextView) rowView.findViewById(R.id.txtRowValue)).setText(value);
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

    View rowSpecialization = view.findViewById(R.id.rowSpecialization);
    View rowHospital = view.findViewById(R.id.rowHospital);
    View rowExperience = view.findViewById(R.id.rowExperience);
    View rowPhone = view.findViewById(R.id.rowPhone);
    View rowEmail = view.findViewById(R.id.rowEmail);
    View rowAddress = view.findViewById(R.id.rowAddress);

    bindRow(rowSpecialization, R.drawable.stethoscope, "Specialization", p.getSpeciality());
    bindRow(rowHospital, R.drawable.hospital, "Hospital", "Hospital #" + p.getHospital_id());
    bindRow(
        rowExperience,
        R.drawable.ic_nav_calendar,
        "Experience",
        years > 0 ? years + " years" : "Not specified");
    bindRow(rowPhone, R.drawable.phone, "Phone", p.getPhone());
    bindRow(rowEmail, R.drawable.email, "Email", sessionManager.getEmail());
    bindRow(rowAddress, R.drawable.location, "Address", p.getAddress());

    View[] rows = {rowSpecialization, rowHospital, rowExperience, rowPhone, rowEmail, rowAddress};
    for (int i = 0; i < rows.length; i++) {
      RoundedListStyler.apply(rows[i], i, rows.length);
    }
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
}
