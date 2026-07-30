package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.model.AvailabilityDay;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.DoctorProfile;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.data.remote.repository.DoctorRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class DoctorProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private LinearLayout availabilityContainer;

    // Existing model (keep for now)
    private DoctorProfile mockProfile;

    // NEW: Repository for backend API
    private DoctorRepository doctorRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Repository
        doctorRepository = new DoctorRepository();

        toolbar = findViewById(R.id.toolbar);
        availabilityContainer = findViewById(R.id.availabilityContainer);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        // Load mock data for missing fields
        mockProfile = DataProvider.getCurrentDoctorProfile();

        fetchProfileData();
    }

    private void fetchProfileData() {
        doctorRepository.getMyProfile().enqueue(new Callback<DoctorResponse>() {
            @Override
            public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DoctorResponse backend = response.body();
                    bindHeader(backend);
                    bindInfoGrid(backend);
                    bindAvailability();
                    bindStatistics(backend);
                } else {
                    android.util.Log.e("API_ERROR", "Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            android.util.Log.e("API_ERROR", "Body: " + response.errorBody().string());
                        }
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(DoctorProfileActivity.this, "Failed to fetch profile (Error " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DoctorResponse> call, Throwable t) {
                android.util.Log.e("API_ERROR", "Failure: " + t.getMessage(), t);
                Toast.makeText(DoctorProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindHeader(DoctorResponse backend){
        ((TextView)findViewById(R.id.txtDoctorName)).setText(backend.getName());
        ((TextView)findViewById(R.id.txtRole)).setText(backend.getSpeciality());
        ((TextView)findViewById(R.id.txtRegistrationBadge)).setText(mockProfile.getRegistrationNo());
        ((TextView)findViewById(R.id.txtQualification)).setText(mockProfile.getQualification());
        ((TextView)findViewById(R.id.txtExperience)).setText(backend.getYearsExperience() + " Years Experience");
        ((TextView)findViewById(R.id.txtPhoneHeader)).setText(backend.getPhone());
        ((TextView)findViewById(R.id.txtEmailHeader)).setText(mockProfile.getEmail());
        ((TextView)findViewById(R.id.txtHospitalName)).setText(backend.getDepartment());
        ((TextView)findViewById(R.id.txtHospitalRole)).setText(backend.getDepartment());
    }

    private void bindInfoGrid(DoctorResponse backend){
        setInfoCard(R.id.cardSpecialization, "SPECIALIZATION", backend.getSpeciality());
        setInfoCard(R.id.cardExperience, "EXPERIENCE", backend.getYearsExperience() + " Years");
        setInfoCard(R.id.cardQualification, "QUALIFICATION", mockProfile.getQualification());
        setInfoCard(R.id.cardRegistration, "REGISTRATION NO:", mockProfile.getRegistrationNo());
        setInfoCard(R.id.cardEmail, "EMAIL", mockProfile.getEmail());
        setInfoCard(R.id.cardPhone, "PHONE", backend.getPhone());
    }
    private void setInfoCard(int cardId, String title, String value){
        MaterialCardView card = findViewById(cardId);
        ((TextView)card.findViewById(R.id.lblTitle)).setText(title);
        ((TextView)card.findViewById(R.id.lblValue)).setText(value);
    }
    private void bindAvailability() {
        availabilityContainer.removeAllViews();
        for (AvailabilityDay a : mockProfile.getAvailability()) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setBackgroundColor(getColor(R.color.tertiary_container));
            item.setPadding(dp(14), dp(12), dp(14), dp(12));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginEnd(dp(8));
            item.setLayoutParams(lp);

            TextView day = new TextView(this);
            day.setText(a.getDay());
            day.setTextSize(13);
            day.setTypeface(null, android.graphics.Typeface.BOLD);
            day.setTextColor(getColor(R.color.on_tertiary_container));
            day.setGravity(Gravity.CENTER);

            TextView time = new TextView(this);
            time.setText(a.getStartTime() + "\n-\n" + a.getEndtime());
            time.setTextSize(11);
            time.setTextColor(getColor(R.color.on_tertiary_container));
            time.setGravity(Gravity.CENTER);

            item.addView(day);
            item.addView(time);
            availabilityContainer.addView(item);
        }
    }
    private void bindStatistics(DoctorResponse backend){
        setStat(R.id.statPatientsMonth, backend.getPatientsThisMonth() +"+","patients this month");
        setStat(R.id.statFeedback, mockProfile.getPositiveFeedbackPercent() +"%","positive feedback");
        setStat(R.id.statPatientsTotal, backend.getTotalPatients() +"+","Patients Total");
        setStat(R.id.statRating, mockProfile.getRating() +"+","Rating");
    }
    private void setStat(int cardId, String value, String label){
        MaterialCardView card= findViewById(cardId);
        ((TextView) card.findViewById(R.id.statValue)).setText(value);
        ((TextView) card.findViewById(R.id.statLabel)).setText(label);
    }
    private int dp(int v){
        return (int) (v*getResources().getDisplayMetrics().density);
    }

}