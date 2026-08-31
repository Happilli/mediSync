package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.SearchSuggestionHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalFragment extends Fragment {
  private RecyclerView rvHospitals;
  private RecyclerView rvSearchSuggestions;
  private MaterialToolbar toolbar;
  private SearchBar searchBar;
  private SearchView searchView;
  private SimpleListAdapter<Hospital> adapter;
  private LoadingIndicator loadingIndicator;

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
    searchBar = view.findViewById(R.id.searchBar);
    searchView = view.findViewById(R.id.searchView);
    rvSearchSuggestions = view.findViewById(R.id.rvSearchSuggestions);
    loadingIndicator = view.findViewById(R.id.loadingIndicator);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void setupSearch() {
    SearchSuggestionHelper<Hospital> searchHelper =
        new SearchSuggestionHelper<>(
            this,
            searchBar,
            searchView,
            rvSearchSuggestions,
            (query, onResult) -> {
              HospitalApi api = ApiClient.api(HospitalApi.class);
              ApiCallback.handle(
                  api.getHospitals(query),
                  this,
                  body -> {
                    List<Hospital> hospitals = new ArrayList<>();
                    for (HospitalResponse r : body) hospitals.add(mapToHospital(r));
                    onResult.onResult(hospitals);
                  },
                  (code, msg) -> onResult.onResult(new ArrayList<>()));
            },
            new SearchSuggestionHelper.SuggestionBinder<Hospital>() {
              @Override
              public String getTitle(Hospital item) {
                return item.getName();
              }

              @Override
              public String getSubtitle(Hospital item) {
                return item.getAddress();
              }

              @Override
              public String getImageUrl(Hospital item) {
                return ApiClient.mediaUrl(item.getImageUrl());
              }

              @Override
              public int getPlaceholderRes() {
                return R.drawable.ic_medisync_logo;
              }
            },
            hospital -> loadHospitals(hospital.getName()),
            query -> loadHospitals(query));

    searchHelper.attach();
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

              ImageView imgHospital = itemView.findViewById(R.id.imgHospital);
              ImageLoader.loadHospitalImage(
                  HospitalFragment.this, imgHospital, hospital.getImageUrl());
              itemView
                  .findViewById(R.id.btnViewMore)
                  .setOnClickListener(v -> onHospitalClicked(hospital));
            },
            null);

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

    args.putString("hospital_image_url", hospital.getImageUrl());

    HospitalDetailFragment fragment = new HospitalDetailFragment();
    fragment.setArguments(args);
    ((MainTabActivity) requireActivity()).pushFragment(fragment);
  }

  private void loadHospitals(String search) {
    LoadingHelper.show(loadingIndicator);
    rvHospitals.setVisibility(View.GONE);

    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitals(search),
        this,
        body ->
            LoadingHelper.hide(
                loadingIndicator,
                () -> {
                  rvHospitals.setVisibility(View.VISIBLE);
                  List<Hospital> hospitals = new ArrayList<>();
                  for (HospitalResponse r : body) {
                    hospitals.add(mapToHospital(r));
                  }
                  adapter.updateData(hospitals);
                }),
        (code, msg) ->
            LoadingHelper.hide(
                loadingIndicator,
                () -> {
                  rvHospitals.setVisibility(View.VISIBLE);
                  ApiCallback.simpleError(requireContext(), "Failed to load hospitals.")
                      .run(code, msg);
                }));
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
}
