package com.bca.medisync;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.adapter.DashboardAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.model.DataProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView rvDashboard;
    private BottomNavigationView  bottomNav;

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
        rvDashboard=findViewById(R.id.rvDashboard);
        bottomNav=findViewById(R.id.bottomNav);
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
                android.R.drawable.ic_menu_agenda,
                android.R.drawable.ic_menu_edit,
                android.R.drawable.ic_menu_add,
                android.R.drawable.ic_menu_myplaces,
                android.R.drawable.ic_menu_info_details,
                android.R.drawable.ic_menu_compass
        );

        DashboardAdapter adapter = new DashboardAdapter(this, titles, icons, positons -> {
            switch (positons){
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
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
            }  else if (id == R.id.nav_appointments) {
                return true;
            } else if (id == R.id.nav_appointments) {
                return true;
            } else if (id == R.id.nav_medications) {
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void setUpAppointmentCard(){
        List<Appointment> appointments  = DataProvider.getAppointments();
        if (!appointments.isEmpty()){
            Appointment next = appointments.get(0);
            findViewById(R.id.cardAppointment).setOnClickListener(v->{

            });
        }
    }
}
