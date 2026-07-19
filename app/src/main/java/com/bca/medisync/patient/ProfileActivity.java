package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class ProfileActivity extends AppCompatActivity {

  private TextView txtName, txtAddress, txtEmergencyContact;
  private View statAge, statBloodGroup, statGender;
  private View rowEmail, rowPhone, rowDob, rowAddress;
  private MaterialCardView cardVerifiedBadge;
  TextView txtVerifiedBadge;
  private MaterialSwitch switchNotifications;
  private SessionManager sessionManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_profile);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    sessionManager = new SessionManager(this);
    initViews();
    setupSettingsRows();
    loadPatientData();
    setupListeners();
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadPatientData();
  }

  private void initViews() {
    txtName = findViewById(R.id.txtName);
    txtAddress = findViewById(R.id.txtAddress);
    txtEmergencyContact = findViewById(R.id.txtEmergencyContact);

    statAge = findViewById(R.id.statAge);
    statBloodGroup = findViewById(R.id.statBloodGroup);
    statGender = findViewById(R.id.statGender);

    rowEmail = findViewById(R.id.rowEmail);
    rowPhone = findViewById(R.id.rowPhone);
    rowDob = findViewById(R.id.rowDob);
    rowAddress = findViewById(R.id.rowAddress);

    cardVerifiedBadge = findViewById(R.id.cardVerifiedBadge);
    txtVerifiedBadge = findViewById(R.id.txtVerifiedBadge);

    switchNotifications = findViewById(R.id.switchNotifications);
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
    ((TextView) findViewById(rowId).findViewById(R.id.txtSettingsLabel)).setText(label);
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
                if (response.isSuccessful() && response.body() != null) {
                  bindPatient(response.body());
                } else if (response.code() == 403) {
                  Toast.makeText(
                          ProfileActivity.this,
                          "Your account is pending verification",
                          Toast.LENGTH_LONG)
                      .show();
                } else {
                  Toast.makeText(
                          ProfileActivity.this, "Failed to load  your profile", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                Toast.makeText(
                        ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
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
    ShapeableImageView imgProfile = findViewById(R.id.imgProfile);
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
      txtVerifiedBadge.setTextColor(getColor(R.color.on_tertiary_container));
      cardVerifiedBadge.setCardBackgroundColor(getColor(R.color.tertiary_container));
      cardVerifiedBadge.setOnClickListener(null);
    } else {
      txtVerifiedBadge.setText("Not Verified - Tap to verify");
      txtVerifiedBadge.setTextColor(getColor(R.color.on_error_container));
      cardVerifiedBadge.setCardBackgroundColor(getColor(R.color.error_container));
      cardVerifiedBadge.setOnClickListener(
          v -> startActivity(new Intent(this, VerificationActivity.class)));
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
    findViewById(R.id.btnEditProfile).setOnClickListener(v -> {});
    findViewById(R.id.rowPrivacyPolicy).setOnClickListener(v -> {});
    findViewById(R.id.rowTerms).setOnClickListener(v -> {});
    findViewById(R.id.rowHelp).setOnClickListener(v -> {});
    findViewById(R.id.btnLogout)
        .setOnClickListener(
            v -> {
              sessionManager.clearSession();
              Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
              startActivity(intent);
            });
  }
}
