package com.bca.medisync.util;

import android.content.Context;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;

public class ImageLoader {
  private static void load(
      RequestManager requestManager,
      ImageView imageView,
      String fullUrl,
      int placeholderRes,
      boolean centerCrop) {
    if (fullUrl == null || fullUrl.isEmpty()) {
      imageView.setImageResource(placeholderRes);
      return;
    }
    RequestBuilder<android.graphics.drawable.Drawable> request =
        requestManager.load(fullUrl).placeholder(placeholderRes).error(placeholderRes);
    if (centerCrop) request = request.centerCrop();
    request.into(imageView);
  }

  public static void load(
      Fragment fragment, ImageView imageView, String fullUrl, int placeholderRes) {
    load(Glide.with(fragment), imageView, fullUrl, placeholderRes, true);
  }

  public static void load(
      Context context, ImageView imageView, String fullUrl, int placeholderRes) {
    load(Glide.with(context), imageView, fullUrl, placeholderRes, true);
  }

  public static void loadTinted(
      Fragment fragment, ImageView imageView, String fullUrl, int placeholderRes) {
    imageView.setImageTintList(null);
    load(Glide.with(fragment), imageView, fullUrl, placeholderRes, false);
  }

  public static void loadProfilePic(Fragment fragment, ImageView imageView, String path) {
    load(fragment, imageView, ApiClient.mediaUrl(path), R.drawable.ic_nav_profile);
  }

  public static void loadProfilePic(Context context, ImageView imageView, String path) {
    load(context, imageView, ApiClient.mediaUrl(path), R.drawable.ic_nav_profile);
  }

  public static void loadHospitalImage(Fragment fragment, ImageView imageView, String path) {
    load(fragment, imageView, ApiClient.mediaUrl(path), R.drawable.ic_medisync_logo);
  }

  public static void loadHospitalImage(Context context, ImageView imageView, String path) {
    load(context, imageView, ApiClient.mediaUrl(path), R.drawable.ic_medisync_logo);
  }

  public static void loadDoctorImage(Fragment fragment, ImageView imageView, String fullUrl) {
    load(fragment, imageView, fullUrl, R.drawable.stethoscope);
  }
}
