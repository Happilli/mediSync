package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.DoctorAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Doctor;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorActivity extends AppCompatActivity {
  private RecyclerView rvDoctors;
  private MaterialToolbar toolbar;
  private TextInputEditText etSearch;
  private DoctorAdapter adapter;

  private Integer filterHospitalId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_doctor);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    setupToolbar();
    setupRecyclerView();
    setupSearch();
    loadDoctors(null);
  }

  private void initViews() {
    rvDoctors = findViewById(R.id.rvDoctors);
    toolbar = findViewById(R.id.toolbar);
    etSearch = findViewById(R.id.etSearch);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
  }

  private void setupRecyclerView() {
    List<Doctor> doctors = DataProvider.getDoctors();
    adapter =
        new DoctorAdapter(
            this,
            doctors,
            doctor -> {
              Intent intent = new Intent(DoctorActivity.this, BookAppointmentActivity.class);
              intent.putExtra("doctor_id", doctor.getId());
              intent.putExtra("doctor_name", doctor.getName());
              intent.putExtra("doctor_speciality", doctor.getSpeciality());
              intent.putExtra("doctor_info", doctor.getInfo());
              intent.putExtra("doctor_department", doctor.getDepartment());
              startActivity(intent);
            });
    rvDoctors.setLayoutManager(new LinearLayoutManager(this));
    rvDoctors.setAdapter(adapter);
  }

  private void loadDoctors(String search) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.getDoctors(filterHospitalId, null, null, search)
        .enqueue(
            new Callback<List<DoctorResponse>>() {
              @Override
              public void onResponse(
                  Call<List<DoctorResponse>> call, Response<List<DoctorResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  List<Doctor> doctors = new ArrayList<>();
                  for (DoctorResponse r : response.body()) {
                    doctors.add(mapToDoctor(r));
                  }
                  adapter.updateData(doctors);
                } else {
                  Toast.makeText(DoctorActivity.this, "failed to load doctors", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<DoctorResponse>> call, Throwable t) {
                Toast.makeText(
                        DoctorActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private Doctor mapToDoctor(DoctorResponse r) {
    String info =
        r.getYears_experience() != null
            ? r.getYears_experience() + "+ Years Exp"
            : (r.getBio() != null ? r.getBio() : "");
    String imageUrl =
        r.getProfile_pic_url() != null
            ? ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + r.getProfile_pic_url()
            : null;
    return new Doctor(
        String.valueOf(r.getId()),
        r.getName(),
        r.getSpeciality(),
        info,
        r.getDepartment(),
        r.getPhone(),
        imageUrl);
  }

  private void setupSearch() {
    etSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filter(s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }
}
