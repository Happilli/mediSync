package com.bca.medisync;

import android.annotation.SuppressLint;
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
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.NotificationSocketHolder;
import com.bca.medisync.data.remote.NotificationSocketManager;
import com.bca.medisync.data.remote.api.AuthApi;
import com.bca.medisync.data.remote.dto.login.LoginRequest;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.doctor.DoctorTabActivity;
import com.bca.medisync.patient.MainTabActivity;
import com.bca.medisync.patient.RegisterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
  private MaterialButton btnLogin;
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
        intent = new Intent(MainActivity.this, DoctorTabActivity.class);
      } else {
        intent = new Intent(MainActivity.this, MainTabActivity.class);
      }
      NotificationSocketHolder.get().connect(sessionManager.getToken(), globalNotificationListener);
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
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    goToRegister = findViewById(R.id.GoToRegister);
    goToRegister.setText("No Account?\nRegister");
    goToRegister.setOnClickListener(
        v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
    btnLogin.setOnClickListener(v -> attemptLogin());
    findViewById(R.id.txtForgotPassword)
        .setOnClickListener(
            v -> startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class)));
  }

  private final NotificationSocketManager.Listener globalNotificationListener =
      new NotificationSocketManager.Listener() {
        @Override
        public void onSocketClosed() {}

        @Override
        public void onNotification(NotificationResponse notification) {
          NotificationCenter.get().broadcast(notification);
          showSystemNotification(notification);
        }
      };

  @SuppressLint("MissingPermission")
  private void showSystemNotification(NotificationResponse n) {
    // unchanged
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
    btnLogin.setEnabled(false);

    AuthApi authApi = ApiClient.getRetrofit().create(AuthApi.class);
    ApiCallback.handle(
        authApi.login(new LoginRequest(email, password)),
        body -> {
          btnLogin.setEnabled(true);
          sessionManager.saveSession(body.getAccess_token(), body.getRole(), body.getEmail());
          NotificationSocketHolder.get()
              .connect(sessionManager.getToken(), globalNotificationListener);

          Intent intent;
          if ("doctor".equalsIgnoreCase(body.getRole())) {
            intent = new Intent(MainActivity.this, DoctorTabActivity.class);
          } else {
            intent = new Intent(MainActivity.this, MainTabActivity.class);
          }
          startActivity(intent);
          finish();
        },
        (code, msg) -> {
          btnLogin.setEnabled(true);
          if (code == -1) {
            Toast.makeText(MainActivity.this, "network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(
                    MainActivity.this, "login failed: invalid credentials", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
