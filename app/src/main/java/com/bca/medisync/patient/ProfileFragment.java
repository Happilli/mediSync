package com.bca.medisync.patient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationSocketHolder;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.RoundedListStyler;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

public class ProfileFragment extends Fragment {

  private TextView txtName, txtEmergencyContact;
  private View rowAge, rowGender, rowBloodGroup, rowEmail, rowPhone, rowDob, rowAddress;
  private MaterialCardView cardVerifiedBadge;
  private TextView txtVerifiedBadge;
  private MaterialSwitch switchNotifications;
  private SessionManager sessionManager;

  private View securityAnswerForm;
  private TextInputEditText etSecurityAnswer, etCurrentPassword;

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
    setupNotificationSwitch();
    setupListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadPatientData();
  }

  private void initViews(View view) {
    txtName = view.findViewById(R.id.txtName);
    txtEmergencyContact = view.findViewById(R.id.txtEmergencyContact);

    rowAge = view.findViewById(R.id.rowAge);
    rowGender = view.findViewById(R.id.rowGender);
    rowBloodGroup = view.findViewById(R.id.rowBloodGroup);
    rowEmail = view.findViewById(R.id.rowEmail);
    rowPhone = view.findViewById(R.id.rowPhone);
    rowDob = view.findViewById(R.id.rowDob);
    rowAddress = view.findViewById(R.id.rowAddress);

    cardVerifiedBadge = view.findViewById(R.id.cardVerifiedBadge);
    txtVerifiedBadge = view.findViewById(R.id.txtVerifiedBadge);

    switchNotifications = view.findViewById(R.id.switchNotifications);

    securityAnswerForm = view.findViewById(R.id.securityAnswerForm);
    etSecurityAnswer = view.findViewById(R.id.etSecurityAnswer);
    etCurrentPassword = view.findViewById(R.id.etCurrentPassword);

    notifPermLauncher =
        registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
              sessionManager.setNotificationsEnabled(granted);
              switchNotifications.setChecked(granted);
              if (!granted) {
                Toast.makeText(
                        requireContext(),
                        "Enable notifiation permissions in system settings to get alerts.",
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private ActivityResultLauncher<String> notifPermLauncher;

  private void bindRow(View rowView, int iconRes, String label, String value) {
    ((ImageView) rowView.findViewById(R.id.imgRowIcon)).setImageResource(iconRes);
    ((TextView) rowView.findViewById(R.id.txtRowLabel)).setText(label);
    ((TextView) rowView.findViewById(R.id.txtRowValue)).setText(value);
  }

  private void setupSettingsRows() {
    setRowLabel(R.id.rowSecurityAnswer, "Security Answer");
  }

  private void applyRoundedRows() {
    View[] rows = {rowAge, rowGender, rowBloodGroup, rowEmail, rowPhone, rowDob, rowAddress};
    for (int i = 0; i < rows.length; i++) {
      RoundedListStyler.apply(rows[i], i, rows.length);
    }
  }

  private void setRowLabel(int rowId, String label) {
    ((TextView) requireView().findViewById(rowId).findViewById(R.id.txtSettingsLabel))
        .setText(label);
  }

  private void bindPatient(PatientResponse patient) {
    txtName.setText(patient.getName());
    txtEmergencyContact.setText(patient.getEmergency_contact());

    bindRow(rowAge, R.drawable.birthdate, "Age", calculateAge(patient.getDate_of_birth()));
    bindRow(rowGender, R.drawable.stethoscope, "Gender", patient.getGender());
    bindRow(rowBloodGroup, R.drawable.stethoscope, "Blood Group", patient.getBlood_group());
    bindRow(rowEmail, R.drawable.email, "Email", sessionManager.getEmail());
    bindRow(rowPhone, R.drawable.phone, "Phone", patient.getPhone());
    bindRow(rowDob, R.drawable.birthdate, "Date of Birth", patient.getDate_of_birth());
    bindRow(rowAddress, R.drawable.location, "Address", patient.getAddress());

    applyRoundedRows();
    bindVerificationBadge(patient.isIs_verified());
    bindProfilePic(patient.getProfile_pic_url());
  }

  private void bindProfilePic(String profilePicUrl) {
    ShapeableImageView imgProfile = requireView().findViewById(R.id.imgProfile);
    ImageLoader.loadProfilePic(this, imgProfile, profilePicUrl);
  }

  private void bindVerificationBadge(boolean isVerified) {
    if (isVerified) {
      txtVerifiedBadge.setText("Verified");
      txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_tertiary_container));
      cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.tertiary_container));
      cardVerifiedBadge.setOnClickListener(null);
    } else {
      txtVerifiedBadge.setText("Not Verified");
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

  private void toggleSecurityAnswerForm() {
    boolean expanding = securityAnswerForm.getVisibility() != View.VISIBLE;
    securityAnswerForm.setVisibility(expanding ? View.VISIBLE : View.GONE);
    if (!expanding) {
      etSecurityAnswer.setText("");
      etCurrentPassword.setText("");
    }
  }

  private void submitSecurityAnswer() {
    String answer =
        etSecurityAnswer.getText() != null ? etSecurityAnswer.getText().toString().trim() : "";
    String password =
        etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";

    if (answer.isEmpty()) {
      etSecurityAnswer.setError("Answer is required");
      return;
    }
    if (password.isEmpty()) {
      etCurrentPassword.setError("Password is required");
      return;
    }

    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    ApiCallback.handle(
        api.updateSecurityAnswer(new PatientSecurityAnswerUpdateRequest(password, answer)),
        this,
        body -> {
          Toast.makeText(requireContext(), "Security answer updated.", Toast.LENGTH_SHORT).show();
          toggleSecurityAnswerForm();
        },
        (code, msg) -> {
          if (code == 401) {
            Toast.makeText(requireContext(), "Current password is incorrect.", Toast.LENGTH_SHORT)
                .show();
          } else if (code == -1) {
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(
                    requireContext(), "Failed to update security answer.", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void loadPatientData() {
    PatientApi patientApi = ApiClient.getRetrofit().create(PatientApi.class);
    ApiCallback.handle(
        patientApi.getMyProfile(),
        this,
        this::bindPatient,
        (code, msg) -> {
          if (code == 403) {
            Toast.makeText(
                    requireContext(), "Your account is pending verification", Toast.LENGTH_LONG)
                .show();
          } else {
            ApiCallback.simpleError(requireContext(), "Failed to load your profile").run(code, msg);
          }
        });
  }

  public void setupNotificationSwitch() {
    switchNotifications.setChecked(sessionManager.isNotificationsEnabled());
    switchNotifications.setOnCheckedChangeListener(
        (btn, isChecked) -> {
          if (!isChecked) {
            sessionManager.setNotificationsEnabled(false);
            return;
          }
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
              && ContextCompat.checkSelfPermission(
                      requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                  != PackageManager.PERMISSION_GRANTED) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
          } else {
            sessionManager.setNotificationsEnabled(true);
          }
        });
  }

  private void setupListeners() {
    requireView()
        .findViewById(R.id.btnEditProfile)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
    requireView()
        .findViewById(R.id.rowSecurityAnswer)
        .setOnClickListener(v -> toggleSecurityAnswerForm());
    requireView()
        .findViewById(R.id.btnSaveSecurityAnswer)
        .setOnClickListener(v -> submitSecurityAnswer());
    requireView()
        .findViewById(R.id.btnLogout)
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
}
