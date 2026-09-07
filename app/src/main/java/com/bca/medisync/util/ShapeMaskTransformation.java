package com.bca.medisync.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ShapeMaskTransformation extends BitmapTransformation {
  private final int maskRes;
  private final Context appContext;
  private static final String ID = "com.bca.medisync.util.ShapeMaskTransformation";

  public ShapeMaskTransformation(Context context, @DrawableRes int maskRes) {
    this.appContext = context.getApplicationContext();
    this.maskRes = maskRes;
  }

  @Override
  protected Bitmap transform(
      @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
    Bitmap result = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(result);

    Bitmap scaled = centerCrop(toTransform, outWidth, outHeight);
    canvas.drawBitmap(scaled, 0, 0, null);

    Drawable maskDrawable = ContextCompat.getDrawable(appContext, maskRes);
    Bitmap mask = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
    Canvas maskCanvas = new Canvas(mask);
    if (maskDrawable != null) {
      maskDrawable.setBounds(0, 0, outWidth, outHeight);
      maskDrawable.draw(maskCanvas);
    }

    Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    canvas.drawBitmap(mask, 0, 0, maskPaint);

    if (!scaled.equals(toTransform)) scaled.recycle();
    mask.recycle();
    return result;
  }

  private Bitmap centerCrop(Bitmap source, int width, int height) {
    float scale = Math.max((float) width / source.getWidth(), (float) height / source.getHeight());
    int scaledW = Math.round(scale * source.getWidth());
    int scaledH = Math.round(scale * source.getHeight());
    Bitmap scaled = Bitmap.createScaledBitmap(source, scaledW, scaledH, true);
    int x = (scaledW - width) / 2;
    int y = (scaledH - height) / 2;
    return Bitmap.createBitmap(scaled, Math.max(x, 0), Math.max(y, 0), width, height);
  }

  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update((ID + maskRes).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ShapeMaskTransformation && ((ShapeMaskTransformation) o).maskRes == maskRes;
  }

  @Override
  public int hashCode() {
    return ID.hashCode() + maskRes;
  }
}
