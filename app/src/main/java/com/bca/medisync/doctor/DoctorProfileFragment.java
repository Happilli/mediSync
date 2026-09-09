package com.bca.medisync.doctor;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.databinding.FragmentDoctorProfileBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.AuthUtils;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.ProfilePicUploader;
import com.bca.medisync.util.ViewUtils;

public class DoctorProfileFragment extends BaseBindingFragment<FragmentDoctorProfileBinding> {
  private SessionManager sessionManager;
  private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

  public DoctorProfileFragment() {}

  @Override
  protected FragmentDoctorProfileBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentDoctorProfileBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sessionManager = new SessionManager(requireContext());
    pickMedia =
        registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
              if (uri != null) {
                uploadProfilePic(uri);
              }
            });
    setupListeners();
    loadProfile();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadProfile();
  }

  private void setupListeners() {
    binding.imgDoctorProfile.setOnClickListener(
        v ->
            pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));
    binding.btnEditDoctorProfile.setOnClickListener(
        v ->
            pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));
    binding.btnLogoutDoctor.setOnClickListener(
        v -> AuthUtils.logout((androidx.appcompat.app.AppCompatActivity) requireActivity()));
  }

  private void uploadProfilePic(Uri uri) {
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ProfilePicUploader.upload(
        requireContext(),
        this,
        uri,
        "doctor_profile_pic",
        api::updateProfilePic,
        d -> {
          Toast.makeText(requireContext(), "Profile picture updated.", Toast.LENGTH_SHORT).show();
          bindProfilePic(d.profile_pic_url());
        });
  }

  private void loadProfile() {
    LoadingHelper.show(binding.loadingIndicator);
    binding.scrollContent.setVisibility(View.GONE);
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        this,
        LoadingHelper.wrapSuccess(
            binding.loadingIndicator,
            binding.scrollContent,
            profile -> {
              bindProfile(profile);
              loadHospitalDetails(profile.doctor().hospital_id());
            }),
        LoadingHelper.wrapError(
            binding.loadingIndicator,
            binding.scrollContent,
            ApiErrorHandler.with(requireContext()).fallback("Failed to load profile.").build()));
  }

  private void loadHospitalDetails(int hospitalId) {
    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitalDetail(hospitalId),
        this,
        h -> {
          if (binding == null) return;
          InfoRowBinder.setValue(binding.rowHospital.getRoot(), h.name());
          ImageLoader.loadHospitalImage(this, binding.imgHospitalBanner, h.image_url());
        },
        (code, msg) -> {
          if (binding == null) return;
          InfoRowBinder.setValue(binding.rowHospital.getRoot(), "Hospital #" + hospitalId);
        });
  }

  private void bindProfile(DoctorProfileResponse p) {
    if (binding == null) return;
    int years = p.doctor().years_experience() != null ? p.doctor().years_experience() : 0;
    binding.txtDoctorName.setText("Dr. " + p.doctor().name());
    binding.txtRole.setText(p.doctor().speciality());
    bindVerificationBadge(p.doctor().is_verified());
    if (p.doctor().bio() != null && !p.doctor().bio().trim().isEmpty()) {
      binding.txtBio.setVisibility(View.VISIBLE);
      binding.txtBio.setText(p.doctor().bio());
    } else {
      binding.txtBio.setVisibility(View.GONE);
    }
    bindProfilePic(p.doctor().profile_pic_url());
    binding.statPatientsMonthValue.setText(String.valueOf(p.patients_this_month()));
    binding.statPatientsTotalValue.setText(String.valueOf(p.total_patients()));
    InfoRowBinder.bind(
        new InfoRowBinder.Row(
            binding.rowSpecialization.getRoot(), "Specialization", p.doctor().speciality()),
        new InfoRowBinder.Row(
            binding.rowHospital.getRoot(), "Hospital", "Hospital #" + p.doctor().hospital_id()),
        new InfoRowBinder.Row(
            binding.rowExperience.getRoot(),
            "Experience",
            years > 0 ? years + " years" : "Not specified"),
        new InfoRowBinder.Row(binding.rowPhone.getRoot(), "Phone", p.doctor().phone()),
        new InfoRowBinder.Row(binding.rowEmail.getRoot(), "Email", sessionManager.getEmail()),
        new InfoRowBinder.Row(binding.rowAddress.getRoot(), "Address", p.doctor().address()));
  }

  private void bindVerificationBadge(boolean isVerified) {
    String text = isVerified ? "Verified" : "Pending Verification";
    int colorRes = isVerified ? R.color.tertiary : R.color.error;
    int iconRes = isVerified ? R.drawable.verified_on : R.drawable.verified_off;
    int color = requireContext().getColor(colorRes);

    binding.txtRegistrationBadge.setText(text);
    binding.txtRegistrationBadge.setTextColor(color);

    Drawable icon = ContextCompat.getDrawable(requireContext(), iconRes);
    if (icon != null) {
      icon = icon.mutate();
      icon.setTint(color);
    }
    binding.txtRegistrationBadge.setCompoundDrawablesRelativeWithIntrinsicBounds(
        icon, null, null, null);
    binding.txtRegistrationBadge.setCompoundDrawablePadding(ViewUtils.dp(requireContext(), 6));
  }

  private void bindProfilePic(String profilePicUrl) {
    if (binding == null) return;
    int borderPx = ViewUtils.dp(requireContext(), 3);
    int borderColor = requireContext().getColor(R.color.surface);
    ImageLoader.loadProfilePicShaped(
        this, binding.imgDoctorProfile, profilePicUrl, R.drawable.pill, borderPx, borderColor);
  }
}
