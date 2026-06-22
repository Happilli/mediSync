package com.bca.medisync;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.adapter.TimeSlotAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.TimeSlot;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextView txtDoctorName, txtDoctorSpeciality, txtDoctorInfo, txtSelectedDate;
    private MaterialButton btnSelectDate , btnConfirm;
    private TextInputEditText etNotes;
    private RecyclerView rvTimeSlots;

    private String selectedDate = "";
    private String selectedTime = "";

    // doctor info from DoctorActivity
    private String doctorName, doctorSpeciality, doctorInfo, doctorDepartment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupToolbar();
        loadDoctorData();
        setupDatePicker();
        setupTimeSlots();
        setupConfirmButton();
    }
    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        txtDoctorName = findViewById(R.id.txtDoctorName);
        txtDoctorSpeciality = findViewById(R.id.txtDoctorSpeciality);
        txtDoctorInfo = findViewById(R.id.txtDoctorInfo);
        txtSelectedDate = findViewById(R.id.txtSelectedDate);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        etNotes = findViewById(R.id.etNotes);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }
    private  void setupDatePicker(){
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("select Appointment Date").build();
        btnSelectDate.setOnClickListener(v -> {
            picker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
        picker.addOnPositiveButtonClickListener(selection ->{
            selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selection));
            txtSelectedDate.setText(selectedDate);
        });
    }

    private void loadDoctorData(){
        doctorName = getIntent().getStringExtra("doctor_name");
        doctorSpeciality = getIntent().getStringExtra("doctor_speciality");
        doctorInfo = getIntent().getStringExtra("doctor_info");
        doctorDepartment = getIntent().getStringExtra("doctor_department");

        if(doctorName!=null){
            txtDoctorName.setText(doctorName);
        }
        if(doctorSpeciality!=null){
            txtDoctorSpeciality.setText(doctorSpeciality);
        }
        if(doctorInfo!=null){
            txtDoctorInfo.setText(doctorInfo);
        }
    }
    private  void setupTimeSlots(){
        List<TimeSlot> slots = DataProvider.getTimeSlots();

        TimeSlotAdapter adapter = new TimeSlotAdapter(this, slots, time->{
            selectedTime = time;
        });
        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 3));
        rvTimeSlots.setAdapter(adapter);
    }
    private  void setupConfirmButton(){
        btnConfirm.setOnClickListener(v -> {
            if(selectedDate.isEmpty()){
                Toast.makeText(this , "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }
            if(selectedTime.isEmpty()){
                Toast.makeText(this , "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }
            String notes = etNotes.getText()!= null ? etNotes.getText().toString():"";
            Toast.makeText(this, "Appointment booked\n" + doctorName+ "\n"+ selectedDate+" at "
            +selectedTime, Toast.LENGTH_LONG).show();
        });
    }

}