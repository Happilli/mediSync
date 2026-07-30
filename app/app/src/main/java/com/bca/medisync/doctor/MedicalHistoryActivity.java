package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.MedicalHistory;
import com.bca.medisync.data.model.MedicalHistoryEntry;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class MedicalHistoryActivity extends AppCompatActivity {
    private ExtendedFloatingActionButton btnStartConsultation;
    private String patientName;
    private TextView tvHeader, tvRxName, tvRXDesc,tvLabTitle, tvLabDesc,  date1, title1, desc1, date2, title2, desc2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupListener();
        loadData();
    }
    private void initViews(){
        btnStartConsultation = findViewById(R.id.fabConsult);
        tvHeader = findViewById(R.id.tvHeader);
        tvRXDesc = findViewById(R.id.tvRxDesc);
        tvRxName = findViewById(R.id.tvRxName);
        tvLabTitle=findViewById(R.id.tvLabTitle);
        tvLabDesc = findViewById(R.id.tvLabDesc);

        date1 = findViewById(R.id.date1);
        title1 = findViewById(R.id.title1);
        desc1 = findViewById(R.id.desc1);

        date2 = findViewById(R.id.date2);
        title2 = findViewById(R.id.title2);
        desc2 = findViewById(R.id.desc2);
    }

    private void loadData(){
        patientName = getIntent().getStringExtra("patient_name");
        tvHeader.setText(patientName!=null ? patientName + "\nOverview":"Patient\nOverview");
        MedicalHistory history = DataProvider.getMedicalHistory(patientName);
        tvRxName.setText(history.getLatestRxName());
        tvRXDesc.setText(history.getLatestRxDesc());
        tvLabTitle.setText(history.getLatestLabTitle());
        tvLabDesc.setText(history.getLatestLabDesc());
        List<MedicalHistoryEntry> timeline = history.getTimeline();

        if (!timeline.isEmpty()) {
            date1.setText(timeline.get(0).getDate());
            title1.setText(timeline.get(0).getTitle());
            desc1.setText(timeline.get(0).getDescription());
        }

        if (timeline.size() >= 2) {
            date2.setText(timeline.get(1).getDate());
            title2.setText(timeline.get(1).getTitle());
            desc2.setText(timeline.get(1).getDescription());
        }
    }
    private void setupListener(){
        btnStartConsultation.setOnClickListener(v -> {
            MedicalHistory history = DataProvider.getMedicalHistory(patientName);
            List<MedicalHistoryEntry> timeline = history.getTimeline();

            String latestDiagnosis = "";
            String latestDate = "";
            if(!timeline.isEmpty()){
                MedicalHistoryEntry latest  = timeline.get(timeline.size()-1);
                latestDiagnosis = latest.getTitle();
                latestDate = latest.getDate();
            }

            Intent intent = new Intent(MedicalHistoryActivity.this, ConsultationActivity.class);
            intent.putExtra("patient_name", patientName);
            intent.putExtra("latest_diagnosis", latestDiagnosis);
            intent.putExtra("latest_date", latestDate);
            startActivity(intent);
        });
    }
}