package com.bca.medisync.patient;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AuthApi;
import com.bca.medisync.data.remote.dto.register.PatientRegisterRequest;
import com.bca.medisync.data.remote.dto.register.RegisterResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

  private TextInputEditText etName, etEmail, etPassword, etPhone, etAddress, etEmergencyContact;
  private AutoCompleteTextView etGender, etBloodGroup;
  private MaterialButton btnSelectDob, btnRegister;
  private android.widget.TextView txtSelectedDob, goToLogin;

  private String selectedDob = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_register);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.mainStuff),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });

    initViews();
    setupDropdowns();
    setupDobPicker();
    setupListeners();
  }

  private void initViews() {
    etName = findViewById(R.id.etName);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    etPhone = findViewById(R.id.etPhone);
    etAddress = findViewById(R.id.etAddress);
    etEmergencyContact = findViewById(R.id.etEmergencyContact);
    etGender = findViewById(R.id.etGender);
    etBloodGroup = findViewById(R.id.etBloodGroup);
    btnSelectDob = findViewById(R.id.btnSelectDob);
    txtSelectedDob = findViewById(R.id.txtSelectedDob);
    btnRegister = findViewById(R.id.btnRegister);
    goToLogin = findViewById(R.id.GoToLogin);
  }

  private void setupDropdowns() {
    String[] genders = {"Male", "Female", "Other"};
    etGender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genders));

    String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    etBloodGroup.setAdapter(
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bloodGroups));
  }

  private void setupDobPicker() {
    MaterialDatePicker<Long> picker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select Date of Birth").build();

    btnSelectDob.setOnClickListener(v -> picker.show(getSupportFragmentManager(), "DOB_PICKER"));

    picker.addOnPositiveButtonClickListener(
        selection -> {
          selectedDob =
              new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection));
          String display =
              new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
          txtSelectedDob.setText(display);
        });
  }

  private void setupListeners() {
    goToLogin.setOnClickListener(v -> finish());
    btnRegister.setOnClickListener(v -> attemptRegister());
  }

  private void attemptRegister() {
    String name = textOf(etName);
    String email = textOf(etEmail);
    String password = textOf(etPassword);
    String phone = textOf(etPhone);
    String address = textOf(etAddress);
    String emergencyContact = textOf(etEmergencyContact);
    String gender = etGender.getText().toString().trim();
    String bloodGroup = etBloodGroup.getText().toString().trim();

    if (name.isEmpty()) {
      etName.setError("Name is required");
      return;
    }
    if (email.isEmpty()) {
      etEmail.setError("Email is required");
      return;
    }
    if (password.isEmpty()) {
      etPassword.setError("Password is required");
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
    if (selectedDob.isEmpty()) {
      Toast.makeText(this, "Please select date of birth", Toast.LENGTH_SHORT).show();
      return;
    }
    if (gender.isEmpty()) {
      Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
      return;
    }
    if (bloodGroup.isEmpty()) {
      Toast.makeText(this, "Please select blood group", Toast.LENGTH_SHORT).show();
      return;
    }
    if (emergencyContact.isEmpty()) {
      etEmergencyContact.setError("Emergency contact is required");
      return;
    }

    btnRegister.setEnabled(false);

    PatientRegisterRequest request =
        new PatientRegisterRequest(
            email,
            password,
            name,
            phone,
            address,
            selectedDob,
            gender.toLowerCase(Locale.ROOT),
            bloodGroup.toUpperCase(Locale.ROOT),
            emergencyContact);

    AuthApi authApi = ApiClient.getRetrofit().create(AuthApi.class);
    authApi
        .registerPatient(request)
        .enqueue(
            new Callback<RegisterResponse>() {
              @Override
              public void onResponse(
                  Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                  Toast.makeText(
                          RegisterActivity.this,
                          response.body().getMessage() + "\n" + response.body().getRemarks(),
                          Toast.LENGTH_LONG)
                      .show();
                  finish();
                } else {
                  Toast.makeText(
                          RegisterActivity.this,
                          "Registration failed. Email may already be in use.",
                          Toast.LENGTH_LONG)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<RegisterResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(
                        RegisterActivity.this,
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
