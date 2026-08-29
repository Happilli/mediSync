package com.bca.medisync.doctor;

import androidx.fragment.app.Fragment;

import com.bca.medisync.BaseTabActivity;
import com.bca.medisync.R;

public class DoctorTabActivity extends BaseTabActivity {

  @Override
  protected int getLayoutRes() {
    return R.layout.activity_doctor_tab;
  }

  @Override
  protected int getContainerId() {
    return R.id.doctorFragmentContainer;
  }

  @Override
  protected int getBottomNavId() {
    return R.id.bottomNavDoctor;
  }

  @Override
  protected int getDefaultTabId() {
    return R.id.nav_doctor_home;
  }

  @Override
  protected Fragment createFragment(int itemId) {
    if (itemId == R.id.nav_doctor_schedule) return new ScheduleFragment();
    if (itemId == R.id.nav_doctor_patients) return new PatientFragment();
    if (itemId == R.id.nav_doctor_profile) return new DoctorProfileFragment();
    return new DoctorHomeFragment();
  }
}
