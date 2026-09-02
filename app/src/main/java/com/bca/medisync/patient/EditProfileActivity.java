package com.bca.medisync.patient;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientUpdateRequest;
import com.bca.medisync.databinding.ActivityEditProfileBinding;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.ProfilePicUploader;

public class EditProfileActivity extends AppCompatActivity {

  private ActivityEditProfileBinding binding;
  private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    pickMedia =
        registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
              if (uri != null) {
                uploadProfilePic(uri);
              }
            });

    EdgeToEdge.enable(this);
    binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    ViewCompat.setOnApplyWindowInsetsListener(
        binding.main,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });

    binding.imgProfilePreview.setOnClickListener(
        v ->
            pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));

    binding.toolbar.setNavigationOnClickListener(v -> finish());
    binding.btnSave.setOnClickListener(v -> attemptSave());

    loadCurrentProfile();
  }

  private void bindProfilePic(String profilePicUrl) {
    ImageLoader.loadProfilePic(this, binding.imgProfilePreview, profilePicUrl);
  }

  private void loadCurrentProfile() {
    PatientApi api = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        p -> {
          binding.etName.setText(p.getName());
          binding.etPhone.setText(p.getPhone());
          binding.etAddress.setText(p.getAddress());
          binding.etEmergencyContact.setText(p.getEmergency_contact());
          bindProfilePic(p.getProfile_pic_url());
        },
        ApiCallback.simpleError(this, "Failed to load current profile."));
  }

  private void uploadProfilePic(Uri uri) {
    PatientApi api = ApiClient.api(PatientApi.class);
    ProfilePicUploader.upload(
        this,
        null,
        uri,
        "profile_pic",
        api::updateProfilePic,
        p -> {
          Toast.makeText(this, "Profile picture updated.", Toast.LENGTH_SHORT).show();
          bindProfilePic(p.getProfile_pic_url());
        });
  }

  private void attemptSave() {
    String name = textOf(binding.etName);
    String phone = textOf(binding.etPhone);
    String address = textOf(binding.etAddress);
    String emergencyContact = textOf(binding.etEmergencyContact);
    if (name.isEmpty()) {
      binding.etName.setError("Name is required");
      return;
    }
    if (phone.isEmpty()) {
      binding.etPhone.setError("Phone is required");
      return;
    }
    if (address.isEmpty()) {
      binding.etAddress.setError("Address is required");
      return;
    }
    if (emergencyContact.isEmpty()) {
      binding.etEmergencyContact.setError("Emergency contact is required");
      return;
    }
    binding.btnSave.setEnabled(false);
    PatientApi api = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        api.updateMyProfile(new PatientUpdateRequest(name, phone, address, emergencyContact)),
        body -> {
          binding.btnSave.setEnabled(true);
          Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show();
          finish();
        },
        (code, msg) -> {
          binding.btnSave.setEnabled(true);
          ApiCallback.simpleError(this, "Failed to update profile.").run(code, msg);
        });
  }

  private String textOf(com.google.android.material.textfield.TextInputEditText et) {
    return et.getText() != null ? et.getText().toString().trim() : "";
  }
}
