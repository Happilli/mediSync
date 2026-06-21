package com.bca.medisync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.adapter.MedicationAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Medication;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class MedicationActivity extends AppCompatActivity {
    private RecyclerView rvMedications;
    private MaterialToolbar toolbar;
    private TextView tvActiveTime, tvActiveName, tvActiveDosage;
    private MaterialButton btnMarkTaken;
    private ExtendedFloatingActionButton fabAddMedication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupToolbar();
        setUpActiveCard();
        setUpRecyclerView();
        setupFab();
        scheduleAlarms();
    }


    private void initViews(){
        rvMedications = findViewById(R.id.rvMedications);
        toolbar = findViewById(R.id.toolbar);
        tvActiveTime = findViewById(R.id.tvActiveTime);
        tvActiveName = findViewById(R.id.tvActiveName);
        tvActiveDosage = findViewById(R.id.tvActiveDosage);
        btnMarkTaken = findViewById(R.id.btnMarkTaken);
        fabAddMedication = findViewById(R.id.fabAddMedication);
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v->finish());
    }

    private void setUpActiveCard(){
        List<Medication> meds = DataProvider.getMedications();
        if(!meds.isEmpty()){
            Medication first = meds.get(0);
            tvActiveName.setText(first.getName()+" "+first.getDosage());
            tvActiveDosage.setText(first.getFrequency());
            tvActiveTime.setText(first.getTime());
        }
        btnMarkTaken.setOnClickListener(v->{
            Toast.makeText(this, "Marked as taken", Toast.LENGTH_SHORT).show();
        });
    }
    private void setUpRecyclerView(){
        List<Medication> meds = DataProvider.getMedications();
        rvMedications.setLayoutManager(new LinearLayoutManager(this));
        rvMedications.setAdapter(new MedicationAdapter(this, meds, medication -> {
            Toast.makeText(this, medication.getName(), Toast.LENGTH_SHORT).show();
        }));
    }
    private void setupFab(){
        fabAddMedication.setOnClickListener(v -> {
          // add medication stuff
        });
    }

    private void scheduleAlarms(){
        List<Medication> meds = DataProvider.getMedications();
        for(Medication med: meds){
            try {
                String[] parts = med.getTime().split("[:. ]");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                String period = parts[2];
                if(period.equalsIgnoreCase("PM") && hour!=12){
                    hour+=12;
                }
                if(period.equalsIgnoreCase("AM") && hour==12){
                    hour=0;
                }
                setAlarm(med.getName(), med.getDosage(), hour, minute);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void setAlarm(String name, String dosage, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("med_name", name);
        intent.putExtra("med_dosage", dosage);

        int requestCode = (name + hour + minute).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        }
}
