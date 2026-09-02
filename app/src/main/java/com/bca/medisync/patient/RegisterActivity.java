package com.bca.medisync.patient;

import android.os.Bundle;
import android.widget.RadioGroup;
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
import com.bca.medisync.util.LoadingHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

  private TextInputEditText etName,
      etEmail,
      etPassword,
      etConfirmPassword,
      etPhone,
      etAddress,
      etEmergencyContact,
      etSecurityAnswer;
  private RadioGroup radioGroupGender;
  private MaterialButtonToggleGroup toggleBloodGroupRow1, toggleBloodGroupRow2;
  private MaterialButton btnSelectDob;
  private ExtendedFloatingActionButton btnRegister;
  private LoadingIndicator loadingIndicator;
  private android.widget.TextView txtSelectedDob, txtPasswordMismatch, goToLogin;

  private String selectedDob = "";
  private boolean passwordsMismatched = false;

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
    setupDobPicker();
    setupBloodGroupToggles();
    setupPasswordMatchWatcher();
    setupListeners();
  }

  private void initViews() {
    etName = findViewById(R.id.etName);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    etConfirmPassword = findViewById(R.id.etConfirmPassword);
    etPhone = findViewById(R.id.etPhone);
    etAddress = findViewById(R.id.etAddress);
    etEmergencyContact = findViewById(R.id.etEmergencyContact);
    radioGroupGender = findViewById(R.id.radioGroupGender);
    toggleBloodGroupRow1 = findViewById(R.id.toggleBloodGroupRow1);
    toggleBloodGroupRow2 = findViewById(R.id.toggleBloodGroupRow2);
    btnSelectDob = findViewById(R.id.btnSelectDob);
    txtSelectedDob = findViewById(R.id.txtSelectedDob);
    txtPasswordMismatch = findViewById(R.id.txtPasswordMismatch);
    btnRegister = findViewById(R.id.btnRegister);
    loadingIndicator = findViewById(R.id.loadingIndicator);
    goToLogin = findViewById(R.id.GoToLogin);
    etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
  }

  private void setupBloodGroupToggles() {
    toggleBloodGroupRow1.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (isChecked) toggleBloodGroupRow2.clearChecked();
        });
    toggleBloodGroupRow2.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (isChecked) toggleBloodGroupRow1.clearChecked();
        });
  }

  private void setupPasswordMatchWatcher() {
    etConfirmPassword.setOnFocusChangeListener(
        (v, hasFocus) -> {
          if (!hasFocus) checkPasswordsMatch();
        });
    etPassword.setOnFocusChangeListener(
        (v, hasFocus) -> {
          if (!hasFocus && !textOf(etConfirmPassword).isEmpty()) checkPasswordsMatch();
        });
  }

  private void checkPasswordsMatch() {
    String password = textOf(etPassword);
    String confirmPassword = textOf(etConfirmPassword);

    if (confirmPassword.isEmpty() || password.isEmpty()) {
      passwordsMismatched = false;
      txtPasswordMismatch.setVisibility(android.view.View.GONE);
      return;
    }

    if (!password.equals(confirmPassword)) {
      passwordsMismatched = true;
      txtPasswordMismatch.setVisibility(android.view.View.VISIBLE);
    } else {
      passwordsMismatched = false;
      txtPasswordMismatch.setVisibility(android.view.View.GONE);
    }
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

  private String getSelectedGender() {
    int checkedId = radioGroupGender.getCheckedRadioButtonId();
    if (checkedId == R.id.radioMale) return "male";
    if (checkedId == R.id.radioFemale) return "female";
    if (checkedId == R.id.radioOther) return "other";
    return "";
  }

  private String getSelectedBloodGroup() {
    int checkedId1 = toggleBloodGroupRow1.getCheckedButtonId();
    if (checkedId1 != android.view.View.NO_ID) {
      MaterialButton btn = findViewById(checkedId1);
      return btn.getText().toString().trim().toUpperCase(Locale.ROOT);
    }
    int checkedId2 = toggleBloodGroupRow2.getCheckedButtonId();
    if (checkedId2 != android.view.View.NO_ID) {
      MaterialButton btn = findViewById(checkedId2);
      return btn.getText().toString().trim().toUpperCase(Locale.ROOT);
    }
    return "";
  }

  private void attemptRegister() {
    String name = textOf(etName);
    String email = textOf(etEmail);
    String password = textOf(etPassword);
    String confirmPassword = textOf(etConfirmPassword);
    String phone = textOf(etPhone);
    String address = textOf(etAddress);
    String emergencyContact = textOf(etEmergencyContact);
    String gender = getSelectedGender();
    String bloodGroup = getSelectedBloodGroup();
    String securityAnswer = etSecurityAnswer.getText().toString().trim();

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
    if (confirmPassword.isEmpty()) {
      etConfirmPassword.setError("Please confirm your password");
      return;
    }
    if (passwordsMismatched || !password.equals(confirmPassword)) {
      txtPasswordMismatch.setVisibility(android.view.View.VISIBLE);
      Toast.makeText(this, "Passwords don't match. Fix before proceeding.", Toast.LENGTH_SHORT)
          .show();
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
    if (securityAnswer.isEmpty()) {
      Toast.makeText(this, "Please answer the security Question... ", Toast.LENGTH_SHORT).show();
      return;
    }
    if (emergencyContact.isEmpty()) {
      etEmergencyContact.setError("Emergency contact is required");
      return;
    }

    btnRegister.setEnabled(false);
    LoadingHelper.show(loadingIndicator);

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
                loadingIndicator,
                () -> {
                  btnRegister.setEnabled(true);
                  Toast.makeText(
                          RegisterActivity.this,
                          body.getMessage() + "\n" + body.getRemarks(),
                          Toast.LENGTH_LONG)
                      .show();
                  finish();
                }),
        (code, msg) ->
            LoadingHelper.hide(
                loadingIndicator,
                () -> {
                  btnRegister.setEnabled(true);
                  if (code == -1) {
                    Toast.makeText(
                            RegisterActivity.this, "Network error: " + msg, Toast.LENGTH_LONG)
                        .show();
                  } else {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Registration failed. Email may already be in use.",
                            Toast.LENGTH_LONG)
                        .show();
                  }
                }));
  }

  private String textOf(TextInputEditText et) {
    return et.getText() != null ? et.getText().toString().trim() : "";
  }
}
