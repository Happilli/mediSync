package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.bca.medisync.BaseTabActivity;
import com.bca.medisync.R;

public class MainTabActivity extends BaseTabActivity {

  @Override
  protected int getLayoutRes() {
    return R.layout.activity_main_tab;
  }

  @Override
  protected int getContainerId() {
    return R.id.fragmentContainer;
  }

  @Override
  protected int getBottomNavId() {
    return R.id.bottomNav;
  }

  @Override
  protected int getDefaultTabId() {
    return R.id.nav_home;
  }

  @Override
  protected Fragment createFragment(int itemId) {
    if (itemId == R.id.nav_appointments) return new AppointmentFragment();
    if (itemId == R.id.nav_medications) return new MedicationFragment();
    if (itemId == R.id.nav_profile) return new ProfileFragment();
    return new HomeFragment();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleDeepLink(getIntent());
  }

  private void handleDeepLink(Intent intent) {
    if (intent == null) return;
    String target = intent.getStringExtra("open_fragment");
    if ("consultation_detail".equals(target)) {
      int appointmentId = intent.getIntExtra("appointment_id", -1);
      if (appointmentId != -1) {
        Bundle args = new Bundle();
        args.putInt("appointment_id", appointmentId);
        ConsultationDetailFragment fragment = new ConsultationDetailFragment();
        fragment.setArguments(args);
        pushFragment(fragment);
      }
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
   handleDeepLink(intent);
  }

  public void popToRootAndRefreshAppointments() {
    popToRoot();
    Fragment f = getFragment(R.id.nav_appointments);
    if (f instanceof AppointmentFragment) {
      ((AppointmentFragment) f).refresh();
    }
  }
}
