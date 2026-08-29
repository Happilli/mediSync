package com.bca.medisync.patient;

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

  public void popToRootAndRefreshAppointments() {
    popToRoot();
    Fragment f = getFragment(R.id.nav_appointments);
    if (f instanceof AppointmentFragment) {
      ((AppointmentFragment) f).refresh();
    }
  }
}
