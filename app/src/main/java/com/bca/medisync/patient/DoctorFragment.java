package com.bca.medisync.patient;

import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.Doctor;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.util.ImageLoader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.bumptech.glide.Glide;

public class DoctorFragment extends Fragment {
  private RecyclerView rvDoctors;
  private MaterialToolbar toolbar;
  private TextInputEditText etSearch;
  private SimpleListAdapter<Doctor> adapter;

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
    etSearch = view.findViewById(R.id.etSearch);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void setupRecyclerView() {
    rvDoctors.setItemAnimator(null);

    adapter =
        new SimpleListAdapter<>(
            R.layout.item_doctor,
            new ArrayList<>(),
            (itemView, doctor, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtDoctorName)).setText(doctor.getName());
              ((TextView) itemView.findViewById(R.id.txtSpeciality))
                  .setText(doctor.getSpeciality());
              ((TextView) itemView.findViewById(R.id.txtInfo)).setText(doctor.getInfo());
                ImageView doctorImage = itemView.findViewById(R.id.imgDoctor);

                String imageUrl = doctor.getImageUrl();

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(DoctorFragment.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.stethoscope)
                            .error(R.drawable.stethoscope)
                            .centerCrop()
                            .into(doctorImage);
                } else {
                    doctorImage.setImageResource(R.drawable.stethoscope);
                }

              boolean expanded = expandedIds.contains(doctor.getId());
              View divider = itemView.findViewById(R.id.dividerExpand);
              View detail = itemView.findViewById(R.id.detailContainer);
              divider.setVisibility(expanded ? View.VISIBLE : View.GONE);
              detail.setVisibility(expanded ? View.VISIBLE : View.GONE);

              ImageView expandArrow = itemView.findViewById(R.id.imgExpandArrow);
              expandArrow.setImageResource(R.drawable.arrow);
              expandArrow.setRotation(expanded ? 90f : 0f);

              if (expanded) {
                TextView txtBio = itemView.findViewById(R.id.txtBio);
                TextView txtAddress = itemView.findViewById(R.id.txtDoctorAddress);
                TextView txtHospital = itemView.findViewById(R.id.txtHospitalName);
                TextView txtPhone = itemView.findViewById(R.id.txtDoctorPhone);

                boolean hasBio = doctor.getBio() != null && !doctor.getBio().isEmpty();
                txtBio.setVisibility(hasBio ? View.VISIBLE : View.GONE);
                txtBio.setText(doctor.getBio());
                txtAddress.setText(doctor.getAddress());
                txtPhone.setText(doctor.getPhone());

                String cachedHospital = hospitalNameCache.get(doctor.getHospitalId());
                if (cachedHospital != null) {
                  txtHospital.setText(cachedHospital);
                } else {
                  txtHospital.setText("Loading hospital...");
                  loadHospitalName(doctor.getHospitalId(), txtHospital);
                }

                itemView.findViewById(R.id.btnBook).setOnClickListener(v -> onBookClicked(doctor));
              }

              itemView.setOnClickListener(
                  v -> {
                    TransitionSet transition =
                        new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new Fade(Fade.IN))
                            .setDuration(280)
                            .setInterpolator(new FastOutSlowInInterpolator());
                    TransitionManager.beginDelayedTransition(rvDoctors, transition);

                    boolean nowExpanded = !expandedIds.contains(doctor.getId());
                    if (nowExpanded) expandedIds.add(doctor.getId());
                    else expandedIds.remove(doctor.getId());

                    expandArrow.animate().rotation(nowExpanded ? 90f : 0f).setDuration(280).start();

                    adapter.notifyItemChanged(pos);
                  });
            },
            null,
            (doctor, q) ->
                doctor.getName().toLowerCase().contains(q)
                    || doctor.getSpeciality().toLowerCase().contains(q));

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
          adapter.updateData(doctors);
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

  private void setupSearch() {
    etSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filter(s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }
}
