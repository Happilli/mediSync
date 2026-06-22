package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtAddress, txtAge, txtBloodGroup, txtGender;
    private TextView txtEmail, txtPhone, txtDob, txtAddressDetail, txtEmergencyContact;
    private MaterialSwitch switchNotifications;

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
        loadPatientData();
        setupListeners();
    }
    private void initViews(){
        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtAge = findViewById(R.id.txtAge);
        txtBloodGroup = findViewById(R.id.txtBloodGroup);
        txtGender = findViewById(R.id.txtGender);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtDob = findViewById(R.id.txtDob);
        txtAddressDetail= findViewById(R.id.txtAddressDetail);
        txtEmergencyContact = findViewById(R.id.txtEmergencyContact);
        switchNotifications = findViewById(R.id.switchNotifications);
    }

    private void loadPatientData(){
        Patient patient = DataProvider.getCurrentPatient();
        txtName.setText(patient.getName());
        txtAddress.setText(patient.getAddress());
        txtBloodGroup.setText(patient.getBloodGroup());
        txtGender.setText(patient.getGender());
        txtEmail.setText(patient.getEmail());
        txtPhone.setText(patient.getPhone());
        txtDob.setText(patient.getDateOfBirth());
        txtAddressDetail.setText(patient.getAddress());
        txtEmergencyContact.setText(patient.getEmergencyContact());

        txtAge.setText(calculateAge(patient.getDateOfBirth()));
        android.util.Log.d("PROFILE", "Name: " + DataProvider.getCurrentPatient().getName());
    }
    private String calculateAge(String dob){
        try {
            String[] parts = dob.split("-");
            int birthYear = Integer.parseInt(parts[0]);
            int currentYear = java.util.Calendar.getInstance().get(Calendar.YEAR);
            return String.valueOf(currentYear - birthYear);
        }catch (Exception e){
            return "--";
        }
    }
    private void setupListeners(){
        findViewById(R.id.btnEditProfile).setOnClickListener(v->{

        });

        findViewById(R.id.rowPrivacyPolicy).setOnClickListener(v->{

        });
        findViewById(R.id.rowTerms).setOnClickListener(v->{

        });
        findViewById(R.id.rowHelp).setOnClickListener(v->{

        });

        findViewById(R.id.btnLogout).setOnClickListener(v->{
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

    }

}