package com.bca.medisync.util;

import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bumptech.glide.Glide;

public class ImageLoader {

  public static void loadProfilePic(Fragment fragment, ImageView imageView, String path) {
    String url = ApiClient.mediaUrl(path);
    if (url == null) {
      imageView.setImageResource(R.drawable.ic_nav_profile);
      return;
    }
    Glide.with(fragment)
        .load(url)
        .placeholder(R.drawable.ic_nav_profile)
        .error(R.drawable.ic_nav_profile)
        .centerCrop()
        .into(imageView);
  }

  public static void loadProfilePic(
      android.content.Context context, ImageView imageView, String path) {
    String url = ApiClient.mediaUrl(path);
    if (url == null) {
      imageView.setImageResource(R.drawable.ic_nav_profile);
      return;
    }
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.ic_nav_profile)
        .error(R.drawable.ic_nav_profile)
        .centerCrop()
        .into(imageView);
  }

  public static void loadTinted(
      Fragment fragment, ImageView imageView, String url, int placeholderRes) {
    if (url == null || url.isEmpty()) {
      imageView.setImageResource(placeholderRes);
      return;
    }
    imageView.setImageTintList(null);
    Glide.with(fragment)
        .load(url)
        .placeholder(placeholderRes)
        .error(placeholderRes)
        .into(imageView);
  }
}
