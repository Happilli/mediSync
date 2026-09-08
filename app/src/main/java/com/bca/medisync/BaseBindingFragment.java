package com.bca.medisync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.util.ApiErrorHandler;
import retrofit2.Call;

public abstract class BaseBindingFragment<VB extends ViewBinding> extends Fragment {
  protected VB binding;

  protected abstract VB inflateBinding(LayoutInflater inflater, ViewGroup container);

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = inflateBinding(inflater, container);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  protected <T> void call(
      Call<T> apiCall, ApiCallback.OnSuccess<T> onSuccess, String fallbackErrorMessage) {
    ApiCallback.handle(
        apiCall,
        this,
        onSuccess,
        ApiErrorHandler.with(requireContext()).fallback(fallbackErrorMessage).build());
  }

  protected <T> void call(
      Call<T> apiCall, ApiCallback.OnSuccess<T> onSuccess, ApiCallback.OnError onError) {
    ApiCallback.handle(apiCall, this, onSuccess, onError);
  }
}
