package com.bca.medisync.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
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
  private final int borderWidthPx;
  @ColorInt private final int borderColor;
  private static final String ID = "com.bca.medisync.util.ShapeMaskTransformation";

  public ShapeMaskTransformation(Context context, @DrawableRes int maskRes) {
    this(context, maskRes, 0, 0);
  }

  public ShapeMaskTransformation(
      Context context, @DrawableRes int maskRes, int borderWidthPx, @ColorInt int borderColor) {
    this.appContext = context.getApplicationContext();
    this.maskRes = maskRes;
    this.borderWidthPx = borderWidthPx;
    this.borderColor = borderColor;
  }

  @Override
  protected Bitmap transform(
      @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
    Bitmap result = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(result);

    if (borderWidthPx > 0) {
      Bitmap borderMask = maskBitmap(outWidth, outHeight);
      Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      borderPaint.setColorFilter(new PorterDuffColorFilter(borderColor, PorterDuff.Mode.SRC_IN));
      canvas.drawBitmap(borderMask, 0, 0, borderPaint);
      borderMask.recycle();
    }

    int innerWidth = outWidth - borderWidthPx * 2;
    int innerHeight = outHeight - borderWidthPx * 2;

    Bitmap scaled = centerCrop(toTransform, innerWidth, innerHeight);
    Bitmap inner = pool.get(innerWidth, innerHeight, Bitmap.Config.ARGB_8888);
    Canvas innerCanvas = new Canvas(inner);
    innerCanvas.drawBitmap(scaled, 0, 0, null);

    Bitmap innerMask = maskBitmap(innerWidth, innerHeight);
    Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    innerCanvas.drawBitmap(innerMask, 0, 0, maskPaint);
    innerMask.recycle();

    canvas.drawBitmap(inner, borderWidthPx, borderWidthPx, null);

    if (!scaled.equals(toTransform)) scaled.recycle();
    inner.recycle();
    return result;
  }

  private Bitmap maskBitmap(int width, int height) {
    Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas maskCanvas = new Canvas(mask);
    Drawable maskDrawable = ContextCompat.getDrawable(appContext, maskRes);
    if (maskDrawable != null) {
      maskDrawable.setBounds(0, 0, width, height);
      maskDrawable.draw(maskCanvas);
    }
    return mask;
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
    messageDigest.update(
        (ID + maskRes + borderWidthPx + borderColor).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ShapeMaskTransformation)) return false;
    ShapeMaskTransformation other = (ShapeMaskTransformation) o;
    return other.maskRes == maskRes
        && other.borderWidthPx == borderWidthPx
        && other.borderColor == borderColor;
  }

  @Override
  public int hashCode() {
    return ID.hashCode() + maskRes + borderWidthPx + borderColor;
  }
}
