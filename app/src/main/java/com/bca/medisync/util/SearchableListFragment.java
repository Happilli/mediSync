package com.bca.medisync.util;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

public abstract class SearchableListFragment<T> extends Fragment {

  protected MaterialToolbar toolbar;
  protected SearchBar searchBar;
  protected SearchView searchView;
  protected RecyclerView rvSearchSuggestions;

  @LayoutRes
  protected abstract int getLayoutRes();

  protected void onInit(@Nullable Bundle args) {}

  protected abstract void setupResultsView(@NonNull View view);

  protected abstract void search(String query, SearchSuggestionHelper.OnResult<T> onResult);

  protected abstract SearchSuggestionHelper.SuggestionBinder<T> getSuggestionBinder();

  protected abstract void onSuggestionSelected(T item);

  protected abstract void loadResults(@Nullable String query);

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(getLayoutRes(), container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    onInit(getArguments());

    toolbar = view.findViewById(R.id.toolbar);
    searchBar = view.findViewById(R.id.searchBar);
    searchView = view.findViewById(R.id.searchView);
    rvSearchSuggestions = view.findViewById(R.id.rvSearchSuggestions);

    if (toolbar != null) {
      toolbar.setNavigationOnClickListener(
          v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    setupResultsView(view);

    new SearchSuggestionHelper<>(
            this,
            searchBar,
            searchView,
            rvSearchSuggestions,
            this::search,
            getSuggestionBinder(),
            this::onSuggestionSelected,
            this::loadResults)
        .attach();

    loadResults(null);
  }
}
