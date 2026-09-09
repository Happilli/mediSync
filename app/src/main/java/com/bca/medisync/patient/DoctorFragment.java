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
import com.bca.medisync.databinding.ItemDoctorRowBinding;
import com.bca.medisync.util.ApiErrorHandler;
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
  private GroupedListAdapter<Doctor, ItemDoctorRowBinding> adapter;
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
            ItemDoctorRowBinding::inflate,
            Doctor::department,
            (rowBinding, doctor, posInGroup, groupSize) -> bindDoctorRow(rowBinding, doctor),
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
        return item.name();
      }

      @Override
      public String getSubtitle(Doctor item) {
        return item.speciality();
      }

      @Override
      public String getImageUrl(Doctor item) {
        return item.imageUrl();
      }

      @Override
      public int getPlaceholderRes() {
        return R.drawable.stethoscope;
      }
    };
  }

  @Override
  protected void onSuggestionSelected(Doctor doctor) {
    loadResults(doctor.name());
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
        ApiErrorHandler.with(requireContext()).fallback("Failed to load doctors.").build());
  }

  private void bindDoctorRow(ItemDoctorRowBinding binding, Doctor doctor) {
    binding.txtDoctorName.setText(doctor.name());
    binding.txtSpeciality.setText(doctor.speciality());
    binding.txtInfo.setText(doctor.info());
    ImageLoader.loadDoctorImage(this, binding.imgDoctor, doctor.imageUrl());
    boolean expanded = expandedIds.contains(doctor.id());
    binding.dividerExpand.setVisibility(expanded ? View.VISIBLE : View.GONE);
    binding.detailContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);
    binding.imgExpandArrow.setRotation(expanded ? 90f : 0f);
    if (expanded) {
      boolean hasBio = doctor.bio() != null && !doctor.bio().isEmpty();
      binding.txtBio.setVisibility(hasBio ? View.VISIBLE : View.GONE);
      binding.txtBio.setText(doctor.bio());
      binding.txtDoctorAddress.setText(doctor.address());
      binding.txtDoctorPhone.setText(doctor.phone());
      String cached = hospitalNameCache.get(doctor.hospitalId());
      if (cached != null) {
        binding.txtHospitalName.setText(cached);
      } else {
        binding.txtHospitalName.setText("Loading hospital...");
        loadHospitalName(doctor.hospitalId(), binding.txtHospitalName);
      }
      binding.btnBook.setOnClickListener(v -> onBookClicked(doctor));
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

    if (expandedIds.contains(doctor.id())) expandedIds.remove(doctor.id());
    else expandedIds.add(doctor.id());
    adapter.notifyDataSetChanged();
  }

  private void loadHospitalName(int hospitalId, TextView target) {
    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitalDetail(hospitalId),
        this,
        h -> {
          hospitalNameCache.put(hospitalId, h.name());
          if (isAdded()) target.setText(h.name());
        },
        (code, msg) -> target.setText("Hospital #" + hospitalId));
  }

  private void onBookClicked(Doctor doctor) {
    Bundle args = new Bundle();
    args.putString("doctor_id", doctor.id());
    args.putString("doctor_name", doctor.name());
    args.putString("doctor_speciality", doctor.speciality());
    args.putString("doctor_info", doctor.info());
    args.putString("doctor_department", doctor.department());
    args.putString("doctor_image_url", doctor.imageUrl());
    BookAppointmentFragment fragment = new BookAppointmentFragment();
    fragment.setArguments(args);
    ((MainTabActivity) requireActivity()).pushFragment(fragment);
  }

  private Doctor mapToDoctor(DoctorResponse r) {
    String info =
        r.years_experience() != null
            ? r.years_experience() + "+ Years Exp"
            : (r.bio() != null ? r.bio() : "");
    return new Doctor(
        String.valueOf(r.id()),
        r.name(),
        r.speciality(),
        info,
        r.department(),
        r.phone(),
        ApiClient.mediaUrl(r.profile_pic_url()),
        r.bio(),
        r.address(),
        r.hospital_id(),
        r.years_experience(),
        r.is_verified());
  }
}
