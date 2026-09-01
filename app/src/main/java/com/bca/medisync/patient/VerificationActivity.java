package com.bca.medisync.patient;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class VerificationActivity extends AppCompatActivity {
  private MaterialToolbar toolbar;
  private ImageView imgPreview;
  private MaterialCardView cardPreview;
  private MaterialButton btnPickPhoto, btnSubmit;
  private TextInputEditText etCitizenshipNumber;
  private LoadingIndicator loadingIndicator;

  private Uri selectedImageUri;
  private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
      registerForActivityResult(
          new ActivityResultContracts.PickVisualMedia(),
          uri -> {
            if (uri != null) {
              selectedImageUri = uri;
              cardPreview.setVisibility(View.VISIBLE);
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
    cardPreview = findViewById(R.id.cardPreview);
    btnPickPhoto = findViewById(R.id.btnPickPhoto);
    btnSubmit = findViewById(R.id.btnSubmit);
    etCitizenshipNumber = findViewById(R.id.etCitizenshipNumber);
    loadingIndicator = findViewById(R.id.loadingIndicator);
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

    File cachedFile;
    try {
      cachedFile = copyUriToCache(selectedImageUri);
    } catch (Exception e) {
      Toast.makeText(this, "couldn't read the selected photo!", Toast.LENGTH_SHORT).show();
      return;
    }

    btnSubmit.setEnabled(false);
    loadingIndicator.setVisibility(View.VISIBLE);
    loadingIndicator.show();

    RequestBody citizenshipBody =
        RequestBody.create(citizenshipNumber, MediaType.parse("text/plain"));
    RequestBody fileBody = RequestBody.create(cachedFile, MediaType.parse("image/*"));
    MultipartBody.Part filePart =
        MultipartBody.Part.createFormData("file", cachedFile.getName(), fileBody);

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
          loadingIndicator.hide();
          loadingIndicator.setVisibility(View.INVISIBLE);
          btnSubmit.setEnabled(true);

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
