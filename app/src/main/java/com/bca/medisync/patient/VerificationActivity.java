package com.bca.medisync.patient;

import android.content.ContentResolver;
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

public class VerificationActivity extends AppCompatActivity {
  private MaterialToolbar toolbar;
  private ImageView imgPreview;
  private MaterialButton btnPickPhoto, btnSubmit;
  private TextInputEditText etCitizenshipNumber;

  private Uri selectedImageUri;
  private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
      registerForActivityResult(
          new ActivityResultContracts.PickVisualMedia(),
          uri -> {
            if (uri != null) {
              selectedImageUri = uri;
              imgPreview.setImageURI(uri);
            }
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_verification);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initiViews();
    setupListeners();
  }

  private void initiViews() {
    toolbar = findViewById(R.id.toolbar);
    imgPreview = findViewById(R.id.imgPreview);
    btnPickPhoto = findViewById(R.id.btnPickPhoto);
    btnSubmit = findViewById(R.id.btnSubmit);
    etCitizenshipNumber = findViewById(R.id.etCitizenshipNumber);
  }

  private void setupListeners() {
    toolbar.setNavigationOnClickListener(v -> finish());
    btnPickPhoto.setOnClickListener(
        v ->
            pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));
    btnSubmit.setOnClickListener(v -> submitVerification());
  }

  private void submitVerification() {
    String citizenshipNumber =
        etCitizenshipNumber.getText() != null
            ? etCitizenshipNumber.getText().toString().trim()
            : "";
    if (citizenshipNumber.isEmpty()) {
      etCitizenshipNumber.setError("Citizenship number is required..");
      return;
    }
    if (selectedImageUri == null) {
      Toast.makeText(this, "please select a citizenship photo", Toast.LENGTH_SHORT).show();
      return;
    }
    btnSubmit.setEnabled(false);
    File cachedFile;

    try {
      cachedFile = copyUriToCache(selectedImageUri);
    } catch (Exception e) {
      btnSubmit.setEnabled(true);
      Toast.makeText(this, "couldn't read the selected photo!", Toast.LENGTH_SHORT).show();
      return;
    }
    RequestBody citizenshipBody =
        RequestBody.create(citizenshipNumber, MediaType.parse("text/plain"));
    RequestBody fileBody = RequestBody.create(cachedFile, MediaType.parse("image/*"));
    MultipartBody.Part filePart =
        MultipartBody.Part.createFormData("file", cachedFile.getName(), fileBody);

    PatientApi patientApi = ApiClient.getRetrofit().create(PatientApi.class);
    patientApi
        .requestVerification(citizenshipBody, filePart)
        .enqueue(
            new Callback<PatientResponse>() {
              @Override
              public void onResponse(
                  Call<PatientResponse> call, Response<PatientResponse> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                  Toast.makeText(
                          VerificationActivity.this,
                          "Verification request submitted, Wait for approval..",
                          Toast.LENGTH_LONG)
                      .show();
                  finish();
                } else if (response.code() == 400) {
                  Toast.makeText(
                          VerificationActivity.this,
                          "Verification already requested...",
                          Toast.LENGTH_SHORT)
                      .show();
                } else {
                  Toast.makeText(
                          VerificationActivity.this,
                          "Submission failed, try again..",
                          Toast.LENGTH_LONG)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(
                        VerificationActivity.this,
                        "Network erros: " + t.getMessage(),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private File copyUriToCache(Uri uri) throws Exception {
    ContentResolver resolver = getContentResolver();
    String mimeType = resolver.getType(uri);
    String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    if (ext == null) ext = "jpg";
    File outFile = new File(getCacheDir(), "citizenship_" + System.currentTimeMillis() + "." + ext);
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
}
