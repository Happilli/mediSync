package com.bca.medisync.doctor;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.adapter.TimeSlotAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.remote.dto.timeslot.TimeSlotResponse;
import com.bca.medisync.data.repository.AppointmentRepository;
import com.bca.medisync.data.repository.DoctorRepository;
import com.bca.medisync.data.repository.TimeSlotRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleActivity extends AppCompatActivity {
    private static final String TAG = "ScheduleDebug";
    private MaterialToolbar toolbar;
    private RecyclerView rvSchedule, rvTimeSlots;
    private LinearLayout dateStripContainer;
    private FloatingActionButton fabAddSlot;
    private TextView txtNoSlots, txtNoAppointments;
    private BottomNavigationView bottomNav;

    private final List<Appointment> allAppointments = new ArrayList<>();
    private final List<TimeSlotResponse> allTimeSlots = new ArrayList<>();

    private AppointmentAdapter adapter;
    private TimeSlotAdapter timeSlotAdapter;
    private AppointmentRepository repository;
    private TimeSlotRepository timeSlotRepository;
    private DoctorRepository doctorRepository;
    private String selectedDate;
    private int currentDoctorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        initViews();
        setupToolbar();
        setupDateStrip();
        setupRecyclerView();
        setupFab();
        setupBottomNav();
    }
    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        rvSchedule = findViewById(R.id.rvSchedule);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        dateStripContainer = findViewById(R.id.dateStripContainer);
        fabAddSlot = findViewById(R.id.fabAddSlot);
        txtNoSlots = findViewById(R.id.txtNoSlots);
        txtNoAppointments = findViewById(R.id.txtNoAppointments);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.doc_nav_schedule);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.doc_nav_dashboard) {
                intent = new Intent(this, DoctorHomeActivity.class);
            } else if (id == R.id.doc_nav_schedule) {
                return true;
            } else if (id == R.id.doc_nav_patients) {
                intent = new Intent(this, DoctorPatientsActivity.class);
            } else if (id == R.id.doc_nav_profile) {
                intent = new Intent(this, DoctorProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }


    private void setupRecyclerView() {

        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentAdapter(
                this,
                new ArrayList<>(),
                false,
                appointment -> {
                    Intent intent = new Intent(this, AppointmentDetailActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivityForResult(intent, 100);
                });

        timeSlotAdapter = new TimeSlotAdapter(this, new ArrayList<>(), slot -> {
            Intent intent = new Intent(ScheduleActivity.this, TimeSlotDetailActivity.class);
            intent.putExtra("time_slot", slot);
            startActivityForResult(intent, 101);
        });

        rvSchedule.setAdapter(adapter);
        rvTimeSlots.setAdapter(timeSlotAdapter);

        repository = new AppointmentRepository();
        timeSlotRepository = new TimeSlotRepository();
        doctorRepository = new DoctorRepository();

        loadData();
    }

    private void loadData() {
        loadAppointments();
        if (currentDoctorId != -1) {
            loadTimeSlots(currentDoctorId);
        } else {
            loadDoctorIdAndSlots();
        }
    }

    private void loadDoctorIdAndSlots() {
        doctorRepository.getProfile().enqueue(new Callback<DoctorProfileResponse>() {
            @Override
            public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentDoctorId = response.body().getId();
                    loadTimeSlots(currentDoctorId);
                }
            }

            @Override
            public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                Toast.makeText(ScheduleActivity.this, "Failed to load doctor profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab() {
        fabAddSlot.setOnClickListener(v -> showDateTimePicker());
    }

    private void showDateTimePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(9)
                    .setMinute(0)
                    .setTitleText("Select Time")
                    .build();

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(v -> {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String isoDateTime = sdf.format(calendar.getTime());

                createNewTimeSlot(isoDateTime);
            });
        });
    }

    private void createNewTimeSlot(String isoDateTime) {
        Log.d(TAG, "Creating new timeslot with ISO date: " + isoDateTime);
        timeSlotRepository.createTimeSlot(isoDateTime).enqueue(new Callback<TimeSlotResponse>() {
            @Override
            public void onResponse(Call<TimeSlotResponse> call, Response<TimeSlotResponse> response) {
                Log.d(TAG, "POST timeslot response code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "POST timeslot successful: " + response.body());
                    Toast.makeText(ScheduleActivity.this, "Time slot added successfully", Toast.LENGTH_SHORT).show();
                    if (currentDoctorId != -1) {
                        Log.d(TAG, "Refreshing timeslots for doctorId: " + currentDoctorId);
                        loadTimeSlots(currentDoctorId);
                    }
                } else if (response.code() == 400) {
                    Toast.makeText(ScheduleActivity.this, "Time slot already exists", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ScheduleActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TimeSlotResponse> call, Throwable t) {
                Toast.makeText(ScheduleActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTimeSlots(int doctorId) {
        Log.d(TAG, "Requesting timeslots for doctorId: " + doctorId);
        timeSlotRepository.getDoctorTimeSlots(doctorId).enqueue(new Callback<List<TimeSlotResponse>>() {
            @Override
            public void onResponse(Call<List<TimeSlotResponse>> call, Response<List<TimeSlotResponse>> response) {
                Log.d(TAG, "GET timeslots response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<TimeSlotResponse> slots = response.body();
                    Log.d(TAG, "GET timeslots full response body: " + slots);
                    Log.d(TAG, "Timeslots list size: " + slots.size());
                    
                    for (TimeSlotResponse slot : slots) {
                        Log.d(TAG, "Slot - ID: " + slot.getId() + ", appointment_at: " + slot.getAppointment_at() + ", is_available: " + slot.isIs_available() + ", doctor_id: " + slot.getDoctor_id());
                    }

                    allTimeSlots.clear();
                    allTimeSlots.addAll(slots);
                    filterByDate(selectedDate);
                } else {
                    Log.d(TAG, "GET timeslots error body: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TimeSlotResponse>> call, Throwable t) {
                Toast.makeText(ScheduleActivity.this, "Failed to load time slots", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100 || requestCode == 101) && resultCode == RESULT_OK) {
            loadData();
        }
    }

    private void loadAppointments() {

        repository.getDoctorAppointments(new Callback<List<AppointmentResponse>>() {

            @Override
            public void onResponse(Call<List<AppointmentResponse>> call,
                                   Response<List<AppointmentResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Appointments returned from Retrofit: " + response.body().size());
                    allAppointments.clear();

                    for (AppointmentResponse dto : response.body()) {
                        allAppointments.add(dto.toAppointment());
                    }

                    filterByDate(selectedDate);

                } else {
                    Toast.makeText(
                            ScheduleActivity.this,
                            "Error : " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }

            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call,
                                  Throwable t) {

                Toast.makeText(
                        ScheduleActivity.this,
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

    }

    private void filterByDate(String date) {
        Log.d(TAG, "selectedDate: " + date);

        boolean isPast = isPastDate(date);
        fabAddSlot.setVisibility(isPast ? View.GONE : View.VISIBLE);

        List<Appointment> filteredApps = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.getDate().equals(date)) {
                filteredApps.add(a);
            }
        }

        List<TimeSlotResponse> filteredSlots = new ArrayList<>();
        if (!isPast) {
            for (TimeSlotResponse slot : allTimeSlots) {
                String slotDate = formatToDateOnly(slot.getAppointment_at());
                if (slotDate.equals(date) && slot.isIs_available()) {
                    filteredSlots.add(slot);
                }
            }
        }

        if (adapter != null) {
            adapter.updateList(filteredApps);
            rvSchedule.setVisibility(filteredApps.isEmpty() ? View.GONE : View.VISIBLE);
            txtNoAppointments.setVisibility(filteredApps.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (timeSlotAdapter != null) {
            timeSlotAdapter.updateList(filteredSlots);
            rvTimeSlots.setVisibility(filteredSlots.isEmpty() ? View.GONE : View.VISIBLE);
            txtNoSlots.setVisibility(filteredSlots.isEmpty() ? View.VISIBLE : View.GONE);
        }

    }

    private boolean isPastDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date selected = fmt.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return selected != null && selected.before(cal.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    private String formatToDateOnly(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            SimpleDateFormat input;
            if (timestamp.contains(".")) {
                input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            } else {
                input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            }
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = input.parse(timestamp);
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            output.setTimeZone(TimeZone.getTimeZone("UTC"));
            return output.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private void setupDateStrip() {

        Calendar cal = Calendar.getInstance();

        SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat fullFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String todayFull = fullFmt.format(new Date());

        for (int i = 0; i < 7; i++) {
            Date date = cal.getTime();
            String dayLabel = dayFmt.format(date);
            String fullDate = fullFmt.format(date);
            int dayNum = cal.get(Calendar.DAY_OF_MONTH);

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
            item.setClickable(true);
            item.setFocusable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd(dpToPx(8));
            item.setLayoutParams(lp);

            TextView txtDay = new TextView(this);
            txtDay.setText(dayLabel);
            txtDay.setTextSize(12);
            txtDay.setGravity(Gravity.CENTER);
            txtDay.setTextColor(getColor(R.color.on_surface_variant));

            TextView txtNum = new TextView(this);
            txtNum.setText(String.valueOf(dayNum));
            txtNum.setTextSize(15);
            txtNum.setGravity(Gravity.CENTER);
            txtNum.setTextColor(getColor(R.color.on_surface));

            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
            numLp.topMargin = dpToPx(4);
            txtNum.setLayoutParams(numLp);

            item.addView(txtDay);
            item.addView(txtNum);

            boolean isToday = fullDate.equals(todayFull);
            if (isToday) {
                txtDay.setTextColor(getColor(R.color.primary));
                txtNum.setBackground(circleDrawable());
                txtNum.setTextColor(getColor(R.color.on_primary));
                selectedDate = fullDate;
            }

            item.setOnClickListener(v -> {
                selectedDate = fullDate;
                filterByDate(fullDate);
                refreshDateStripSelection(item);
            });

            dateStripContainer.addView(item);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void refreshDateStripSelection(LinearLayout selected) {
        for (int i = 0; i < dateStripContainer.getChildCount(); i++) {
            LinearLayout child = (LinearLayout) dateStripContainer.getChildAt(i);
            boolean isSelected = child == selected;
            TextView txtDay = (TextView) child.getChildAt(0);
            TextView txtNum = (TextView) child.getChildAt(1);
            txtDay.setTextColor(getColor(isSelected ? R.color.primary : R.color.on_surface_variant));
            txtNum.setBackground(isSelected ? circleDrawable() : null);
            txtNum.setTextColor(getColor(isSelected ? R.color.on_primary : R.color.on_surface));
        }
    }

    private GradientDrawable circleDrawable() {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(getColor(R.color.primary));
        return circle;
    }
    private int dpToPx(int dp){
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}