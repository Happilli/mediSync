package com.bca.medisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AuthApi;
import com.bca.medisync.doctor.DoctorHomeActivity;
import com.bca.medisync.data.remote.dto.login.LoginRequest;
import com.bca.medisync.data.remote.dto.login.LoginResponse;
import com.bca.medisync.patient.MainTabActivity;
import com.bca.medisync.patient.RegisterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
  private MaterialButton btnLogin;
  private ChipGroup chipGroupRole;
  private TextInputEditText etEmail, etPassword;
  private SessionManager sessionManager;
  private TextView goToRegister;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sessionManager = new SessionManager(this);

    if (sessionManager.isLoggedIn()) {
      Intent intent;
      if ("doctor".equalsIgnoreCase(sessionManager.getRole())) {
        intent = new Intent(MainActivity.this, DoctorHomeActivity.class);
      } else {
        intent = new Intent(MainActivity.this, MainTabActivity.class);
      }
      startActivity(intent);
      finish();
      return;
    }

    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.mainStuff),
        (v, i) -> {
          Insets systemBars = i.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return i;
        });

    sessionManager = new SessionManager(this);
    btnLogin = findViewById(R.id.btnLogin);
    chipGroupRole = findViewById(R.id.chipGroupRole);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    goToRegister = findViewById(R.id.GoToRegister);
    chipGroupRole.setOnCheckedStateChangeListener((group, checkedIds) -> updateRegisterLabel());
    updateRegisterLabel();
    btnLogin.setOnClickListener(
        v -> {
          attemptLogin();
        });
  }

  private void updateRegisterLabel() {
    boolean isDoctor = chipGroupRole.getCheckedChipId() == R.id.chipDoctor;
    if (isDoctor) {
      goToRegister.setText("Only hospitals can register\ndoctor accounts");
      goToRegister.setOnClickListener(null);
      goToRegister.setClickable(false);
    } else {
      goToRegister.setText("No Account?\nRegister");
      goToRegister.setOnClickListener(
          v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
      goToRegister.setClickable(true);
    }
  }

  public void attemptLogin() {
    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
    String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

    if (email.isEmpty()) {
      etEmail.setError("Email is required");
      return;
    }
    if (password.isEmpty()) {
      etPassword.setError("Password is required");
      return;
    }
    String selectedRole =
        chipGroupRole.getCheckedChipId() == R.id.chipDoctor ? "doctor" : "patient";
    btnLogin.setEnabled(false);

    AuthApi authApi = ApiClient.getRetrofit().create(AuthApi.class);
    authApi
        .login(new LoginRequest(email, password))
        .enqueue(
            new Callback<LoginResponse>() {
              @Override
              public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                  LoginResponse body = response.body();

                  if (!selectedRole.equalsIgnoreCase(body.getRole())) {
                    Toast.makeText(
                            MainActivity.this,
                            "This account is not registered as " + selectedRole,
                            Toast.LENGTH_LONG)
                        .show();
                    return;
                  }
                  sessionManager.saveSession(
                      body.getAccess_token(), body.getRole(), body.getEmail());

                  Intent intent;
                  if ("doctor".equalsIgnoreCase(body.getRole())) {
                    intent = new Intent(MainActivity.this, DoctorHomeActivity.class);
                  } else {
                    intent = new Intent(MainActivity.this, MainTabActivity.class);
                  }
                  startActivity(intent);
                  finish();
                } else {
                  Toast.makeText(
                          MainActivity.this,
                          "login failed: invalid credentials",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(
                        MainActivity.this, "network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }
}
