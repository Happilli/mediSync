package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.DoctorPatientAdapter;
import com.bca.medisync.data.remote.dto.patient.DoctorPatientResponse;
import com.bca.medisync.data.repository.PatientRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorPatientsActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG_FLOW";
    private MaterialToolbar toolbar;
    private RecyclerView rvPatients;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private SearchView searchView;
    private TextView txtPatientCount;
    private BottomNavigationView bottomNav;

    private DoctorPatientAdapter adapter;
    private PatientRepository repository;
    private final List<DoctorPatientResponse> patientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_patients);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupBottomNav();
        setupSearch();

        repository = new PatientRepository();
        loadPatients();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvPatients = findViewById(R.id.rvPatients);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        searchView = findViewById(R.id.searchView);
        txtPatientCount = findViewById(R.id.txtPatientCount);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.doc_nav_patients);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.doc_nav_dashboard) {
                intent = new Intent(this, DoctorHomeActivity.class);
            } else if (id == R.id.doc_nav_schedule) {
                intent = new Intent(this, ScheduleActivity.class);
            } else if (id == R.id.doc_nav_patients) {
                return true;
            } else if (id == R.id.doc_nav_profile) {
                intent = new Intent(this, DoctorProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorPatientAdapter(this, patientList, patient -> {
            Intent intent = new Intent(this, DoctorPatientDetailActivity.class);
            intent.putExtra("patient", patient);
            startActivity(intent);
        });
        rvPatients.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup complete. Adapter attached.");
    }

    private void loadPatients() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        Log.d(TAG, "Requesting patients from repository...");
        repository.getDoctorPatients(new Callback<List<DoctorPatientResponse>>() {
            @Override
            public void onResponse(Call<List<DoctorPatientResponse>> call, Response<List<DoctorPatientResponse>> response) {
                progressBar.setVisibility(View.GONE);
                
                Log.d(TAG, "Step 1: HTTP Code: " + response.code());
                Log.d(TAG, "Step 1: Request URL: " + call.request().url());
                Log.d(TAG, "Step 1: Response Headers: " + response.headers());

                if (response.isSuccessful() && response.body() != null) {
                    List<DoctorPatientResponse> receivedList = response.body();
                    Log.d(TAG, "Step 2: Parse Success. Items received: " + receivedList.size());
                    Log.d(TAG, "Step 2: Raw Body (via Gson): " + new Gson().toJson(receivedList));

                    Log.d(TAG, "Step 4: Activity patientList size BEFORE clear: " + patientList.size());
                    patientList.clear();
                    patientList.addAll(receivedList);
                    Log.d(TAG, "Step 4: Activity patientList size AFTER addAll: " + patientList.size());

                    adapter.updateList(new ArrayList<>(patientList));
                    
                    txtPatientCount.setText("Showing " + patientList.size() + " patients");

                    if (patientList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Step 4: Empty list. Showing empty state.");
                    } else {
                        Log.d(TAG, "Step 4: Data ready. txtPatientCount: " + txtPatientCount.getText());
                    }
                } else {
                    Log.e(TAG, "Step 3: Response unsuccessful or null body.");
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Step 3: Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Step 3: Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(DoctorPatientsActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DoctorPatientResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Step 3: Repository failure callback triggered: " + t.getMessage());
                Toast.makeText(DoctorPatientsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}