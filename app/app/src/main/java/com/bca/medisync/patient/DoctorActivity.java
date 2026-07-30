package com.bca.medisync.patient;

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

import com.bca.medisync.R;
import com.bca.medisync.adapter.DoctorAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;


public class DoctorActivity extends AppCompatActivity {
    private RecyclerView rvDoctors;
    private MaterialToolbar toolbar;
    private TextInputEditText etSearch;
    private DoctorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
    }
    private void initViews(){
        rvDoctors = findViewById(R.id.rvDoctors);
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v->{
            finish();
        });
    }
    private void setupRecyclerView(){
        List<Doctor> doctors = DataProvider.getDoctors();
        adapter = new DoctorAdapter(this, doctors, doctor -> {
            Intent intent = new Intent(DoctorActivity.this, BookAppointmentActivity.class);
            intent.putExtra("doctor_id", doctor.getId());
            intent.putExtra("doctor_name", doctor.getName());
            intent.putExtra("doctor_speciality", doctor.getSpeciality());
            intent.putExtra("doctor_info", doctor.getInfo());
            intent.putExtra("doctor_department", doctor.getDepartment());
            startActivity(intent);
        });
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvDoctors.setAdapter(adapter);
    }
    private void setupSearch(){
        etSearch.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s,  int start, int count, int after){

            }
            @Override
            public void onTextChanged(CharSequence s,  int start, int before, int count){
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s){
            }
        });
    }
}