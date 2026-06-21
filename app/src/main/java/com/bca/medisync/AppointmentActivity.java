package com.bca.medisync;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.model.DataProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AppointmentActivity extends AppCompatActivity {
    private RecyclerView rvUpcoming, rvHistory;
    private TabLayout tabLayout;
    private MaterialToolbar toolbar;
    private ExtendedFloatingActionButton faBookAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupToolbar();
        setupTabs();
        setUpRecylerViews();
        setupFab();
    }
    private void initViews(){
        rvUpcoming = findViewById(R.id.rvUpcoming);
        rvHistory = findViewById(R.id.rvHistory);
        tabLayout = findViewById(R.id.tabLayout);
        toolbar = findViewById(R.id.toolbar);
        faBookAppointment = findViewById(R.id.fabBookAppointment);
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v ->finish());
    }
    private void setupTabs(){
        tabLayout.addTab(tabLayout.newTab().setText("UpcominG"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
           @Override
            public void onTabSelected(TabLayout.Tab tab){
               if(tab.getPosition()==0){
                   rvUpcoming.setVisibility(View.VISIBLE);
                   rvHistory.setVisibility(View.GONE);
               } else {
                   rvUpcoming.setVisibility(View.GONE);
                   rvHistory.setVisibility(View.VISIBLE);
               }
           }

           @Override
            public void onTabUnselected(TabLayout.Tab tab){
           }
            @Override
            public void onTabReselected(TabLayout.Tab tab){
            }
        });
    }

    private void setUpRecylerViews(){
        List<Appointment> all = DataProvider.getAppointments();
        List<Appointment> upcoming  = new ArrayList<>();
        List<Appointment> history  = new ArrayList<>();

        for (Appointment a: all){
            if (a.getStatus().equals("Confirmed")||a.getStatus().equals("Scheduled")){
                upcoming.add(a);
            }else {
                history.add(a);
            }
        }
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        rvUpcoming.setAdapter(new AppointmentAdapter(this, upcoming, appointment -> {
// appointment details
        }));

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(new AppointmentAdapter(this, upcoming, appointment -> {
// appointment details
        }));
    }
    private  void setupFab(){
        faBookAppointment.setOnClickListener(v ->{
            // booking appointment flow nav something
        });
    }
}
