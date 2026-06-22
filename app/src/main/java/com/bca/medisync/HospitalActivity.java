package com.bca.medisync;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.adapter.HospitalAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Hospital;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class HospitalActivity extends AppCompatActivity {
    private RecyclerView rvHospitals;
    private MaterialToolbar toolbar;
    private HospitalAdapter adapter;
    private TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hospital);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
        setupToolbar();
        setupRecycleView();
        setupSearch();
    }
    private void initView(){
        rvHospitals = findViewById(R.id.rvHospitals);
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
    }
    private  void setupToolbar(){
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void setupRecycleView(){
        List<Hospital> hospitals = DataProvider.getHospitals();
        adapter = new HospitalAdapter(this, hospitals, hospital -> {
            Intent intent = new Intent(HospitalActivity.this, HospitalDetailActivity.class);
            intent.putExtra("hospital_id", hospital.getId());
            intent.putExtra("hospital_name", hospital.getName());
            intent.putExtra("hospital_address", hospital.getAddress());
            intent.putExtra("hospital_phone", hospital.getPhone());
            intent.putExtra("hospital_website", hospital.getWebsite());
            intent.putExtra("hospital_description", hospital.getDescription());
            intent.putExtra("hospita_rating", hospital.getRating());
            startActivity(intent);
        });
        rvHospitals.setLayoutManager(new LinearLayoutManager(this));
        rvHospitals.setAdapter(adapter);
    }
    private void setupSearch(){
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s){
            }
        });
    }
}