package com.bca.medisync.patient;

import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.GroupedListAdapter;
import com.bca.medisync.data.model.Doctor;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.SearchSuggestionHelper;
import com.bca.medisync.util.SearchableListFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DoctorFragment extends SearchableListFragment<Doctor> {
  private RecyclerView rvDoctors;
  private GroupedListAdapter<Doctor> adapter;

  private Integer filterHospitalId;
  private final Set<String> expandedIds = new HashSet<>();
  private final Map<Integer, String> hospitalNameCache = new HashMap<>();

  @Override
  protected int getLayoutRes() {
    return R.layout.fragment_doctor;
  }

  @Override
  protected void onInit(@Nullable Bundle args) {
    if (args == null) return;
    String hospitalIdStr = args.getString("hospital_id");
    if (hospitalIdStr != null) {
      try {
        filterHospitalId = Integer.parseInt(hospitalIdStr);
      } catch (NumberFormatException ignored) {
      }
    }
  }

  @Override
  protected void setupResultsView(@NonNull View view) {
    rvDoctors = view.findViewById(R.id.rvDoctors);
    rvDoctors.setItemAnimator(null);

    adapter =
        new GroupedListAdapter<>(
            R.layout.item_doctor_row,
            Doctor::getDepartment,
            (itemView, doctor, posInGroup, groupSize) -> bindDoctorRow(itemView, doctor),
            this::onRowClicked);

    rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvDoctors.setAdapter(adapter);
  }

  @Override
  protected void search(String query, SearchSuggestionHelper.OnResult<Doctor> onResult) {
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
  }

  @Override
  protected SearchSuggestionHelper.SuggestionBinder<Doctor> getSuggestionBinder() {
    return new SearchSuggestionHelper.SuggestionBinder<Doctor>() {
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
    };
  }

  @Override
  protected void onSuggestionSelected(Doctor doctor) {
    loadResults(doctor.getName());
  }

  @Override
  protected void loadResults(@Nullable String query) {
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getDoctors(filterHospitalId, null, null, query),
        this,
        body -> {
          List<Doctor> doctors = new ArrayList<>();
          for (DoctorResponse r : body) doctors.add(mapToDoctor(r));
          adapter.submitList(doctors);
        },
        ApiCallback.simpleError(requireContext(), "Failed to load doctors."));
  }

  private void bindDoctorRow(View itemView, Doctor doctor) {
    ((TextView) itemView.findViewById(R.id.txtDoctorName)).setText(doctor.getName());
    ((TextView) itemView.findViewById(R.id.txtSpeciality)).setText(doctor.getSpeciality());
    ((TextView) itemView.findViewById(R.id.txtInfo)).setText(doctor.getInfo());
    ImageLoader.loadDoctorImage(this, itemView.findViewById(R.id.imgDoctor), doctor.getImageUrl());

    boolean expanded = expandedIds.contains(doctor.getId());
    View divider = itemView.findViewById(R.id.dividerExpand);
    View detail = itemView.findViewById(R.id.detailContainer);
    divider.setVisibility(expanded ? View.VISIBLE : View.GONE);
    detail.setVisibility(expanded ? View.VISIBLE : View.GONE);
    itemView.findViewById(R.id.imgExpandArrow).setRotation(expanded ? 90f : 0f);

    if (expanded) {
      TextView txtBio = itemView.findViewById(R.id.txtBio);
      boolean hasBio = doctor.getBio() != null && !doctor.getBio().isEmpty();
      txtBio.setVisibility(hasBio ? View.VISIBLE : View.GONE);
      txtBio.setText(doctor.getBio());
      ((TextView) itemView.findViewById(R.id.txtDoctorAddress)).setText(doctor.getAddress());
      ((TextView) itemView.findViewById(R.id.txtDoctorPhone)).setText(doctor.getPhone());

      TextView txtHospital = itemView.findViewById(R.id.txtHospitalName);
      String cached = hospitalNameCache.get(doctor.getHospitalId());
      if (cached != null) {
        txtHospital.setText(cached);
      } else {
        txtHospital.setText("Loading hospital...");
        loadHospitalName(doctor.getHospitalId(), txtHospital);
      }
      itemView.findViewById(R.id.btnBook).setOnClickListener(v -> onBookClicked(doctor));
    }
  }

  private void onRowClicked(Doctor doctor) {
    TransitionSet transition =
        new TransitionSet()
            .addTransition(new ChangeBounds())
            .addTransition(new Fade(Fade.IN))
            .setDuration(280)
            .setInterpolator(new FastOutSlowInInterpolator());
    TransitionManager.beginDelayedTransition(rvDoctors, transition);

    if (expandedIds.contains(doctor.getId())) expandedIds.remove(doctor.getId());
    else expandedIds.add(doctor.getId());
    adapter.notifyDataSetChanged();
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
