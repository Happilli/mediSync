package com.bca.medisync;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bca.medisync.R;
import com.bca.medisync.doctor.DoctorHomeFragment;
import com.bca.medisync.doctor.DoctorProfileFragment;
import com.bca.medisync.doctor.PatientFragment;
import com.bca.medisync.doctor.ScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class DoctorTabActivity extends AppCompatActivity {
  private BottomNavigationView bottomNav;
  private final Map<Integer, Fragment> fragmentCache = new HashMap<>();
  private Fragment activeFragment;
  private int activeTabId = R.id.nav_doctor_home;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_doctor_tab);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
          return insets;
        });
    bottomNav = findViewById(R.id.bottomNavDoctor);

    if (savedInstanceState == null) {
      switchTo(R.id.nav_doctor_home);
    }

    bottomNav.setOnItemSelectedListener(
        item -> {
          getSupportFragmentManager()
              .popBackStackImmediate(
                  null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
          bottomNav.setVisibility(View.VISIBLE);
          switchTo(item.getItemId());
          return true;
        });

    getSupportFragmentManager()
        .addOnBackStackChangedListener(
            () -> {
              if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                bottomNav.setVisibility(View.VISIBLE);
              }
            });
  }

  private void switchTo(int itemId) {
    activeTabId = itemId;
    Fragment fragment = fragmentCache.get(itemId);
    if (fragment == null) {
      fragment = createFragment(itemId);
      fragmentCache.put(itemId, fragment);
    }

    FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
    if (!fragment.isAdded()) {
      tx.add(R.id.doctorFragmentContainer, fragment);
    }
    for (Fragment f : fragmentCache.values()) {
      if (f != fragment) tx.hide(f);
    }
    tx.show(fragment).commit();
    activeFragment = fragment;
  }

  private Fragment createFragment(int itemId) {
    if (itemId == R.id.nav_doctor_schedule) return new ScheduleFragment();
    if (itemId == R.id.nav_doctor_patients) return new PatientFragment();
    if (itemId == R.id.nav_doctor_profile) return new DoctorProfileFragment();
    return new DoctorHomeFragment();
  }

  public void pushFragment(Fragment fragment) {
    bottomNav.setVisibility(View.VISIBLE);
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.doctorFragmentContainer, fragment)
        .addToBackStack(null)
        .commit();
  }
}
