package com.bca.medisync;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseTabActivity extends AppCompatActivity {
  private BottomNavigationView bottomNav;
  private final Map<Integer, Fragment> fragmentCache = new HashMap<>();
  private Fragment activeFragment;
  protected abstract int getLayoutRes();
  protected abstract int getContainerId();
  protected abstract int getBottomNavId();
  protected abstract int getDefaultTabId();
  protected abstract Fragment createFragment(int itemId);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(getLayoutRes());
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
          return insets;
        });
    bottomNav = findViewById(getBottomNavId());

    if (savedInstanceState == null) {
      switchTo(getDefaultTabId());
    }

    bottomNav.setOnItemSelectedListener(
        item -> {
          getSupportFragmentManager()
              .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

  protected Fragment getFragment(int itemId) {
    return fragmentCache.get(itemId);
  }

  private void switchTo(int itemId) {
    Fragment fragment = fragmentCache.get(itemId);
    if (fragment == null) {
      fragment = createFragment(itemId);
      fragmentCache.put(itemId, fragment);
    }

    FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
    if (!fragment.isAdded()) {
      tx.add(getContainerId(), fragment);
    }
    for (Fragment f : fragmentCache.values()) {
      if (f != fragment) tx.hide(f);
    }
    tx.show(fragment).commit();
    activeFragment = fragment;
  }

  public void pushFragment(Fragment fragment) {
    bottomNav.setVisibility(View.VISIBLE);
    getSupportFragmentManager()
        .beginTransaction()
        .add(getContainerId(), fragment)
        .addToBackStack(null)
        .commit();
  }

  public void popToRoot() {
    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    bottomNav.setVisibility(View.VISIBLE);
  }
}
