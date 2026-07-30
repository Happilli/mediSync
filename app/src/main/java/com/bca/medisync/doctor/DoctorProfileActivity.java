package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.model.AvailabilityDay;
import com.bca.medisync.data.model.DoctorProfile;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvAvailability;
    private BottomNavigationView bottomNav;
    private MaterialButton btnLogout;
    private DoctorProfile profile;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_profile);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        setupToolbar();
        setupBottomNav();
        setupLogout();
        loadDoctorProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvAvailability = findViewById(R.id.rvAvailability);
        bottomNav = findViewById(R.id.bottomNav);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Logout", (dialog, which) -> performLogout())
                    .show();
        });
    }

    private void performLogout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadDoctorProfile() {
        DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);

        api.getMyProfile().enqueue(new Callback<DoctorProfileResponse>() {
            @Override
            public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DoctorProfileResponse doctor = response.body();

                    List<AvailabilityDay> availabilityDays = new ArrayList<>();
                    if (doctor.getAvailability() != null) {
                        for (DoctorProfileResponse.AvailabilityDayResponse adr : doctor.getAvailability()) {
                            availabilityDays.add(new AvailabilityDay(adr.getDay(), adr.getStartTime(), adr.getEndTime()));
                        }
                    }

                    profile = new DoctorProfile(
                            doctor.getName(),
                            "Doctor",
                            "MBBS",
                            doctor.getLicenseNumber(),
                            doctor.getPhone(),
                            doctor.getEmail(),
                            doctor.getSpeciality(),
                            doctor.getYearsExperience(),
                            doctor.getHospitalName(),
                            doctor.getDepartment(),
                            availabilityDays,
                            doctor.getPatientsThisMonth(),
                            doctor.getTotalPatients(),
                            doctor.getPositiveFeedback(),
                            doctor.getRating()
                    );

                    bindHeader();
                    bindInfoGrid();
                    bindAvailability();
                    bindStatistics();
                } else {
                    Toast.makeText(DoctorProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                Toast.makeText(DoctorProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.doc_nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.doc_nav_dashboard) {
                intent = new Intent(this, DoctorHomeActivity.class);
            } else if (id == R.id.doc_nav_schedule) {
                intent = new Intent(this, ScheduleActivity.class);
            } else if (id == R.id.doc_nav_patients) {
                intent = new Intent(this, DoctorPatientsActivity.class);
            } else if (id == R.id.doc_nav_profile) {
                return true;
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void bindHeader() {
        ((TextView) findViewById(R.id.txtDoctorName)).setText(profile.getName());
        ((TextView) findViewById(R.id.txtRole)).setText(profile.getRole());
        ((TextView) findViewById(R.id.txtRegistrationBadge)).setText(profile.getRegistrationNo());
        ((TextView) findViewById(R.id.txtQualification)).setText(profile.getQualification());
        ((TextView) findViewById(R.id.txtExperience)).setText(profile.getExperienceYears() + " Years Experience");
        ((TextView) findViewById(R.id.txtPhoneHeader)).setText(profile.getPhone());
        ((TextView) findViewById(R.id.txtEmailHeader)).setText(profile.getEmail());
        ((TextView) findViewById(R.id.txtHospitalName)).setText(profile.getHospitalName());
        ((TextView) findViewById(R.id.txtHospitalRole)).setText(profile.getHospitalRole());
    }

    private void bindInfoGrid() {
        setInfoCard(R.id.cardSpecialization, "SPECIALIZATION", profile.getSpecialization());
        setInfoCard(R.id.cardExperience, "EXPERIENCE", profile.getExperienceYears() + " Years");
        setInfoCard(R.id.cardQualification, "QUALIFICATION", profile.getQualification());
        setInfoCard(R.id.cardRegistration, "REGISTRATION NO", profile.getRegistrationNo());
        setInfoCard(R.id.cardEmail, "EMAIL", profile.getEmail());
        setInfoCard(R.id.cardPhone, "PHONE", profile.getPhone());
    }

    private void setInfoCard(int cardId, String title, String value) {
        MaterialCardView card = findViewById(cardId);
        ((TextView) card.findViewById(R.id.lblTitle)).setText(title);
        ((TextView) card.findViewById(R.id.lblValue)).setText(value);
    }

    private void bindAvailability() {
        List<AvailabilityDay> list = profile.getAvailability();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (list == null || list.isEmpty()) ? 2 : 1;
            }
        });

        rvAvailability.setLayoutManager(layoutManager);
        rvAvailability.setAdapter(new AvailabilityAdapter(list));

        rvAvailability.setAlpha(0f);
        rvAvailability.animate().alpha(1f).setDuration(600).start();
    }

    private class AvailabilityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_EMPTY = 0;
        private static final int TYPE_ITEM = 1;
        private final List<AvailabilityDay> items;

        AvailabilityAdapter(List<AvailabilityDay> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return (items == null || items.isEmpty()) ? TYPE_EMPTY : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_EMPTY) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_availability_empty, parent, false);
                return new RecyclerView.ViewHolder(v) {
                };
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_availability, parent, false);
            return new ItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder) {
                AvailabilityDay item = items.get(position);
                ItemViewHolder h = (ItemViewHolder) holder;
                h.txtDay.setText(item.getDay());
                h.txtTime.setText(item.getStartTime() + " - " + item.getEndtime());
            }
        }

        @Override
        public int getItemCount() {
            return (items == null || items.isEmpty()) ? 1 : items.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView txtDay, txtTime;

            ItemViewHolder(View v) {
                super(v);
                txtDay = v.findViewById(R.id.txtDayName);
                txtTime = v.findViewById(R.id.txtTimeRange);
            }
        }
    }

    private void bindStatistics() {
        setStat(R.id.statPatientsMonth, profile.getPatientsThisMonth() + "+", "Patients This Month");
        setStat(R.id.statFeedback, profile.getPositiveFeedbackPercent() + "%", "Positive Feedback");
        setStat(R.id.statPatientsTotal, profile.getTotalPatients() + "+", "Total Patients");
        setStat(R.id.statRating, String.valueOf(profile.getRating()), "Rating");
    }

    private void setStat(int cardId, String value, String label) {
        MaterialCardView card = findViewById(cardId);
        ((TextView) card.findViewById(R.id.statValue)).setText(value);
        ((TextView) card.findViewById(R.id.statLabel)).setText(label);
    }

}
