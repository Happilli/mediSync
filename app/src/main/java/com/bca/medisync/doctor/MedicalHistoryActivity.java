package com.bca.medisync.doctor;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.MedicalHistoryAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.MedicalHistory;
import com.google.android.material.appbar.MaterialToolbar;

public class MedicalHistoryActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextView tvToolbarTitle;
    private RecyclerView rvHistory;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        setupToolbar();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        rvHistory = findViewById(R.id.rvHistory);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadData() {
        patientName = getIntent().getStringExtra("patient_name");
        if (patientName != null) {
            tvToolbarTitle.setText(patientName + " History");
        }

        MedicalHistory history = DataProvider.getMedicalHistory(patientName);
        if (history != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            MedicalHistoryAdapter adapter = new MedicalHistoryAdapter(history.getTimeline());
            rvHistory.setAdapter(adapter);
        }
    }
}