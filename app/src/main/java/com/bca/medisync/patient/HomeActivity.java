package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.DashboardAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Patient;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView rvDashboard;
    private BottomNavigationView  bottomNav;

    private TextView txtPatientName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        txtPatientName = findViewById(R.id.txtPatientName);
        rvDashboard=findViewById(R.id.rvDashboard);
        bottomNav=findViewById(R.id.bottomNav);

       //setting patient name
        Patient patient = DataProvider.getCurrentPatient();
        txtPatientName.setText(patient.getName());

        setupDashboard();
        setupBottomNav();
        setUpAppointmentCard();
    }


    private void setupDashboard(){
        List<String> titles = Arrays.asList(
                "Appointments",
                "Prescriptions",
                "Medications",
                "My Profile",
                "Health Records",
                "Hospitals"
        );
        List<Integer> icons = Arrays.asList(
                R.drawable.ic_nav_calendar,
                R.drawable.edit,
                R.drawable.ic_nav_medicine,
                R.drawable.ic_nav_profile,
                R.drawable.record,
                R.drawable.hospital
        );

        DashboardAdapter adapter = new DashboardAdapter(this, titles, icons, positons -> {
            switch (positons){
                case 0:
                    startActivity(new Intent(HomeActivity.this, AppointmentActivity.class));
                    break;
                case 1:
                    break;
                case 2:
                    startActivity(new Intent(HomeActivity.this, MedicationActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    break;
                case 4:
                    break;
                case 5:
                    startActivity(new Intent(HomeActivity.this, HospitalActivity.class));
                    break;
            }
        });
        rvDashboard.setLayoutManager(new GridLayoutManager(this, 3));
        rvDashboard.setAdapter(adapter);
    }


    private void setupBottomNav(){
        bottomNav.setOnItemSelectedListener(item->{
            int id = item.getItemId();
            if (id== R.id.nav_home){
                return true;
            } else if (id == R.id.nav_appointments) {
                startActivity(new Intent(HomeActivity.this, AppointmentActivity.class));
                return true;
            } else if (id == R.id.nav_medications) {
                startActivity(new Intent(HomeActivity.this, MedicationActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setUpAppointmentCard(){
        List<Appointment> appointments  = DataProvider.getAppointments();
        if (!appointments.isEmpty()){
            Appointment next = appointments.get(0);
            TextView txtDoctor = findViewById(R.id.txtAppointmentDoctor);
            TextView txtSpeciality = findViewById(R.id.txtAppointmentSpeciality);
            TextView txtDate  = findViewById(R.id.txtAppointmentDate);
            TextView txtTime = findViewById(R.id.txtAppointmentTime);

            txtDoctor.setText(next.getDoctorName());
            txtSpeciality.setText(next.getSpeciality());
            txtDate.setText(next.getDate());
            txtTime.setText(next.getTime());
            findViewById(R.id.cardAppointment).setOnClickListener(v->{
                startActivity(new Intent(HomeActivity.this, AppointmentActivity.class));
            });
        }
    }
}
