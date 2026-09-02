package com.bca.medisync.util;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.bca.medisync.data.remote.ApiCallback;
import java.io.File;
import okhttp3.MultipartBody;
import retrofit2.Call;

public class ProfilePicUploader {

  public interface CallProvider<T> {
    Call<T> createCall(MultipartBody.Part filePart);
  }

  public static <T> void upload(
      Context context,
      Fragment fragmentOrNull,
      Uri uri,
      String prefix,
      CallProvider<T> callProvider,
      ApiCallback.OnSuccess<T> onSuccess) {
    File cachedFile;
    try {
      cachedFile = FileUploadHelper.copyUriToCache(context, uri, prefix);
    } catch (Exception e) {
      Toast.makeText(context, "Couldn't read the selected photo!", Toast.LENGTH_SHORT).show();
      return;
    }
    MultipartBody.Part filePart = FileUploadHelper.toImagePart(cachedFile, "file");
    ApiCallback.handle(
        callProvider.createCall(filePart),
        fragmentOrNull,
        onSuccess,
        (code, msg) -> {
          if (code == -1) {
            Toast.makeText(context, "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(context, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
          }
        });
  }
}
