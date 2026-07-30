package com.bca.medisync.doctor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.repository.AppointmentRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentDetailActivity extends AppCompatActivity {

    private TextView txtPatientName, txtStatus, txtDoctorName, txtDepartment, txtSpeciality, txtDate, txtTime, txtNotes;
    private MaterialButton btnConfirm, btnComplete, btnCancel;
    private Appointment appointment;
    private AppointmentRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        repository = new AppointmentRepository();
        initViews();

        appointment = (Appointment) getIntent().getSerializableExtra("appointment");

        if (appointment != null) {
            bindData();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        txtPatientName = findViewById(R.id.txtPatientName);
        txtStatus = findViewById(R.id.txtStatus);
        txtDoctorName = findViewById(R.id.txtDoctorName);
        txtDepartment = findViewById(R.id.txtDepartment);
        txtSpeciality = findViewById(R.id.txtSpeciality);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtNotes = findViewById(R.id.txtNotes);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);

        btnConfirm.setOnClickListener(v -> updateStatus("confirmed"));
        btnComplete.setOnClickListener(v -> updateStatus("completed"));
        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes", (dialog, which) -> updateStatus("cancelled"))
                .setNegativeButton("No", null)
                .show();
    }

    private void bindData() {
        txtPatientName.setText(appointment.getPatientName());
        txtStatus.setText(appointment.getStatus());
        txtDoctorName.setText(appointment.getDoctorName());
        txtDepartment.setText(appointment.getDepartment());
        txtSpeciality.setText(appointment.getSpeciality());
        txtDate.setText(appointment.getDate());
        txtTime.setText(appointment.getTime());
        txtNotes.setText(appointment.getNotes() == null || appointment.getNotes().isEmpty() ? "No additional notes provided." : appointment.getNotes());

        updateStatusUI();
    }

    private void updateStatusUI() {
        String status = appointment.getStatus() != null ? appointment.getStatus().toLowerCase() : "";

        // Apply status colors
        switch (status) {
            case "confirmed":
            case "completed":
                txtStatus.setTextColor(getColor(R.color.tertiary));
                txtStatus.setBackgroundTintList(getColorStateList(R.color.tertiary_container));
                break;
            case "pending":
                txtStatus.setTextColor(getColor(R.color.secondary));
                txtStatus.setBackgroundTintList(getColorStateList(R.color.secondary_container));
                break;
            case "cancelled":
                txtStatus.setTextColor(getColor(R.color.error));
                txtStatus.setBackgroundTintList(getColorStateList(R.color.error_container));
                break;
            default:
                txtStatus.setTextColor(getColor(R.color.primary));
                txtStatus.setBackgroundTintList(getColorStateList(R.color.primary_container));
                break;
        }

        // Button visibility rules
        btnConfirm.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        if ("pending".equals(status)) {
            btnConfirm.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if ("confirmed".equals(status)) {
            btnComplete.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatus(String newStatus) {
        repository.updateAppointmentStatus(appointment.getId(), newStatus, new Callback<AppointmentResponse>() {
            @Override
            public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                // Log request details
                Log.d("AppointmentStatus", "Request URL: " + call.request().url());
                if (call.request().body() != null) {
                    try {
                        Buffer buffer = new Buffer();
                        call.request().body().writeTo(buffer);
                        Log.d("AppointmentStatus", "Request Body: " + buffer.readUtf8());
                    } catch (Exception e) {
                        Log.d("AppointmentStatus", "Could not read request body");
                    }
                }

                Log.d("AppointmentStatus", "HTTP Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String toastMessage = "Appointment " + newStatus;
                    if ("confirmed".equals(newStatus)) toastMessage = "Appointment confirmed successfully.";
                    else if ("completed".equals(newStatus)) toastMessage = "Appointment completed successfully.";
                    else if ("cancelled".equals(newStatus)) toastMessage = "Appointment cancelled successfully.";
                    
                    Toast.makeText(AppointmentDetailActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    
                    // Refresh Appointment object and UI
                    appointment = response.body().toAppointment();
                    bindData();
                    
                    // Notify previous activity
                    setResult(RESULT_OK);
                } else {
                    String errorMessage = "Failed to update appointment status.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            Log.d("AppointmentStatus", "Error Body: " + errorBodyString);
                            JSONObject jsonObject = new JSONObject(errorBodyString);
                            if (jsonObject.has("detail")) {
                                errorMessage = jsonObject.getString("detail");
                            }
                        }
                    } catch (Exception e) {
                        Log.d("AppointmentStatus", "Error parsing error body: " + e.getMessage());
                    }
                    Toast.makeText(AppointmentDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentResponse> call, Throwable t) {
                Log.d("AppointmentStatus", "Request URL: " + call.request().url());
                Log.d("AppointmentStatus", "Network Error: " + t.getMessage());
                Toast.makeText(AppointmentDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
