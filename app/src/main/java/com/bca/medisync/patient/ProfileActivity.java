package com.bca.medisync.patient;

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
import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Patient;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtAddress, txtEmergencyContact;
    private View statAge, statBloodGroup, statGender;
    private View rowEmail, rowPhone, rowDob, rowAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupSettingsRows();
        loadPatientData();
        setupListeners();
    }

    private void initViews() {
        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtEmergencyContact = findViewById(R.id.txtEmergencyContact);

        statAge = findViewById(R.id.statAge);
        statBloodGroup = findViewById(R.id.statBloodGroup);
        statGender = findViewById(R.id.statGender);

        rowEmail = findViewById(R.id.rowEmail);
        rowPhone = findViewById(R.id.rowPhone);
        rowDob = findViewById(R.id.rowDob);
        rowAddress = findViewById(R.id.rowAddress);

        switchNotifications = findViewById(R.id.switchNotifications);
    }

    private void bindStat(View statView, String value, String label) {
        ((TextView) statView.findViewById(R.id.txtStatValue)).setText(value);
        ((TextView) statView.findViewById(R.id.txtStatLabel)).setText(label);
    }

    private void bindRow(View rowView, int iconRes, String label, String value) {
        ((ImageView) rowView.findViewById(R.id.imgRowIcon)).setImageResource(iconRes);
        ((TextView) rowView.findViewById(R.id.txtRowLabel)).setText(label);
        ((TextView) rowView.findViewById(R.id.txtRowValue)).setText(value);
    }
    private void setupSettingsRows() {
        setRowLabel(R.id.rowPrivacyPolicy, "Privacy Policy");
        setRowLabel(R.id.rowTerms, "Terms and Conditions");
        setRowLabel(R.id.rowHelp, "Help and Support");
    }

    private void setRowLabel(int rowId, String label) {
        ((TextView) findViewById(rowId).findViewById(R.id.txtSettingsLabel)).setText(label);
    }
    private void loadPatientData() {
        Patient patient = DataProvider.getCurrentPatient();

        txtName.setText(patient.getName());
        txtAddress.setText(patient.getAddress());
        txtEmergencyContact.setText(patient.getEmergencyContact());

        bindStat(statAge, calculateAge(patient.getDateOfBirth()), "Age");
        bindStat(statBloodGroup, patient.getBloodGroup(), "Blood");
        bindStat(statGender, patient.getGender(), "Gender");

        bindRow(rowEmail, R.drawable.email, "Email", patient.getEmail());
        bindRow(rowPhone, R.drawable.phone, "Phone", patient.getPhone());
        bindRow(rowDob, R.drawable.birthdate, "Date of Birth", patient.getDateOfBirth());
        bindRow(rowAddress, R.drawable.location, "Address", patient.getAddress());
    }

    private String calculateAge(String dob) {
        try {
            String[] parts = dob.split("-");
            int birthYear = Integer.parseInt(parts[0]);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return String.valueOf(currentYear - birthYear);
        } catch (Exception e) {
            return "--";
        }
    }

    private void setupListeners() {
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
        });
        findViewById(R.id.rowPrivacyPolicy).setOnClickListener(v -> {
        });
        findViewById(R.id.rowTerms).setOnClickListener(v -> {
        });
        findViewById(R.id.rowHelp).setOnClickListener(v -> {
        });
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}