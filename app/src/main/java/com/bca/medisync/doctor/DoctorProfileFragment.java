package com.bca.medisync.doctor;

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
import androidx.fragment.app.Fragment;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.databinding.FragmentDoctorProfileBinding;
import com.bca.medisync.util.AuthUtils;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.ProfilePicUploader;

public class DoctorProfileFragment extends Fragment {

  private FragmentDoctorProfileBinding binding;

  private SessionManager sessionManager;
  private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

  public DoctorProfileFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentDoctorProfileBinding.inflate(inflater, container, false);
    return binding.getRoot();
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

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void setupListeners() {
    binding.imgDoctorProfile.setOnClickListener(
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
          bindProfilePic(d.getProfile_pic_url());
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
              loadHospitalName(profile.getHospital_id());
            }),
        LoadingHelper.wrapError(
            binding.loadingIndicator,
            binding.scrollContent,
            ApiCallback.simpleError(requireContext(), "Failed to load profile.")));
  }

  private void loadHospitalName(int hospitalId) {
    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitalDetail(hospitalId),
        this,
        h -> {
          if (binding == null) return;
          InfoRowBinder.setValue(binding.rowHospital.getRoot(), h.getName());
        },
        (code, msg) -> {
          if (binding == null) return;
          InfoRowBinder.setValue(binding.rowHospital.getRoot(), "Hospital #" + hospitalId);
        });
  }

  private void bindProfile(DoctorProfileResponse p) {
    if (binding == null) return;

    int years = p.getYears_experience() != null ? p.getYears_experience() : 0;

    binding.txtDoctorName.setText("Dr. " + p.getName());
    binding.txtRole.setText(p.getSpeciality());

    if (p.is_verified()) {
      binding.txtRegistrationBadge.setText("Verified");
      binding.txtRegistrationBadge.setTextColor(
          requireContext().getColor(R.color.on_tertiary_container));
      binding.txtRegistrationBadge.setBackgroundColor(
          requireContext().getColor(R.color.tertiary_container));
    } else {
      binding.txtRegistrationBadge.setText("Pending Verification");
      binding.txtRegistrationBadge.setTextColor(
          requireContext().getColor(R.color.on_error_container));
      binding.txtRegistrationBadge.setBackgroundColor(
          requireContext().getColor(R.color.error_container));
    }

    if (p.getBio() != null && !p.getBio().trim().isEmpty()) {
      binding.txtBio.setVisibility(View.VISIBLE);
      binding.txtBio.setText(p.getBio());
    } else {
      binding.txtBio.setVisibility(View.GONE);
    }

    bindProfilePic(p.getProfile_pic_url());

    binding.statPatientsMonthValue.setText(String.valueOf(p.getPatients_this_month()));
    binding.statPatientsTotalValue.setText(String.valueOf(p.getTotal_patients()));

    InfoRowBinder.bind(
        new InfoRowBinder.Row(
            binding.rowSpecialization.getRoot(),
            R.drawable.stethoscope,
            "Specialization",
            p.getSpeciality()),
        new InfoRowBinder.Row(
            binding.rowHospital.getRoot(),
            R.drawable.hospital,
            "Hospital",
            "Hospital #" + p.getHospital_id()),
        new InfoRowBinder.Row(
            binding.rowExperience.getRoot(),
            R.drawable.ic_nav_calendar,
            "Experience",
            years > 0 ? years + " years" : "Not specified"),
        new InfoRowBinder.Row(binding.rowPhone.getRoot(), R.drawable.phone, "Phone", p.getPhone()),
        new InfoRowBinder.Row(
            binding.rowEmail.getRoot(), R.drawable.email, "Email", sessionManager.getEmail()),
        new InfoRowBinder.Row(
            binding.rowAddress.getRoot(), R.drawable.location, "Address", p.getAddress()));
  }

  private void bindProfilePic(String profilePicUrl) {
    if (binding == null) return;
    ImageLoader.loadProfilePic(this, binding.imgDoctorProfile, profilePicUrl);
  }
}
