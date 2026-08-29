package com.bca.medisync.patient;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.Hospital;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.hospital.HospitalResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalFragment extends Fragment {
  private RecyclerView rvHospitals;
  private MaterialToolbar toolbar;
  private SimpleListAdapter<Hospital> adapter;
  private TextInputEditText etSearch;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_hospital, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView(view);
    setupToolbar();
    setupRecycleView();
    setupSearch();
    loadHospitals(null);
  }

  private void initView(View view) {
    rvHospitals = view.findViewById(R.id.rvHospitals);
    toolbar = view.findViewById(R.id.toolbar);
    etSearch = view.findViewById(R.id.etSearch);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void setupRecycleView() {
    adapter =
        new SimpleListAdapter<>(
            R.layout.item_hospital,
            new ArrayList<>(),
            (itemView, hospital, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtHospitalName)).setText(hospital.getName());
              ((TextView) itemView.findViewById(R.id.txtHospitalAddress))
                  .setText(hospital.getAddress());
              TextView rating = itemView.findViewById(R.id.txtHospitalRating);
              rating.setText(
                  hospital.getRating() > 0
                      ? String.format(Locale.getDefault(), "%.1f", hospital.getRating())
                      : "");
              itemView
                  .findViewById(R.id.btnViewMore)
                  .setOnClickListener(v -> onHospitalClicked(hospital));
            },
            null,
            (hospital, q) ->
                hospital.getName().toLowerCase().contains(q)
                    || hospital.getAddress().toLowerCase().contains(q));

    rvHospitals.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvHospitals.setAdapter(adapter);
  }

  private void onHospitalClicked(Hospital hospital) {
    Bundle args = new Bundle();
    args.putString("hospital_id", hospital.getId());
    args.putString("hospital_name", hospital.getName());
    args.putString("hospital_address", hospital.getAddress());
    args.putString("hospital_phone", hospital.getPhone());
    args.putString("hospital_website", hospital.getWebsite());
    args.putString("hospital_description", hospital.getDescription());
    args.putDouble("hospital_rating", hospital.getRating());

    HospitalDetailFragment fragment = new HospitalDetailFragment();
    fragment.setArguments(args);
    ((MainTabActivity) requireActivity()).pushFragment(fragment);
  }

  private void loadHospitals(String search) {
    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitals(search),
        this,
        body -> {
          List<Hospital> hospitals = new ArrayList<>();
          for (HospitalResponse r : body) {
            hospitals.add(mapToHospital(r));
          }
          adapter.updateData(hospitals);
        },
        ApiCallback.simpleError(requireContext(), "Failed to load hospitals."));
  }

  private Hospital mapToHospital(HospitalResponse r) {
    return new Hospital(
        String.valueOf(r.getId()),
        r.getName(),
        r.getAddress(),
        r.getPhone(),
        r.getWebsite(),
        r.getDescription(),
        0.0,
        r.getImage_url());
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
