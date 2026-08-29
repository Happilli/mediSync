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
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientUpdateRequest;
import com.bca.medisync.util.ImageLoader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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

  private void bindProfilePic(String profilePicUrl) {
    ImageLoader.loadProfilePic(this, imgProfilePreview, profilePicUrl);
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

  private void loadCurrentProfile() {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        p -> {
          etName.setText(p.getName());
          etPhone.setText(p.getPhone());
          etAddress.setText(p.getAddress());
          etEmergencyContact.setText(p.getEmergency_contact());
          bindProfilePic(p.getProfile_pic_url());
        },
        ApiCallback.simpleError(this, "Failed to load current profile."));
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
    ApiCallback.handle(
        api.updateProfilePic(filePart),
        p -> {
          Toast.makeText(this, "Profile picture updated.", Toast.LENGTH_SHORT).show();
          bindProfilePic(p.getProfile_pic_url());
        },
        ApiCallback.simpleError(this, "Failed to update profile picture."));
    ;
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
    ;
    ApiCallback.handle(
        api.updateMyProfile(new PatientUpdateRequest(name, phone, address, emergencyContact)),
        body -> {
          btnSave.setEnabled(true);
          Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show();
          finish();
        },
        (code, msg) -> {
          btnSave.setEnabled(true);
          ApiCallback.simpleError(this, "Failed to update profile.").run(code, msg);
        });
  }

  private String textOf(TextInputEditText et) {
    return et.getText() != null ? et.getText().toString().trim() : "";
  }
}
