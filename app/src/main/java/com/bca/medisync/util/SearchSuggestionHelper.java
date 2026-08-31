package com.bca.medisync.util;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionHelper<T> {

  public interface SearchCall<T> {
    void search(String query, OnResult<T> onResult);
  }

  public interface OnResult<T> {
    void onResult(List<T> results);
  }

  public interface SuggestionBinder<T> {
    String getTitle(T item);

    String getSubtitle(T item);

    String getImageUrl(T item);

    int getPlaceholderRes();
  }

  public interface OnSuggestionSelected<T> {
    void onSelected(T item);
  }

  public interface OnSubmit {
    void onSubmit(String query);
  }

  private static final long DEBOUNCE_MS = 350;

  private final Fragment fragment;
  private final SearchBar searchBar;
  private final SearchView searchView;
  private final RecyclerView recyclerView;
  private final SearchCall<T> searchCall;
  private final SuggestionBinder<T> binder;
  private final OnSuggestionSelected<T> onSelected;
  private final OnSubmit onSubmit;

  private final Handler debounceHandler = new Handler(Looper.getMainLooper());
  private Runnable pendingSearch;
  private SimpleListAdapter<T> suggestionsAdapter;

  public SearchSuggestionHelper(
      Fragment fragment,
      SearchBar searchBar,
      SearchView searchView,
      RecyclerView recyclerView,
      SearchCall<T> searchCall,
      SuggestionBinder<T> binder,
      OnSuggestionSelected<T> onSelected,
      OnSubmit onSubmit) {
    this.fragment = fragment;
    this.searchBar = searchBar;
    this.searchView = searchView;
    this.recyclerView = recyclerView;
    this.searchCall = searchCall;
    this.binder = binder;
    this.onSelected = onSelected;
    this.onSubmit = onSubmit;
  }

  public void attach() {
    suggestionsAdapter =
        new SimpleListAdapter<>(
            R.layout.item_search_suggestion,
            new ArrayList<>(),
            this::bindRow,
            item -> {
              searchBar.setText(binder.getTitle(item));
              searchView.hide();
              onSelected.onSelected(item);
            });

    recyclerView.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
    recyclerView.setAdapter(suggestionsAdapter);

    searchView
        .getEditText()
        .setOnEditorActionListener(
            (v, actionId, event) -> {
              String query =
                  searchView.getText() != null ? searchView.getText().toString().trim() : "";
              searchBar.setText(query);
              searchView.hide();
              onSubmit.onSubmit(query.isEmpty() ? null : query);
              return false;
            });

    searchView
        .getEditText()
        .addTextChangedListener(
            new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pendingSearch != null) debounceHandler.removeCallbacks(pendingSearch);
                String query = s.toString();
                pendingSearch = () -> runSearch(query);
                debounceHandler.postDelayed(pendingSearch, DEBOUNCE_MS);
              }

              @Override
              public void afterTextChanged(Editable s) {}
            });
  }

  private void bindRow(android.view.View itemView, T item, int pos) {
    ((TextView) itemView.findViewById(R.id.txtSuggestionTitle)).setText(binder.getTitle(item));
    TextView subtitle = itemView.findViewById(R.id.txtSuggestionSubtitle);
    String sub = binder.getSubtitle(item);
    subtitle.setText(sub);
    subtitle.setVisibility(
        sub == null || sub.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);

    ImageView icon = itemView.findViewById(R.id.imgSuggestionIcon);
    ImageLoader.load(fragment, icon, binder.getImageUrl(item), binder.getPlaceholderRes());
  }

  private void runSearch(String query) {
    if (query == null || query.trim().isEmpty()) {
      suggestionsAdapter.updateData(new ArrayList<>());
      return;
    }
    searchCall.search(query, results -> suggestionsAdapter.updateData(results));
  }
}
