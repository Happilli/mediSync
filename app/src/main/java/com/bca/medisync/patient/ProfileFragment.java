package com.bca.medisync.patient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.databinding.FragmentProfileBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.AuthUtils;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.ViewUtils;
import java.util.Calendar;

public class ProfileFragment extends BaseBindingFragment<FragmentProfileBinding>
    implements NotificationCenter.Listener {
  private SessionManager sessionManager;
  private ActivityResultLauncher<String> notifPermLauncher;

  public ProfileFragment() {}

  @Override
  protected FragmentProfileBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
    return FragmentProfileBinding.inflate(inflater, container, false);
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
    if ("patient_verified".equals(notification.type())
        || "patient_verification_rejected".equals(notification.type())) {
      loadPatientData();
    }
  }

  private void bindPatient(PatientResponse patient) {
    if (binding == null) return;
    binding.txtName.setText(patient.name());
    binding.txtEmergencyContact.setText(patient.emergency_contact());
    InfoRowBinder.bind(
        new InfoRowBinder.Row(
            binding.rowAge.getRoot(), "Age", calculateAge(patient.date_of_birth())),
        new InfoRowBinder.Row(binding.rowGender.getRoot(), "Gender", patient.gender()),
        new InfoRowBinder.Row(
            binding.rowBloodGroup.getRoot(), "Blood Group", patient.blood_group()),
        new InfoRowBinder.Row(binding.rowEmail.getRoot(), "Email", sessionManager.getEmail()),
        new InfoRowBinder.Row(binding.rowPhone.getRoot(), "Phone", patient.phone()),
        new InfoRowBinder.Row(binding.rowDob.getRoot(), "Date of Birth", patient.date_of_birth()),
        new InfoRowBinder.Row(binding.rowAddress.getRoot(), "Address", patient.address()));
    bindVerificationBadge(
        patient.is_verified(), patient.citizenship_number(), patient.rejection_reason());
    bindProfilePic(patient.profile_pic_url());
  }

  private void bindProfilePic(String profilePicUrl) {
    if (binding == null) return;
    int borderPx = ViewUtils.dp(requireContext(), 3);
    int borderColor = requireContext().getColor(R.color.surface);
    ImageLoader.loadProfilePicShaped(
        this, binding.imgProfile, profilePicUrl, R.drawable.cookie12sided, borderPx, borderColor);
  }

  private void bindVerificationBadge(
      boolean isVerified, String citizenshipNumber, String rejectionReason) {
    if (isVerified) {
      setBadge("Verified", R.drawable.verified_on, R.color.tertiary, null);
    } else if (citizenshipNumber != null) {
      setBadge("Pending Review", R.drawable.verified_off, R.color.secondary, null);
    } else if (rejectionReason != null) {
      setBadge(
          "Rejected - Tap to resubmit",
          R.drawable.verified_off,
          R.color.error,
          v -> {
            Toast.makeText(requireContext(), rejectionReason, Toast.LENGTH_LONG).show();
            startActivity(new Intent(requireContext(), VerificationActivity.class));
          });
    } else {
      setBadge(
          "Not Verified - Tap to verify",
          R.drawable.verified_off,
          R.color.error,
          v -> startActivity(new Intent(requireContext(), VerificationActivity.class)));
    }
  }

  private void setBadge(String text, int iconRes, int colorRes, View.OnClickListener listener) {
    binding.txtVerifiedBadge.setText(text);
    int color = requireContext().getColor(colorRes);
    binding.txtVerifiedBadge.setTextColor(color);

    Drawable icon = ContextCompat.getDrawable(requireContext(), iconRes);
    if (icon != null) {
      icon = icon.mutate();
      icon.setTint(color);
    }
    binding.txtVerifiedBadge.setCompoundDrawablesRelativeWithIntrinsicBounds(
        icon, null, null, null);
    binding.txtVerifiedBadge.setCompoundDrawablePadding(ViewUtils.dp(requireContext(), 6));
    binding.txtVerifiedBadge.setClickable(listener != null);
    binding.txtVerifiedBadge.setOnClickListener(listener);
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
            (code, msg) ->
                ApiErrorHandler.with(requireContext())
                    .on(403, "Your account is pending verification")
                    .fallback("Failed to load your profile")
                    .build()
                    .run(code, msg)));
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
    binding.btnLogout.setOnClickListener(
        v -> AuthUtils.logout((androidx.appcompat.app.AppCompatActivity) requireActivity()));
  }
}
