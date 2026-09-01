package com.bca.medisync.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUploadHelper {

  public static File copyUriToCache(Context context, Uri uri, String prefix) throws Exception {
    ContentResolver resolver = context.getContentResolver();
    String mimeType = resolver.getType(uri);
    String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    if (ext == null) ext = "jpg";
    File outFile =
        new File(context.getCacheDir(), prefix + "_" + System.currentTimeMillis() + "." + ext);
    try (InputStream in = resolver.openInputStream(uri);
        FileOutputStream out = new FileOutputStream(outFile)) {
      byte[] buffer = new byte[8192];
      int read;
      while (in != null && (read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    }
    return outFile;
  }

  public static MultipartBody.Part toImagePart(File file, String fieldName) {
    RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/*"));
    return MultipartBody.Part.createFormData(fieldName, file.getName(), fileBody);
  }
}
