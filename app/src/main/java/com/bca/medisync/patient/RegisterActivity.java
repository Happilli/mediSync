package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AuthApi;
import com.bca.medisync.data.remote.dto.register.PatientRegisterRequest;
import com.bca.medisync.databinding.ActivityRegisterBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.ViewUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
  private ActivityRegisterBinding binding;
  private String selectedDob = "";
  private boolean passwordsMismatched = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    binding = ActivityRegisterBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    ViewCompat.setOnApplyWindowInsetsListener(
        binding.mainStuff,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    setupDobPicker();
    setupPasswordMatchWatcher();
    setupListeners();
  }

  private void setupPasswordMatchWatcher() {
    binding.etConfirmPassword.setOnFocusChangeListener(
        (v, hasFocus) -> {
          if (!hasFocus) checkPasswordsMatch();
        });
    binding.etPassword.setOnFocusChangeListener(
        (v, hasFocus) -> {
          if (!hasFocus && !ViewUtils.textOf(binding.etConfirmPassword).isEmpty())
            checkPasswordsMatch();
        });
  }

  private void checkPasswordsMatch() {
    String password = ViewUtils.textOf(binding.etPassword);
    String confirmPassword = ViewUtils.textOf(binding.etConfirmPassword);
    if (confirmPassword.isEmpty() || password.isEmpty()) {
      passwordsMismatched = false;
      binding.txtPasswordMismatch.setVisibility(View.GONE);
      return;
    }
    passwordsMismatched = !password.equals(confirmPassword);
    binding.txtPasswordMismatch.setVisibility(passwordsMismatched ? View.VISIBLE : View.GONE);
  }

  private void setupDobPicker() {
    MaterialDatePicker<Long> picker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select Date of Birth").build();

    binding.btnSelectDob.setOnClickListener(
        v -> picker.show(getSupportFragmentManager(), "DOB_PICKER"));

    picker.addOnPositiveButtonClickListener(
        selection -> {
          selectedDob =
              new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection));
          String display =
              new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
          binding.txtSelectedDob.setText(display);
        });
  }

  private void setupListeners() {
    binding.GoToLogin.setOnClickListener(v -> finish());
    binding.btnRegister.setOnClickListener(v -> attemptRegister());
  }

  private String getSelectedGender() {
    int checkedId = binding.radioGroupGender.getCheckedRadioButtonId();
    if (checkedId == R.id.radioMale) return "male";
    if (checkedId == R.id.radioFemale) return "female";
    if (checkedId == R.id.radioOther) return "other";
    return "";
  }

  private String getSelectedBloodGroup() {
    int checkedId = binding.chipGroupBloodGroup.getCheckedChipId();
    if (checkedId == View.NO_ID) return "";
    Chip chip = binding.getRoot().findViewById(checkedId);
    return chip.getText().toString().trim().toUpperCase(Locale.ROOT);
  }

  private void attemptRegister() {
    String name = ViewUtils.textOf(binding.etName);
    String email = ViewUtils.textOf(binding.etEmail);
    String password = ViewUtils.textOf(binding.etPassword);
    String confirmPassword = ViewUtils.textOf(binding.etConfirmPassword);
    String phone = ViewUtils.textOf(binding.etPhone);
    String address = ViewUtils.textOf(binding.etAddress);
    String emergencyContact = ViewUtils.textOf(binding.etEmergencyContact);
    String gender = getSelectedGender();
    String bloodGroup = getSelectedBloodGroup();
    String securityAnswer = ViewUtils.textOf(binding.etSecurityAnswer);

    if (name.isEmpty()) {
      binding.etName.setError("Name is required");
      return;
    }
    if (email.isEmpty()) {
      binding.etEmail.setError("Email is required");
      return;
    }
    if (password.isEmpty()) {
      binding.etPassword.setError("Password is required");
      return;
    }
    if (confirmPassword.isEmpty()) {
      binding.etConfirmPassword.setError("Please confirm your password");
      return;
    }
    if (passwordsMismatched || !password.equals(confirmPassword)) {
      binding.txtPasswordMismatch.setVisibility(View.VISIBLE);
      Toast.makeText(this, "Passwords don't match. Fix before proceeding.", Toast.LENGTH_SHORT)
          .show();
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
    if (securityAnswer.isEmpty()) {
      Toast.makeText(this, "Please answer the security question", Toast.LENGTH_SHORT).show();
      return;
    }
    if (emergencyContact.isEmpty()) {
      binding.etEmergencyContact.setError("Emergency contact is required");
      return;
    }

    binding.btnRegister.setEnabled(false);
    LoadingHelper.show(binding.loadingIndicator);

    PatientRegisterRequest request =
        new PatientRegisterRequest(
            email,
            password,
            name,
            phone,
            address,
            selectedDob,
            gender,
            bloodGroup,
            emergencyContact,
            securityAnswer);
    AuthApi authApi = ApiClient.api(AuthApi.class);
    ApiCallback.handle(
        authApi.registerPatient(request),
        body ->
            LoadingHelper.hide(
                binding.loadingIndicator,
                () -> {
                  binding.btnRegister.setEnabled(true);
                  Toast.makeText(
                          RegisterActivity.this,
                          body.message() + "\n" + body.remarks(),
                          Toast.LENGTH_LONG)
                      .show();
                  finish();
                }),
        (code, msg) ->
            LoadingHelper.hide(
                binding.loadingIndicator,
                () -> {
                  binding.btnRegister.setEnabled(true);
                  ApiErrorHandler.with(RegisterActivity.this)
                      .fallback("Registration failed. Email may already be in use.")
                      .build()
                      .run(code, msg);
                }));
  }
}
