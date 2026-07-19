package com.bca.medisync.patient;

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
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

  private TextView txtName, txtAddress, txtEmergencyContact;
  private View statAge, statBloodGroup, statGender;
  private View rowEmail, rowPhone, rowDob, rowAddress;
  private MaterialCardView cardVerifiedBadge;
  private TextView txtVerifiedBadge;
  private MaterialSwitch switchNotifications;
  private SessionManager sessionManager;

  public ProfileFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_profile, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sessionManager = new SessionManager(requireContext());
    initViews(view);
    setupSettingsRows();
    setupListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadPatientData();
  }

  private void initViews(View view) {
    txtName = view.findViewById(R.id.txtName);
    txtAddress = view.findViewById(R.id.txtAddress);
    txtEmergencyContact = view.findViewById(R.id.txtEmergencyContact);

    statAge = view.findViewById(R.id.statAge);
    statBloodGroup = view.findViewById(R.id.statBloodGroup);
    statGender = view.findViewById(R.id.statGender);

    rowEmail = view.findViewById(R.id.rowEmail);
    rowPhone = view.findViewById(R.id.rowPhone);
    rowDob = view.findViewById(R.id.rowDob);
    rowAddress = view.findViewById(R.id.rowAddress);

    cardVerifiedBadge = view.findViewById(R.id.cardVerifiedBadge);
    txtVerifiedBadge = view.findViewById(R.id.txtVerifiedBadge);

    switchNotifications = view.findViewById(R.id.switchNotifications);
  }

  private void bindStat(View statView, String value, String label) {
    ((TextView) statView.findViewById(R.id.txtStatValue)).setText(value);
    ((TextView) statView.findViewById(R.id.txtStatLabel)).setText(label);
  }

  private void bindRow(View rowView, int iconRes, String label, String value) {
    ((ImageView) rowView.findViewById(R.id.imgRowIcon)).setImageResource(iconRes);
    ((TextView) rowView.findViewById(R.id.txtRowLabel)).setText(label);
    ((TextView) rowView.findViewById(R.id.txtRowValue)).setText(value);
  }

  private void setupSettingsRows() {
    setRowLabel(R.id.rowPrivacyPolicy, "Privacy Policy");
    setRowLabel(R.id.rowTerms, "Terms and Conditions");
    setRowLabel(R.id.rowHelp, "Help and Support");
  }

  private void setRowLabel(int rowId, String label) {
    ((TextView) requireView().findViewById(rowId).findViewById(R.id.txtSettingsLabel))
        .setText(label);
  }

  private void loadPatientData() {
    PatientApi patientApi = ApiClient.getRetrofit().create(PatientApi.class);
    patientApi
        .getMyProfile()
        .enqueue(
            new Callback<PatientResponse>() {
              @Override
              public void onResponse(
                  Call<PatientResponse> call, Response<PatientResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  bindPatient(response.body());
                } else if (response.code() == 403) {
                  Toast.makeText(
                          requireContext(),
                          "Your account is pending verification",
                          Toast.LENGTH_LONG)
                      .show();
                } else {
                  Toast.makeText(
                          requireContext(), "Failed to load  your profile", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindPatient(PatientResponse patient) {
    txtName.setText(patient.getName());
    txtAddress.setText(patient.getAddress());
    txtEmergencyContact.setText(patient.getEmergency_contact());

    bindStat(statAge, calculateAge(patient.getDate_of_birth()), "Age");
    bindStat(statBloodGroup, patient.getBlood_group(), "Blood");
    bindStat(statGender, patient.getGender(), "Gender");

    bindRow(rowEmail, R.drawable.email, "Email", sessionManager.getEmail());
    bindRow(rowPhone, R.drawable.phone, "Phone", patient.getPhone());
    bindRow(rowDob, R.drawable.birthdate, "Date of Birth", patient.getDate_of_birth());
    bindRow(rowAddress, R.drawable.location, "Address", patient.getAddress());

    bindVerificationBadge(patient.isIs_verified());
    bindProfilePic(patient.getProfile_pic_url());
  }

  private void bindProfilePic(String profilePicUrl) {
    ShapeableImageView imgProfile = requireView().findViewById(R.id.imgProfile);
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

  private void bindVerificationBadge(boolean isVerified) {
    if (isVerified) {
      txtVerifiedBadge.setText("Verified");
      txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_tertiary_container));
      cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.tertiary_container));
      cardVerifiedBadge.setOnClickListener(null);
    } else {
      txtVerifiedBadge.setText("Not Verified - Tap to verify");
      txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_error_container));
      cardVerifiedBadge.setCardBackgroundColor(requireContext().getColor(R.color.error_container));
      cardVerifiedBadge.setOnClickListener(
          v -> startActivity(new Intent(requireContext(), VerificationActivity.class)));
    }
  }

  private String calculateAge(String dob) {
    try {
      String[] parts = dob.split("-");
      int birthYear = Integer.parseInt(parts[0]);
      int currentYear = Calendar.getInstance().get(Calendar.YEAR);
      return String.valueOf(currentYear - birthYear);
    } catch (Exception e) {
      return "--";
    }
  }

  private void setupListeners() {
    requireView().findViewById(R.id.btnEditProfile).setOnClickListener(v -> {});
    requireView().findViewById(R.id.rowPrivacyPolicy).setOnClickListener(v -> {});
    requireView().findViewById(R.id.rowTerms).setOnClickListener(v -> {});
    requireView().findViewById(R.id.rowHelp).setOnClickListener(v -> {});
    requireView()
        .findViewById(R.id.btnLogout)
        .setOnClickListener(
            v -> {
              sessionManager.clearSession();
              Intent intent = new Intent(requireContext(), MainActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
              startActivity(intent);
              requireActivity().finish();
            });
  }
}
