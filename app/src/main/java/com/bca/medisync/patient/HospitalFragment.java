package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.bca.medisync.util.SearchableListFragment;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.HeroCarouselStrategy;
import com.google.android.material.loadingindicator.LoadingIndicator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HospitalFragment extends SearchableListFragment<Hospital> {
  private RecyclerView rvHospitals;
  private RecyclerView rvHospitalsCarousel;
  private SimpleListAdapter<Hospital> adapter;
  private SimpleListAdapter<Hospital> carouselAdapter;
  private LoadingIndicator loadingIndicator;
  private View scrollContent;

  private final Set<String> expandedHospitalIds = new HashSet<>();

  @Override
  protected int getLayoutRes() {
    return R.layout.fragment_hospital;
  }

  @Override
  protected void setupResultsView(@NonNull View view) {
    rvHospitals = view.findViewById(R.id.rvHospitals);
    rvHospitalsCarousel = view.findViewById(R.id.rvHospitalsCarousel);
    scrollContent = view.findViewById(R.id.scrollContent);
    loadingIndicator = view.findViewById(R.id.loadingIndicator);

    setupCarousel();
    setupRecycleView();
  }

  private void setupCarousel() {
    rvHospitalsCarousel.setLayoutManager(new CarouselLayoutManager(new HeroCarouselStrategy()));

    carouselAdapter =
        new SimpleListAdapter<>(
            R.layout.item_hospital_carousel,
            new ArrayList<>(),
            (itemView, hospital, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtHospitalName)).setText(hospital.getName());
              ((TextView) itemView.findViewById(R.id.txtHospitalAddress))
                  .setText(hospital.getAddress());
              TextView rating = itemView.findViewById(R.id.txtHospitalRating);
              rating.setText(
                  hospital.getRating() > 0
                      ? "\u2605 " + String.format(Locale.getDefault(), "%.1f", hospital.getRating())
                      : "");

              ImageView imgHospital = itemView.findViewById(R.id.imgHospital);
              ImageLoader.loadHospitalImage(
                  HospitalFragment.this, imgHospital, hospital.getImageUrl());

              View overlay = itemView.findViewById(R.id.overlayScrim);
              View textContainer = itemView.findViewById(R.id.textContainer);
              boolean expanded = expandedHospitalIds.contains(hospital.getId());
              overlay.setVisibility(expanded ? View.VISIBLE : View.GONE);
              textContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);

              itemView.setOnClickListener(
                  v -> {
                    if (expandedHospitalIds.contains(hospital.getId())) {
                      onHospitalClicked(hospital);
                    } else {
                      expandedHospitalIds.clear();
                      expandedHospitalIds.add(hospital.getId());
                      carouselAdapter.notifyDataSetChanged();
                    }
                  });

              itemView
                  .findViewById(R.id.btnViewMore)
                  .setOnClickListener(v -> onHospitalClicked(hospital));
            },
            null);

    rvHospitalsCarousel.setAdapter(carouselAdapter);
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
                      ? "\u2605 " + String.format(Locale.getDefault(), "%.1f", hospital.getRating())
                      : "");
              itemView
                  .findViewById(R.id.btnViewMore)
                  .setOnClickListener(v -> onHospitalClicked(hospital));
            },
            null);

    rvHospitals.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvHospitals.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  @Override
  protected void search(String query, SearchSuggestionHelper.OnResult<Hospital> onResult) {
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
  }

  @Override
  protected SearchSuggestionHelper.SuggestionBinder<Hospital> getSuggestionBinder() {
    return new SearchSuggestionHelper.SuggestionBinder<Hospital>() {
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
    };
  }

  @Override
  protected void onSuggestionSelected(Hospital hospital) {
    loadResults(hospital.getName());
  }

  @Override
  protected void loadResults(@Nullable String query) {
    updateCarouselVisibility(query);
    LoadingHelper.show(loadingIndicator);
    scrollContent.setVisibility(View.GONE);

    HospitalApi api = ApiClient.api(HospitalApi.class);
    ApiCallback.handle(
        api.getHospitals(query),
        this,
        LoadingHelper.wrapSuccess(
            loadingIndicator,
            scrollContent,
            body -> {
              List<Hospital> hospitals = new ArrayList<>();
              for (HospitalResponse r : body) hospitals.add(mapToHospital(r));
              adapter.updateData(hospitals);
              boolean isSearch = query != null && !query.trim().isEmpty();
              if (!isSearch) carouselAdapter.updateData(hospitals);
            }),
        LoadingHelper.wrapError(
            loadingIndicator,
            scrollContent,
            ApiCallback.simpleError(requireContext(), "Failed to load hospitals.")));
  }

  private void updateCarouselVisibility(String search) {
    boolean isSearch = search != null && !search.trim().isEmpty();
    int visibility = isSearch ? View.GONE : View.VISIBLE;
    rvHospitalsCarousel.setVisibility(visibility);
    View root = getView();
    if (root == null) return;
    View lblFeatured = root.findViewById(R.id.lblFeaturedHospitals);
    if (lblFeatured != null) lblFeatured.setVisibility(visibility);
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
