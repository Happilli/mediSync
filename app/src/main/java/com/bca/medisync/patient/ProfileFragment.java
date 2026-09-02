package com.bca.medisync.patient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.databinding.FragmentProfileBinding;
import com.bca.medisync.util.AuthUtils;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.bca.medisync.util.LoadingHelper;
import java.util.Calendar;

public class ProfileFragment extends Fragment implements NotificationCenter.Listener {

  private FragmentProfileBinding binding;

  private SessionManager sessionManager;
  private ActivityResultLauncher<String> notifPermLauncher;

  public ProfileFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentProfileBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sessionManager = new SessionManager(requireContext());

    notifPermLauncher =
        registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
              sessionManager.setNotificationsEnabled(granted);
              binding.switchNotifications.setChecked(granted);
              if (!granted) {
                Toast.makeText(
                        requireContext(),
                        "Enable notifiation permissions in system settings to get alerts.",
                        Toast.LENGTH_LONG)
                    .show();
              }
            });

    setupSettingsRows();
    setupNotificationSwitch();
    setupListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    NotificationCenter.get().register(this);
    loadPatientData();
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    if (!hidden && isAdded()) {
      loadPatientData();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    NotificationCenter.get().unregister(this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onNotificationReceived(NotificationResponse notification) {
    if (!isAdded()) return;
    if ("patient_verified".equals(notification.getType())
        || "patient_verification_rejected".equals(notification.getType())) {
      loadPatientData();
    }
  }

  private void setupSettingsRows() {
    setRowLabel(binding.rowSecurityAnswer.getRoot(), "Security Answer");
  }

  private int genderIcon(String gender) {
    switch (gender.trim().toLowerCase()) {
      case "male":
        return R.drawable.male;
      case "female":
        return R.drawable.female;
      default:
        return R.drawable.othergender;
    }
  }

  private void setRowLabel(View row, String label) {
    ((TextView) row.findViewById(R.id.txtSettingsLabel)).setText(label);
  }

  private void bindPatient(PatientResponse patient) {
    if (binding == null) return;

    binding.txtName.setText(patient.getName());
    binding.txtEmergencyContact.setText(patient.getEmergency_contact());
    InfoRowBinder.bind(
        new InfoRowBinder.Row(
            binding.rowAge.getRoot(),
            R.drawable.age,
            "Age",
            calculateAge(patient.getDate_of_birth())),
        new InfoRowBinder.Row(
            binding.rowGender.getRoot(),
            genderIcon(patient.getGender()),
            "Gender",
            patient.getGender()),
        new InfoRowBinder.Row(
            binding.rowBloodGroup.getRoot(),
            R.drawable.bloodtype,
            "Blood Group",
            patient.getBlood_group()),
        new InfoRowBinder.Row(
            binding.rowEmail.getRoot(), R.drawable.email, "Email", sessionManager.getEmail()),
        new InfoRowBinder.Row(
            binding.rowPhone.getRoot(), R.drawable.phone, "Phone", patient.getPhone()),
        new InfoRowBinder.Row(
            binding.rowDob.getRoot(),
            R.drawable.birthdate,
            "Date of Birth",
            patient.getDate_of_birth()),
        new InfoRowBinder.Row(
            binding.rowAddress.getRoot(), R.drawable.location, "Address", patient.getAddress()));
    bindVerificationBadge(
        patient.is_verified(), patient.getCitizenship_number(), patient.getRejection_reason());
    bindProfilePic(patient.getProfile_pic_url());
  }

  private void bindProfilePic(String profilePicUrl) {
    if (binding == null) return;
    ImageLoader.loadProfilePic(this, binding.imgProfile, profilePicUrl);
  }

  private void bindVerificationBadge(
      boolean isVerified, String citizenshipNumber, String rejectionReason) {
    if (isVerified) {
      binding.txtVerifiedBadge.setText("Verified");
      binding.txtVerifiedBadge.setTextColor(
          requireContext().getColor(R.color.on_tertiary_container));
      binding.cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.tertiary_container));
      binding.cardVerifiedBadge.setOnClickListener(null);
      binding.cardVerifiedBadge.setClickable(false);
    } else if (citizenshipNumber != null) {
      binding.txtVerifiedBadge.setText("Pending Review");
      binding.txtVerifiedBadge.setTextColor(
          requireContext().getColor(R.color.on_secondary_container));
      binding.cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.secondary_container));
      binding.cardVerifiedBadge.setOnClickListener(null);
      binding.cardVerifiedBadge.setClickable(false);
    } else if (rejectionReason != null) {
      binding.txtVerifiedBadge.setText("Rejected - Tap to resubmit");
      binding.txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_error_container));
      binding.cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.error_container));
      binding.cardVerifiedBadge.setClickable(true);
      binding.cardVerifiedBadge.setOnClickListener(
          v -> {
            Toast.makeText(requireContext(), rejectionReason, Toast.LENGTH_LONG).show();
            startActivity(new Intent(requireContext(), VerificationActivity.class));
          });
    } else {
      binding.txtVerifiedBadge.setText("Not Verified");
      binding.txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_error_container));
      binding.cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.error_container));
      binding.cardVerifiedBadge.setClickable(true);
      binding.cardVerifiedBadge.setOnClickListener(
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
    boolean expanding = binding.securityAnswerForm.getVisibility() != View.VISIBLE;
    binding.securityAnswerForm.setVisibility(expanding ? View.VISIBLE : View.GONE);
    if (!expanding) {
      binding.etSecurityAnswer.setText("");
      binding.etCurrentPassword.setText("");
    }
  }

  private void submitSecurityAnswer() {
    String answer =
        binding.etSecurityAnswer.getText() != null
            ? binding.etSecurityAnswer.getText().toString().trim()
            : "";
    String password =
        binding.etCurrentPassword.getText() != null
            ? binding.etCurrentPassword.getText().toString().trim()
            : "";

    if (answer.isEmpty()) {
      binding.etSecurityAnswer.setError("Answer is required");
      return;
    }
    if (password.isEmpty()) {
      binding.etCurrentPassword.setError("Password is required");
      return;
    }

    PatientApi api = ApiClient.api(PatientApi.class);
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
    LoadingHelper.show(binding.loadingIndicator);
    binding.scrollContent.setVisibility(View.GONE);

    PatientApi patientApi = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        patientApi.getMyProfile(),
        this,
        LoadingHelper.wrapSuccess(
            binding.loadingIndicator, binding.scrollContent, this::bindPatient),
        LoadingHelper.wrapError(
            binding.loadingIndicator,
            binding.scrollContent,
            (code, msg) -> {
              if (code == 403) {
                Toast.makeText(
                        requireContext(), "Your account is pending verification", Toast.LENGTH_LONG)
                    .show();
              } else {
                ApiCallback.simpleError(requireContext(), "Failed to load your profile")
                    .run(code, msg);
              }
            }));
  }

  public void setupNotificationSwitch() {
    binding.switchNotifications.setChecked(sessionManager.isNotificationsEnabled());
    binding.switchNotifications.setOnCheckedChangeListener(
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
    binding.btnEditProfile.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
    binding.rowSecurityAnswer.getRoot().setOnClickListener(v -> toggleSecurityAnswerForm());
    binding.btnSaveSecurityAnswer.setOnClickListener(v -> submitSecurityAnswer());
    binding.btnLogout.setOnClickListener(
        v -> AuthUtils.logout((androidx.appcompat.app.AppCompatActivity) requireActivity()));
  }
}
