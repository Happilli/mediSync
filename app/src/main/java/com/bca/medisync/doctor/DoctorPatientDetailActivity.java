package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.dto.patient.DoctorPatientResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class DoctorPatientDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView imgProfile;
    private TextView txtName;
    private View rowGender, rowBloodGroup, rowPhone, rowEmergencyContact;
    private View cardMedicalHistory, cardMedications;
    private MaterialButton btnStartConsultation;

    private DoctorPatientResponse currentPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_patient_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        setupToolbar();

        currentPatient = (DoctorPatientResponse) getIntent().getSerializableExtra("patient");
        if (currentPatient != null) {
            bindPatientData(currentPatient);
        }

        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgProfile = findViewById(R.id.imgProfile);
        txtName = findViewById(R.id.txtName);

        rowGender = findViewById(R.id.rowGender);
        rowBloodGroup = findViewById(R.id.rowBloodGroup);
        rowPhone = findViewById(R.id.rowPhone);
        rowEmergencyContact = findViewById(R.id.rowEmergencyContact);

        cardMedicalHistory = findViewById(R.id.cardMedicalHistory);
        cardMedications = findViewById(R.id.cardMedications);
        btnStartConsultation = findViewById(R.id.btnStartConsultation);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindPatientData(DoctorPatientResponse patient) {
        txtName.setText(patient.getName());
        
        setupInfoRow(rowGender, R.drawable.ic_nav_profile, "Gender", capitalize(patient.getGender()));
        setupInfoRow(rowBloodGroup, R.drawable.bg_status_badge, "Blood Group", patient.getBlood_group());
        setupInfoRow(rowPhone, R.drawable.phone, "Phone Number", patient.getPhone());
        setupInfoRow(rowEmergencyContact, R.drawable.emergency, "Emergency Contact", patient.getEmergency_contact());

        if (patient.getProfile_pic_url() != null && !patient.getProfile_pic_url().isEmpty()) {
            String imageUrl = ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + patient.getProfile_pic_url();
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .centerCrop()
                    .into(imgProfile);
        }
    }

    private void setupInfoRow(View row, int iconRes, String label, String value) {
        ((ImageView) row.findViewById(R.id.imgRowIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.txtRowLabel)).setText(label);
        ((TextView) row.findViewById(R.id.txtRowValue)).setText(value);
    }

    private void setupListeners() {
        cardMedicalHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, MedicalHistoryActivity.class);
            intent.putExtra("patient_name", currentPatient.getName());
            startActivity(intent);
        });

        cardMedications.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorMedicationsActivity.class);
            intent.putExtra("patient_name", currentPatient.getName());
            startActivity(intent);
        });

        btnStartConsultation.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsultationActivity.class);
            intent.putExtra("patient_name", currentPatient.getName());
            startActivity(intent);
        });
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}