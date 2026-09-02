package com.bca.medisync.doctor;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.bca.medisync.util.AuthUtils;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.ProfilePicUploader;
import com.google.android.material.loadingindicator.LoadingIndicator;

public class DoctorProfileFragment extends Fragment {

  private SessionManager sessionManager;
  private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
  private View scrollContent;
  private LoadingIndicator loadingIndicator;

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
    scrollContent = view.findViewById(R.id.scrollContent);
    loadingIndicator = view.findViewById(R.id.loadingIndicator);

    pickMedia =
        registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
              if (uri != null) {
                uploadProfilePic(uri);
              }
            });

    setupListeners(view);
    loadProfile();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadProfile();
  }

  private void setupListeners(View view) {
    view.findViewById(R.id.imgDoctorProfile)
        .setOnClickListener(
            v ->
                pickMedia.launch(
                    new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

    view.findViewById(R.id.btnLogoutDoctor)
        .setOnClickListener(
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
    LoadingHelper.show(loadingIndicator);
    scrollContent.setVisibility(View.GONE);

    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        this,
        LoadingHelper.wrapSuccess(
            loadingIndicator,
            scrollContent,
            profile -> {
              bindProfile(profile);
              loadHospitalName(profile.getHospital_id());
            }),
        LoadingHelper.wrapError(
            loadingIndicator,
            scrollContent,
            ApiCallback.simpleError(requireContext(), "Failed to load profile.")));
  }

  private void loadHospitalName(int hospitalId) {
    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitalDetail(hospitalId),
        this,
        h -> {
          View view = getView();
          if (view == null) return;
          InfoRowBinder.setValue(view.findViewById(R.id.rowHospital), h.getName());
        },
        (code, msg) -> {
          View view = getView();
          if (view == null) return;
          InfoRowBinder.setValue(view.findViewById(R.id.rowHospital), "Hospital #" + hospitalId);
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

    View rowSpecialization = view.findViewById(R.id.rowSpecialization);
    View rowHospital = view.findViewById(R.id.rowHospital);
    View rowExperience = view.findViewById(R.id.rowExperience);
    View rowPhone = view.findViewById(R.id.rowPhone);
    View rowEmail = view.findViewById(R.id.rowEmail);
    View rowAddress = view.findViewById(R.id.rowAddress);

    InfoRowBinder.bind(
        new InfoRowBinder.Row(
            rowSpecialization, R.drawable.stethoscope, "Specialization", p.getSpeciality()),
        new InfoRowBinder.Row(
            rowHospital, R.drawable.hospital, "Hospital", "Hospital #" + p.getHospital_id()),
        new InfoRowBinder.Row(
            rowExperience,
            R.drawable.ic_nav_calendar,
            "Experience",
            years > 0 ? years + " years" : "Not specified"),
        new InfoRowBinder.Row(rowPhone, R.drawable.phone, "Phone", p.getPhone()),
        new InfoRowBinder.Row(rowEmail, R.drawable.email, "Email", sessionManager.getEmail()),
        new InfoRowBinder.Row(rowAddress, R.drawable.location, "Address", p.getAddress()));
  }

  private void bindProfilePic(String profilePicUrl) {
    View view = getView();
    if (view == null) return;
    ImageLoader.loadProfilePic(this, view.findViewById(R.id.imgDoctorProfile), profilePicUrl);
  }
}
