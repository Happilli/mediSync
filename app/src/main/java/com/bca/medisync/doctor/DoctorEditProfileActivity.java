package com.bca.medisync.doctor;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorUpdateRequest;
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

public class DoctorEditProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etName, etPhone, etSpeciality, etDepartment, etExperience, etBio, etAddress;
    private MaterialButton btnSave;
    private ImageView imgProfilePreview;
    private ProgressBar progressBar;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                uploadProfilePic(uri);
            }
        });

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
        etSpeciality = findViewById(R.id.etSpeciality);
        etDepartment = findViewById(R.id.etDepartment);
        etExperience = findViewById(R.id.etExperience);
        etBio = findViewById(R.id.etBio);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);
        imgProfilePreview = findViewById(R.id.imgProfilePreview);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCurrentProfile() {
        showLoading(true);
        DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
        api.getMyProfile().enqueue(new Callback<DoctorProfileResponse>() {
            @Override
            public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    DoctorProfileResponse p = response.body();
                    etName.setText(p.getName());
                    etPhone.setText(p.getPhone());
                    etSpeciality.setText(p.getSpeciality());
                    etDepartment.setText(p.getDepartment());
                    etExperience.setText(p.getYears_experience() != null ? String.valueOf(p.getYears_experience()) : "");
                    etBio.setText(p.getBio());
                    etAddress.setText(p.getAddress());
                    bindProfilePic(p.getProfile_pic_url());
                } else {
                    Toast.makeText(DoctorEditProfileActivity.this, "Failed to load current profile.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(DoctorEditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", cachedFile.getName(), fileBody);

        showLoading(true);
        DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
        api.updateProfilePic(filePart).enqueue(new Callback<DoctorProfileResponse>() {
            @Override
            public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DoctorEditProfileActivity.this, "Profile picture updated.", Toast.LENGTH_SHORT).show();
                    bindProfilePic(response.body().getProfile_pic_url());
                } else {
                    Toast.makeText(DoctorEditProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(DoctorEditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private File copyUriToCache(Uri uri) throws Exception {
        android.content.ContentResolver resolver = getContentResolver();
        String mimeType = resolver.getType(uri);
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (ext == null) ext = "jpg";
        File outFile = new File(getCacheDir(), "profile_pic_doctor_" + System.currentTimeMillis() + "." + ext);
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
        imgProfilePreview.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void attemptSave() {
        String name = textOf(etName);
        String phone = textOf(etPhone);
        String speciality = textOf(etSpeciality);
        String department = textOf(etDepartment);
        String experienceStr = textOf(etExperience);
        String bio = textOf(etBio);
        String address = textOf(etAddress);

        if (name.isEmpty()) { etName.setError("Required"); return; }
        if (phone.isEmpty()) { etPhone.setError("Required"); return; }

        Integer yearsExperience = null;
        if (!experienceStr.isEmpty()) {
            try {
                yearsExperience = Integer.parseInt(experienceStr);
            } catch (NumberFormatException ignored) {}
        }

        btnSave.setEnabled(false);
        showLoading(true);

        DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
        api.updateMyProfile(new DoctorUpdateRequest(name, phone, bio, address, yearsExperience, speciality, department))
                .enqueue(new Callback<DoctorProfileResponse>() {
                    @Override
                    public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                        showLoading(false);
                        btnSave.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(DoctorEditProfileActivity.this, "Profile updated.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(DoctorEditProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                        showLoading(false);
                        btnSave.setEnabled(true);
                        Toast.makeText(DoctorEditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
