package com.bca.medisync.patient;

import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.DoctorGroupedAdapter;
import com.bca.medisync.data.model.Doctor;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.util.SearchSuggestionHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DoctorFragment extends Fragment {
  private RecyclerView rvDoctors;
  private RecyclerView rvSearchSuggestions;
  private MaterialToolbar toolbar;
  private SearchBar searchBar;
  private SearchView searchView;
  private DoctorGroupedAdapter adapter;

  private Integer filterHospitalId;

  private final Set<String> expandedIds = new HashSet<>();
  private final Map<Integer, String> hospitalNameCache = new HashMap<>();

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_doctor, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      String hospitalIdStr = args.getString("hospital_id");
      if (hospitalIdStr != null) {
        try {
          filterHospitalId = Integer.parseInt(hospitalIdStr);
        } catch (NumberFormatException ignored) {
        }
      }
    }
    initViews(view);
    setupToolbar();
    setupRecyclerView();
    setupSearch();
    loadDoctors(null);
  }

  private void initViews(View view) {
    rvDoctors = view.findViewById(R.id.rvDoctors);
    toolbar = view.findViewById(R.id.toolbar);
    searchBar = view.findViewById(R.id.searchBar);
    searchView = view.findViewById(R.id.searchView);
    rvSearchSuggestions = view.findViewById(R.id.rvSearchSuggestions);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void setupSearch() {
    SearchSuggestionHelper<Doctor> searchHelper =
        new SearchSuggestionHelper<>(
            this,
            searchBar,
            searchView,
            rvSearchSuggestions,
            (query, onResult) -> {
              DoctorApi api = ApiClient.api(DoctorApi.class);
              ApiCallback.handle(
                  api.getDoctors(filterHospitalId, null, null, query),
                  this,
                  body -> {
                    List<Doctor> doctors = new ArrayList<>();
                    for (DoctorResponse r : body) doctors.add(mapToDoctor(r));
                    onResult.onResult(doctors);
                  },
                  (code, msg) -> onResult.onResult(new ArrayList<>()));
            },
            new SearchSuggestionHelper.SuggestionBinder<Doctor>() {
              @Override
              public String getTitle(Doctor item) {
                return item.getName();
              }

              @Override
              public String getSubtitle(Doctor item) {
                return item.getSpeciality();
              }

              @Override
              public String getImageUrl(Doctor item) {
                return item.getImageUrl();
              }

              @Override
              public int getPlaceholderRes() {
                return R.drawable.stethoscope;
              }
            },
            doctor -> loadDoctors(doctor.getName()),
            query -> loadDoctors(query));

    searchHelper.attach();
  }

  private void setupRecyclerView() {
    rvDoctors.setItemAnimator(null);

    adapter =
        new DoctorGroupedAdapter(
            this,
            expandedIds,
            new DoctorGroupedAdapter.Callbacks() {
              @Override
              public void onToggleExpand(Doctor doctor) {
                TransitionSet transition =
                    new TransitionSet()
                        .addTransition(new ChangeBounds())
                        .addTransition(new Fade(Fade.IN))
                        .setDuration(280)
                        .setInterpolator(new FastOutSlowInInterpolator());
                TransitionManager.beginDelayedTransition(rvDoctors, transition);

                if (expandedIds.contains(doctor.getId())) {
                  expandedIds.remove(doctor.getId());
                } else {
                  expandedIds.add(doctor.getId());
                }
                adapter.notifyDataSetChanged();
              }

              @Override
              public void onBookClicked(Doctor doctor) {
                DoctorFragment.this.onBookClicked(doctor);
              }

              @Override
              public String getHospitalName(int hospitalId, TextView target) {
                String cached = hospitalNameCache.get(hospitalId);
                if (cached != null) return cached;
                loadHospitalName(hospitalId, target);
                return "Loading hospital...";
              }
            });

    rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvDoctors.setAdapter(adapter);
  }

  private void loadHospitalName(int hospitalId, TextView target) {
    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitalDetail(hospitalId),
        this,
        h -> {
          hospitalNameCache.put(hospitalId, h.getName());
          if (isAdded()) target.setText(h.getName());
        },
        (code, msg) -> target.setText("Hospital #" + hospitalId));
  }

  private void onBookClicked(Doctor doctor) {
    Bundle args = new Bundle();
    args.putString("doctor_id", doctor.getId());
    args.putString("doctor_name", doctor.getName());
    args.putString("doctor_speciality", doctor.getSpeciality());
    args.putString("doctor_info", doctor.getInfo());
    args.putString("doctor_department", doctor.getDepartment());
    args.putString("doctor_image_url", doctor.getImageUrl());

    BookAppointmentFragment fragment = new BookAppointmentFragment();
    fragment.setArguments(args);
    ((MainTabActivity) requireActivity()).pushFragment(fragment);
  }

  private void loadDoctors(String search) {
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getDoctors(filterHospitalId, null, null, search),
        this,
        body -> {
          List<Doctor> doctors = new ArrayList<>();
          for (DoctorResponse r : body) {
            doctors.add(mapToDoctor(r));
          }
          adapter.submitList(doctors);
        },
        ApiCallback.simpleError(requireContext(), "Failed to load doctors."));
  }

  private Doctor mapToDoctor(DoctorResponse r) {
    String info =
        r.getYears_experience() != null
            ? r.getYears_experience() + "+ Years Exp"
            : (r.getBio() != null ? r.getBio() : "");
    return new Doctor(
        String.valueOf(r.getId()),
        r.getName(),
        r.getSpeciality(),
        info,
        r.getDepartment(),
        r.getPhone(),
        ApiClient.mediaUrl(r.getProfile_pic_url()),
        r.getBio(),
        r.getAddress(),
        r.getHospital_id(),
        r.getYears_experience(),
        r.isIs_verified());
  }
}
