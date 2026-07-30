package com.bca.medisync.patient;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.data.remote.dto.patient.PatientUpdateRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

  private MaterialToolbar toolbar;
  private TextInputEditText etName, etPhone, etAddress, etEmergencyContact;
  private MaterialButton btnSave;
  private ImageView imgProfilePreview;
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
    setContentView(R.layout.activity_edit_profile);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    setupToolbar();
    loadCurrentProfile();
    setupListeners();
  }

  private void initViews() {
    toolbar = findViewById(R.id.toolbar);
    etName = findViewById(R.id.etName);
    etPhone = findViewById(R.id.etPhone);
    etAddress = findViewById(R.id.etAddress);
    etEmergencyContact = findViewById(R.id.etEmergencyContact);
    btnSave = findViewById(R.id.btnSave);
    imgProfilePreview = findViewById(R.id.imgProfilePreview);
    imgProfilePreview.setOnClickListener(
        v ->
            pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void loadCurrentProfile() {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    api.getMyProfile()
        .enqueue(
            new Callback<PatientResponse>() {
              @Override
              public void onResponse(
                  Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                  PatientResponse p = response.body();
                  etName.setText(p.getName());
                  etPhone.setText(p.getPhone());
                  etAddress.setText(p.getAddress());
                  etEmergencyContact.setText(p.getEmergency_contact());
                  bindProfilePic(p.getProfile_pic_url());
                } else {
                  Toast.makeText(
                          EditProfileActivity.this,
                          "Failed to load current profile.",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                Toast.makeText(
                        EditProfileActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindProfilePic(String profilePicUrl) {
    if (profilePicUrl == null || profilePicUrl.isEmpty()) {
      imgProfilePreview.setImageResource(R.drawable.ic_nav_profile);
      return;
    }
    Glide.with(this)
        .load(ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + profilePicUrl)
        .placeholder(R.drawable.ic_nav_profile)
        .error(R.drawable.ic_nav_profile)
        .centerCrop()
        .into(imgProfilePreview);
  }

  private void uploadProfilePic(Uri uri) {
    File cachedFile;
    try {
      cachedFile = copyUriToCache(uri);
    } catch (Exception e) {
      Toast.makeText(this, "Couldn't read the selected photo!", Toast.LENGTH_SHORT).show();
      return;
    }

    RequestBody fileBody = RequestBody.create(cachedFile, MediaType.parse("image/*"));
    MultipartBody.Part filePart =
        MultipartBody.Part.createFormData("file", cachedFile.getName(), fileBody);

    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    api.updateProfilePic(filePart)
        .enqueue(
            new Callback<PatientResponse>() {
              @Override
              public void onResponse(
                  Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                  Toast.makeText(
                          EditProfileActivity.this, "Profile picture updated.", Toast.LENGTH_SHORT)
                      .show();
                  bindProfilePic(response.body().getProfile_pic_url());
                } else {
                  Toast.makeText(
                          EditProfileActivity.this,
                          "Failed to update profile picture.",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                Toast.makeText(
                        EditProfileActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private File copyUriToCache(Uri uri) throws Exception {
    android.content.ContentResolver resolver = getContentResolver();
    String mimeType = resolver.getType(uri);
    String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    if (ext == null) ext = "jpg";
    File outFile = new File(getCacheDir(), "profile_pic_" + System.currentTimeMillis() + "." + ext);
    try (InputStream in = resolver.openInputStream(uri);
        FileOutputStream out = new FileOutputStream(outFile)) {
      byte[] buffer = new byte[8192];
      int read;
      while (in != null && (read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    }
    return outFile;
  }

  private void setupListeners() {
    btnSave.setOnClickListener(v -> attemptSave());
  }

  private void attemptSave() {
    String name = textOf(etName);
    String phone = textOf(etPhone);
    String address = textOf(etAddress);
    String emergencyContact = textOf(etEmergencyContact);

    if (name.isEmpty()) {
      etName.setError("Name is required");
      return;
    }
    if (phone.isEmpty()) {
      etPhone.setError("Phone is required");
      return;
    }
    if (address.isEmpty()) {
      etAddress.setError("Address is required");
      return;
    }
    if (emergencyContact.isEmpty()) {
      etEmergencyContact.setError("Emergency contact is required");
      return;
    }

    btnSave.setEnabled(false);

    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    api.updateMyProfile(new PatientUpdateRequest(name, phone, address, emergencyContact))
        .enqueue(
            new Callback<PatientResponse>() {
              @Override
              public void onResponse(
                  Call<PatientResponse> call, Response<PatientResponse> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                  Toast.makeText(EditProfileActivity.this, "Profile updated.", Toast.LENGTH_SHORT)
                      .show();
                  finish();
                } else {
                  Toast.makeText(
                          EditProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(
                        EditProfileActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private String textOf(TextInputEditText et) {
    return et.getText() != null ? et.getText().toString().trim() : "";
  }
}
