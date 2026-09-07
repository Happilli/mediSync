package com.bca.medisync.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class MorphShapeView extends View {
  private static final int POINTS = 24;

  public static class Shapes {
    public static final float[] ROUND = generate(0, 0f);
    public static final float[] CLOVER = generate(4, 0.22f);
    public static final float[] BURST = generate(10, 0.32f);
    public static final float[] SOFTBOOM = generate(6, 0.28f);
    public static final float[] GEM = generate(5, 0.18f);
    public static final float[] PENTAGON = generate(5, 0.1f);
    public static final float[] FLOWER = generate(8, 0.26f);

    private static float[] generate(int lobes, float amp) {
      float[] r = new float[POINTS];
      for (int i = 0; i < POINTS; i++) {
        double theta = (2 * Math.PI * i) / POINTS;
        double bump = lobes == 0 ? 0 : (Math.cos(lobes * theta) + 1) / 2.0;
        r[i] = (float) (1f - amp + amp * bump);
      }
      return r;
    }
  }

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path path = new Path();
  private float[] currentRadii = Shapes.ROUND.clone();
  private float[] fromRadii = Shapes.ROUND.clone();
  private float[] targetRadii = Shapes.ROUND.clone();
  private ValueAnimator animator;
  private float cx, cy, baseRadius;

  public MorphShapeView(Context context) {
    super(context);
    paint.setStyle(Paint.Style.FILL);
  }

  public void setShapeColor(int color) {
    paint.setColor(color);
    invalidate();
  }

  public void setShapeImmediate(float[] radii) {
    if (animator != null) animator.cancel();
    currentRadii = radii.clone();
    targetRadii = radii.clone();
    invalidate();
  }

  public void morphTo(float[] newTarget, long duration) {
    morphTo(newTarget, duration, null);
  }

  public void morphTo(float[] newTarget, long duration, Runnable onEnd) {
    if (animator != null) animator.cancel();
    fromRadii = currentRadii.clone();
    targetRadii = newTarget.clone();
    animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setDuration(duration);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.addUpdateListener(
        a -> {
          float t = (float) a.getAnimatedValue();
          for (int i = 0; i < POINTS; i++) {
            currentRadii[i] = fromRadii[i] + (targetRadii[i] - fromRadii[i]) * t;
          }
          invalidate();
        });
    if (onEnd != null) {
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              onEnd.run();
            }
          });
    }
    animator.start();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    cx = w / 2f;
    cy = h / 2f;
    baseRadius = Math.min(w, h) / 2f * 0.85f;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    buildPath();
    canvas.drawPath(path, paint);
  }

  private void buildPath() {
    path.reset();
    int n = POINTS;
    float[] xs = new float[n];
    float[] ys = new float[n];
    for (int i = 0; i < n; i++) {
      double theta = (2 * Math.PI * i) / n;
      float r = baseRadius * currentRadii[i];
      xs[i] = (float) (cx + r * Math.cos(theta));
      ys[i] = (float) (cy + r * Math.sin(theta));
    }
    path.moveTo((xs[0] + xs[n - 1]) / 2f, (ys[0] + ys[n - 1]) / 2f);
    for (int i = 0; i < n; i++) {
      int next = (i + 1) % n;
      float midX = (xs[i] + xs[next]) / 2f;
      float midY = (ys[i] + ys[next]) / 2f;
      path.quadTo(xs[i], ys[i], midX, midY);
    }
    path.close();
  }

  public void destroy() {
    if (animator != null) animator.cancel();
  }
}
