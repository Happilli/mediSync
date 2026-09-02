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
import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.NotificationSocketHolder;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.bca.medisync.util.LoadingHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

public class ProfileFragment extends Fragment implements NotificationCenter.Listener {

  private TextView txtName, txtEmergencyContact;
  private View rowAge, rowGender, rowBloodGroup, rowEmail, rowPhone, rowDob, rowAddress;
  private LoadingIndicator loadingIndicator;
  private MaterialCardView cardVerifiedBadge;
  private TextView txtVerifiedBadge;
  private MaterialSwitch switchNotifications;
  private SessionManager sessionManager;
  private View scrollContent;

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
  public void onNotificationReceived(NotificationResponse notification) {
    if (!isAdded()) return;
    if ("patient_verified".equals(notification.getType())
        || "patient_verification_rejected".equals(notification.getType())) {
      loadPatientData();
    }
  }

  private void initViews(View view) {
    txtName = view.findViewById(R.id.txtName);
    txtEmergencyContact = view.findViewById(R.id.txtEmergencyContact);

    rowAge = view.findViewById(R.id.rowAge);
    rowGender = view.findViewById(R.id.rowGender);
    rowBloodGroup = view.findViewById(R.id.rowBloodGroup);
    rowEmail = view.findViewById(R.id.rowEmail);
    scrollContent = view.findViewById(R.id.scrollContent);
    rowPhone = view.findViewById(R.id.rowPhone);
    rowDob = view.findViewById(R.id.rowDob);
    rowAddress = view.findViewById(R.id.rowAddress);
    cardVerifiedBadge = view.findViewById(R.id.cardVerifiedBadge);
    txtVerifiedBadge = view.findViewById(R.id.txtVerifiedBadge);
    loadingIndicator = view.findViewById(R.id.loadingIndicator);
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

  private void setupSettingsRows() {
    setRowLabel(R.id.rowSecurityAnswer, "Security Answer");
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

  private void setRowLabel(int rowId, String label) {
    ((TextView) requireView().findViewById(rowId).findViewById(R.id.txtSettingsLabel))
        .setText(label);
  }

  private void bindPatient(PatientResponse patient) {
    txtName.setText(patient.getName());
    txtEmergencyContact.setText(patient.getEmergency_contact());
    InfoRowBinder.bind(
        new InfoRowBinder.Row(
            rowAge, R.drawable.age, "Age", calculateAge(patient.getDate_of_birth())),
        new InfoRowBinder.Row(
            rowGender, genderIcon(patient.getGender()), "Gender", patient.getGender()),
        new InfoRowBinder.Row(
            rowBloodGroup, R.drawable.bloodtype, "Blood Group", patient.getBlood_group()),
        new InfoRowBinder.Row(rowEmail, R.drawable.email, "Email", sessionManager.getEmail()),
        new InfoRowBinder.Row(rowPhone, R.drawable.phone, "Phone", patient.getPhone()),
        new InfoRowBinder.Row(
            rowDob, R.drawable.birthdate, "Date of Birth", patient.getDate_of_birth()),
        new InfoRowBinder.Row(rowAddress, R.drawable.location, "Address", patient.getAddress()));
    bindVerificationBadge(
        patient.isIs_verified(), patient.getCitizenship_number(), patient.getRejection_reason());
    bindProfilePic(patient.getProfile_pic_url());
  }

  private void bindProfilePic(String profilePicUrl) {
    ShapeableImageView imgProfile = requireView().findViewById(R.id.imgProfile);
    ImageLoader.loadProfilePic(this, imgProfile, profilePicUrl);
  }

  private void bindVerificationBadge(
      boolean isVerified, String citizenshipNumber, String rejectionReason) {
    if (isVerified) {
      txtVerifiedBadge.setText("Verified");
      txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_tertiary_container));
      cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.tertiary_container));
      cardVerifiedBadge.setOnClickListener(null);
      cardVerifiedBadge.setClickable(false);
    } else if (citizenshipNumber != null) {
      txtVerifiedBadge.setText("Pending Review");
      txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_secondary_container));
      cardVerifiedBadge.setCardBackgroundColor(
          requireContext().getColor(R.color.secondary_container));
      cardVerifiedBadge.setOnClickListener(null);
      cardVerifiedBadge.setClickable(false);
    } else if (rejectionReason != null) {
      txtVerifiedBadge.setText("Rejected - Tap to resubmit");
      txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_error_container));
      cardVerifiedBadge.setCardBackgroundColor(requireContext().getColor(R.color.error_container));
      cardVerifiedBadge.setClickable(true);
      cardVerifiedBadge.setOnClickListener(
          v -> {
            Toast.makeText(requireContext(), rejectionReason, Toast.LENGTH_LONG).show();
            startActivity(new Intent(requireContext(), VerificationActivity.class));
          });
    } else {
      txtVerifiedBadge.setText("Not Verified");
      txtVerifiedBadge.setTextColor(requireContext().getColor(R.color.on_error_container));
      cardVerifiedBadge.setCardBackgroundColor(requireContext().getColor(R.color.error_container));
      cardVerifiedBadge.setClickable(true);
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
    LoadingHelper.show(loadingIndicator);
    scrollContent.setVisibility(View.GONE);

    PatientApi patientApi = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        patientApi.getMyProfile(),
        this,
        LoadingHelper.wrapSuccess(loadingIndicator, scrollContent, this::bindPatient),
        LoadingHelper.wrapError(
            loadingIndicator,
            scrollContent,
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
