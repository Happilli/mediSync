package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.TimeSlotAdapter;
import com.bca.medisync.data.model.TimeSlot;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.TimeSlotResponse;
import com.bca.medisync.data.remote.dto.appointment.AppointmentCreateRequest;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookAppointmentActivity extends AppCompatActivity {
  private MaterialToolbar toolbar;
  private TextView txtDoctorName, txtDoctorSpeciality, txtDoctorInfo;
  private MaterialButton btnConfirm;
  private TextInputEditText etNotes;
  private RecyclerView rvTimeSlots;

  private TimeSlot selectedTimeSlot;

  // doctor info from DoctorActivity
  private String doctorName, doctorSpeciality, doctorInfo, doctorDepartment;
  private int doctorId = -1;
  private TextView txtNoSlots;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_book_appointment);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    setupToolbar();
    loadDoctorData();
    setupTimeSlots();
    setupConfirmButton();
  }

  private void initViews() {
    toolbar = findViewById(R.id.toolbar);
    txtDoctorName = findViewById(R.id.txtDoctorName);
    txtDoctorSpeciality = findViewById(R.id.txtDoctorSpeciality);
    txtDoctorInfo = findViewById(R.id.txtDoctorInfo);
    btnConfirm = findViewById(R.id.btnConfirm);
    btnConfirm.setEnabled(false);
    etNotes = findViewById(R.id.etNotes);
    rvTimeSlots = findViewById(R.id.rvTimeSlots);
    txtNoSlots = findViewById(R.id.txtNoSlots);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void loadDoctorData() {
    String doctorIdStr = getIntent().getStringExtra("doctor_id");
    doctorName = getIntent().getStringExtra("doctor_name");
    doctorSpeciality = getIntent().getStringExtra("doctor_speciality");
    doctorInfo = getIntent().getStringExtra("doctor_info");
    doctorDepartment = getIntent().getStringExtra("doctor_department");

    if (doctorIdStr != null) {
      try {
        doctorId = Integer.parseInt(doctorIdStr);
      } catch (NumberFormatException e) {
        Toast.makeText(this, "Invalid doctor reference", Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    } else {
      Toast.makeText(this, "Doctor not specified", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    if (doctorName != null) {
      txtDoctorName.setText(doctorName);
    }
    if (doctorSpeciality != null) {
      txtDoctorSpeciality.setText(doctorSpeciality);
    }
    if (doctorInfo != null) {
      txtDoctorInfo.setText(doctorInfo);
    }
  }

  private void setupTimeSlots() {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.getDoctorTimeslots(doctorId, true)
        .enqueue(
            new Callback<List<TimeSlotResponse>>() {
              @Override
              public void onResponse(
                  Call<List<TimeSlotResponse>> call, Response<List<TimeSlotResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  List<TimeSlot> slots = new ArrayList<>();
                  for (TimeSlotResponse r : response.body()) {
                    slots.add(mapToTimeSlot(r));
                  }
                  bindTimeSlots(slots);
                } else {
                  Toast.makeText(
                          BookAppointmentActivity.this,
                          "Failed to load available slots",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<TimeSlotResponse>> call, Throwable t) {
                Toast.makeText(
                        BookAppointmentActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindTimeSlots(List<TimeSlot> slots) {
    if (slots.isEmpty()) {
      rvTimeSlots.setVisibility(View.GONE);
      txtNoSlots.setVisibility(View.VISIBLE);
      btnConfirm.setEnabled(false);
      return;
    }
    rvTimeSlots.setVisibility(View.VISIBLE);
    txtNoSlots.setVisibility(View.GONE);
    btnConfirm.setEnabled(true);

    TimeSlotAdapter adapter = new TimeSlotAdapter(this, slots, slot -> selectedTimeSlot = slot);
    rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 3));
    rvTimeSlots.setAdapter(adapter);
  }

  private TimeSlot mapToTimeSlot(TimeSlotResponse r) {
    String displayTime = r.getAppointment_at();
    try {
      LocalDateTime dt = LocalDateTime.parse(r.getAppointment_at());
      displayTime = dt.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault()));
    } catch (Exception ignored) {
    }
    return new TimeSlot(r.getId(), r.getAppointment_at(), displayTime, r.isIs_available());
  }

  private void setupConfirmButton() {
    btnConfirm.setOnClickListener(
        v -> {
          if (selectedTimeSlot == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
          }
          String notes = etNotes.getText() != null ? etNotes.getText().toString() : "";
          btnConfirm.setEnabled(false);

          AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
          api.createAppointment(new AppointmentCreateRequest(selectedTimeSlot.getId(), notes))
              .enqueue(
                  new Callback<AppointmentResponse>() {
                    @Override
                    public void onResponse(
                        Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                      btnConfirm.setEnabled(true);
                      if (response.isSuccessful()) {
                        Toast.makeText(
                                BookAppointmentActivity.this,
                                "Appointment booked!",
                                Toast.LENGTH_LONG)
                            .show();
                        finish();
                      } else if (response.code() == 403) {
                        Toast.makeText(
                                BookAppointmentActivity.this,
                                "You need to be verified before booking appointments.",
                                Toast.LENGTH_LONG)
                            .show();
                      } else if (response.code() == 400) {
                        Toast.makeText(
                                BookAppointmentActivity.this,
                                "This slot is no longer available.",
                                Toast.LENGTH_LONG)
                            .show();
                        setupTimeSlots();
                      } else {
                        Toast.makeText(
                                BookAppointmentActivity.this, "Booking failed.", Toast.LENGTH_SHORT)
                            .show();
                      }
                    }

                    @Override
                    public void onFailure(Call<AppointmentResponse> call, Throwable t) {
                      btnConfirm.setEnabled(true);
                      Toast.makeText(
                              BookAppointmentActivity.this,
                              "Network error: " + t.getMessage(),
                              Toast.LENGTH_LONG)
                          .show();
                    }
                  });
        });
  }
}
