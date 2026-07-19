package com.bca.medisync.patient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.MedicationAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Medication;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class MedicationFragment extends Fragment {
  private RecyclerView rvMedications;
  private TextView tvActiveTime, tvActiveName, tvActiveDosage;
  private MaterialButton btnMarkTaken;
  private ExtendedFloatingActionButton fabAddMedication;

  public MedicationFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_medication, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setUpActiveCard();
    setUpRecyclerView();
    setupFab();
    scheduleAlarms();
  }

  private void initViews(View view) {
    rvMedications = view.findViewById(R.id.rvMedications);
    tvActiveTime = view.findViewById(R.id.tvActiveTime);
    tvActiveName = view.findViewById(R.id.tvActiveName);
    tvActiveDosage = view.findViewById(R.id.tvActiveDosage);
    btnMarkTaken = view.findViewById(R.id.btnMarkTaken);
    fabAddMedication = view.findViewById(R.id.fabAddMedication);
  }

  private void setUpActiveCard() {
    List<Medication> meds = DataProvider.getMedications();
    if (!meds.isEmpty()) {
      Medication first = meds.get(0);
      tvActiveName.setText(first.getName() + " " + first.getDosage());
      tvActiveDosage.setText(first.getFrequency());
      tvActiveTime.setText(first.getTime());
    }
    btnMarkTaken.setOnClickListener(
        v -> Toast.makeText(requireContext(), "Marked as taken", Toast.LENGTH_SHORT).show());
  }

  private void setUpRecyclerView() {
    List<Medication> meds = DataProvider.getMedications();
    rvMedications.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvMedications.setAdapter(
        new MedicationAdapter(
            requireContext(),
            meds,
            medication ->
                Toast.makeText(requireContext(), medication.getName(), Toast.LENGTH_SHORT).show()));
  }

  private void setupFab() {
    fabAddMedication.setOnClickListener(
        v -> {
          // add medication stuff
        });
  }

  private void scheduleAlarms() {
    List<Medication> meds = DataProvider.getMedications();
    for (Medication med : meds) {
      try {
        String[] parts = med.getTime().split("[:. ]");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        String period = parts[2];
        if (period.equalsIgnoreCase("PM") && hour != 12) {
          hour += 12;
        }
        if (period.equalsIgnoreCase("AM") && hour == 12) {
          hour = 0;
        }
        setAlarm(med.getName(), med.getDosage(), hour, minute);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void setAlarm(String name, String dosage, int hour, int minute) {
    AlarmManager alarmManager =
        (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
        Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        startActivity(intent);
        return;
      }
    }

    Intent intent = new Intent(requireContext(), AlarmReceiver.class);
    intent.putExtra("med_name", name);
    intent.putExtra("med_dosage", dosage);

    int requestCode = (name + hour + minute).hashCode();

    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
            AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
  }
}
