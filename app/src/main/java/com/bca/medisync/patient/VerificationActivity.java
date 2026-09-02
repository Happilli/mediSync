package com.bca.medisync.patient;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.bca.medisync.databinding.ActivityVerificationBinding;
import com.bca.medisync.util.FileUploadHelper;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class VerificationActivity extends AppCompatActivity {

  private ActivityVerificationBinding binding;

  private Uri selectedImageUri;
  private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
      registerForActivityResult(
          new ActivityResultContracts.PickVisualMedia(),
          uri -> {
            if (uri != null) {
              selectedImageUri = uri;
              binding.cardPreview.setVisibility(View.VISIBLE);
              binding.imgPreview.setImageURI(uri);
            }
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    binding = ActivityVerificationBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    ViewCompat.setOnApplyWindowInsetsListener(
        binding.main,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });

    setupListeners();
  }

  private void setupListeners() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
    binding.btnPickPhoto.setOnClickListener(
        v ->
            pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));
    binding.btnSubmit.setOnClickListener(v -> submitVerification());
  }

  private void submitVerification() {
    String citizenshipNumber =
        binding.etCitizenshipNumber.getText() != null
            ? binding.etCitizenshipNumber.getText().toString().trim()
            : "";
    if (citizenshipNumber.isEmpty()) {
      binding.etCitizenshipNumber.setError("Citizenship number is required..");
      return;
    }
    if (selectedImageUri == null) {
      Toast.makeText(this, "please select a citizenship photo", Toast.LENGTH_SHORT).show();
      return;
    }

    File cachedFile;
    try {
      cachedFile = FileUploadHelper.copyUriToCache(this, selectedImageUri, "citizenship");
    } catch (Exception e) {
      Toast.makeText(this, "couldn't read the selected photo!", Toast.LENGTH_SHORT).show();
      return;
    }

    binding.btnSubmit.setEnabled(false);
    binding.loadingIndicator.setVisibility(View.VISIBLE);
    binding.loadingIndicator.show();

    RequestBody citizenshipBody =
        RequestBody.create(citizenshipNumber, MediaType.parse("text/plain"));
    MultipartBody.Part filePart = FileUploadHelper.toImagePart(cachedFile, "file");

    PatientApi patientApi = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        patientApi.requestVerification(citizenshipBody, filePart),
        body -> {
          Toast.makeText(
                  VerificationActivity.this,
                  "Verification request submitted, Wait for approval..",
                  Toast.LENGTH_LONG)
              .show();
          finish();
        },
        (code, msg) -> {
          binding.loadingIndicator.hide();
          binding.loadingIndicator.setVisibility(View.INVISIBLE);
          binding.btnSubmit.setEnabled(true);

          if (code == 400) {
            Toast.makeText(
                    VerificationActivity.this,
                    "Verification already requested...",
                    Toast.LENGTH_SHORT)
                .show();
            finish();
          } else if (code == -1) {
            Toast.makeText(VerificationActivity.this, "Network erros: " + msg, Toast.LENGTH_SHORT)
                .show();
          } else {
            Toast.makeText(
                    VerificationActivity.this, "Submission failed, try again..", Toast.LENGTH_LONG)
                .show();
          }
        });
  }
}
