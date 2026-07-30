package com.bca.medisync.doctor;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.dto.timeslot.TimeSlotResponse;
import com.bca.medisync.data.repository.TimeSlotRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimeSlotDetailActivity extends AppCompatActivity {

    private TextView txtDate, txtTime, txtStatus;
    private MaterialToolbar toolbar;
    private MaterialButton btnEdit, btnDelete;
    private LinearLayout layoutActions;
    private TimeSlotResponse slot;
    private TimeSlotRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_slot_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            
            View toolbarView = findViewById(R.id.toolbar);
            toolbarView.setPadding(0, systemBars.top, 0, 0);
            
            int actionBarSize = 0;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
            if (actionBarSize > 0) {
                toolbarView.getLayoutParams().height = actionBarSize + systemBars.top;
            }

            return insets;
        });

        repository = new TimeSlotRepository();
        initViews();
        setupToolbar();
        setupListeners();
        displayDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtStatus = findViewById(R.id.txtStatus);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        layoutActions = findViewById(R.id.layoutActions);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> showTimePicker());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(9)
                .setMinute(0)
                .setTitleText("Select New Time")
                .build();

        timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

        timePicker.addOnPositiveButtonClickListener(v -> {
            updateTimeSlot(timePicker.getHour(), timePicker.getMinute());
        });
    }

    private void updateTimeSlot(int hour, int minute) {
        try {
            SimpleDateFormat sdfInput = slot.getAppointment_at().contains(".") ?
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()) :
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdfInput.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date originalDate = sdfInput.parse(slot.getAppointment_at());
            if (originalDate == null) return;

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(originalDate);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            sdfOutput.setTimeZone(TimeZone.getTimeZone("UTC"));
            String isoDateTime = sdfOutput.format(calendar.getTime());

            repository.updateTimeSlot(slot.getId(), isoDateTime).enqueue(new Callback<TimeSlotResponse>() {
                @Override
                public void onResponse(Call<TimeSlotResponse> call, Response<TimeSlotResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(TimeSlotDetailActivity.this, "Time slot updated", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(TimeSlotDetailActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TimeSlotResponse> call, Throwable t) {
                    Toast.makeText(TimeSlotDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error updating time", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Time Slot")
                .setMessage("Are you sure you want to delete this available slot?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTimeSlot())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTimeSlot() {
        repository.deleteTimeSlot(slot.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TimeSlotDetailActivity.this, "Time slot deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(TimeSlotDetailActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TimeSlotDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDetails() {
        slot = (TimeSlotResponse) getIntent().getSerializableExtra("time_slot");
        if (slot != null) {
            String timestamp = slot.getAppointment_at();
            txtDate.setText(formatToDate(timestamp));
            txtTime.setText(formatToTime(timestamp));
            txtStatus.setText(slot.isIs_available() ? "Available" : "Booked");
            txtStatus.setTextColor(getResources().getColor(slot.isIs_available() ? R.color.primary : R.color.secondary, getTheme()));

            if (isPastDate(timestamp) || !slot.isIs_available()) {
                layoutActions.setVisibility(View.GONE);
            } else {
                layoutActions.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isPastDate(String timestamp) {
        try {
            SimpleDateFormat input = timestamp.contains(".") ?
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()) :
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = input.parse(timestamp);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return date != null && date.before(cal.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    private String formatToDate(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            SimpleDateFormat input = timestamp.contains(".") ?
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()) :
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = input.parse(timestamp);
            SimpleDateFormat output = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            output.setTimeZone(TimeZone.getTimeZone("UTC"));
            return output.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }

    private String formatToTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            SimpleDateFormat input = timestamp.contains(".") ?
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()) :
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = input.parse(timestamp);
            SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            output.setTimeZone(TimeZone.getTimeZone("UTC"));
            return output.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }
}