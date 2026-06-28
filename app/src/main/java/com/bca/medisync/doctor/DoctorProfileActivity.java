package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.model.AvailabilityDay;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.DoctorProfile;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class DoctorProfileActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private LinearLayout availabilityContainer;
    private DoctorProfile profile;

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

        toolbar = findViewById(R.id.toolbar);
        availabilityContainer = findViewById(R.id.availabilityContainer);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
        profile = DataProvider.getCurrentDoctorProfile();
        bindHeader();
        bindInfoGrid();
        bindAvailability();
        bindStatistics();
    }
    private void bindHeader(){
        ((TextView)findViewById(R.id.txtDoctorName)).setText(profile.getName());
        ((TextView)findViewById(R.id.txtRole)).setText(profile.getRole());
        ((TextView)findViewById(R.id.txtRegistrationBadge)).setText(profile.getRegistrationNo());
        ((TextView)findViewById(R.id.txtQualification)).setText(profile.getQualification());
        ((TextView)findViewById(R.id.txtExperience)).setText(profile.getExperienceYears() + " Years Experience");
        ((TextView)findViewById(R.id.txtPhoneHeader)).setText(profile.getPhone());
        ((TextView)findViewById(R.id.txtEmailHeader)).setText(profile.getEmail());
        ((TextView)findViewById(R.id.txtHospitalName)).setText(profile.getHospitalRole());
        ((TextView)findViewById(R.id.txtHospitalRole)).setText(profile.getHospitalRole());
    }
    private void bindInfoGrid(){
        setInfoCard(R.id.cardSpecialization, "SPECIALIZATION", profile.getSpecialization());
        setInfoCard(R.id.cardExperience, "EXPERIENCE", profile.getSpecialization());
        setInfoCard(R.id.cardQualification, "QUALIFICATION", profile.getQualification());
        setInfoCard(R.id.cardRegistration, "REGISTRATION NO:", profile.getRegistrationNo());
        setInfoCard(R.id.cardEmail, "EMAIL", profile.getEmail());
        setInfoCard(R.id.cardPhone, "PHONE", profile.getPhone());
    }
    private void setInfoCard(int cardId, String title, String value){
        MaterialCardView card = findViewById(cardId);
        ((TextView)card.findViewById(R.id.lblTitle)).setText(title);
        ((TextView)card.findViewById(R.id.lblValue)).setText(value);
    }
    private void bindAvailability() {
        for (AvailabilityDay a : profile.getAvailability()) {
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
    private void bindStatistics(){
        setStat(R.id.statPatientsMonth, profile.getPatientsThisMonth() +"+","patients this month");
        setStat(R.id.statFeedback, profile.getPositiveFeedbackPercent() +"%","positive feedback");
        setStat(R.id.statPatientsTotal, profile.getTotalPatients() +"+","Patients Total");
        setStat(R.id.statRating, profile.getRating() +"+","Rating");
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