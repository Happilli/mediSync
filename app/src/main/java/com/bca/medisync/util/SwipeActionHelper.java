package com.bca.medisync.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;

public class SwipeActionHelper {

  public interface DirectionProvider {
    int getMovementFlags(int position);
  }

  public interface OnSwipeAction {
    void onSwiped(int position, int direction);
  }

  private final Fragment fragment;
  private final DirectionProvider directionProvider;
  private final OnSwipeAction onSwipeAction;

  @ColorRes private int rightBubbleColor = R.color.tertiary_container;
  @ColorRes private int rightIconColor = R.color.on_tertiary_container;
  @ColorRes private int leftBubbleColor = R.color.error_container;
  @ColorRes private int leftIconColor = R.color.on_error_container;

  public SwipeActionHelper(
      Fragment fragment, DirectionProvider directionProvider, OnSwipeAction onSwipeAction) {
    this.fragment = fragment;
    this.directionProvider = directionProvider;
    this.onSwipeAction = onSwipeAction;
  }

  public SwipeActionHelper withColors(
      @ColorRes int rightBubbleColor,
      @ColorRes int rightIconColor,
      @ColorRes int leftBubbleColor,
      @ColorRes int leftIconColor) {
    this.rightBubbleColor = rightBubbleColor;
    this.rightIconColor = rightIconColor;
    this.leftBubbleColor = leftBubbleColor;
    this.leftIconColor = leftIconColor;
    return this;
  }

  public void attachTo(RecyclerView recyclerView) {
    new ItemTouchHelper(new Callback(recyclerView)).attachToRecyclerView(recyclerView);
  }

  private int dpToPx(int dp) {
    return ViewUtils.dp(fragment.requireContext(), dp);
  }

  private class Callback extends ItemTouchHelper.SimpleCallback {
    private final RecyclerView recyclerView;

    Callback(RecyclerView recyclerView) {
      super(0, 0);
      this.recyclerView = recyclerView;
    }

    @Override
    public int getMovementFlags(
        @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder viewHolder) {
      int position = viewHolder.getAbsoluteAdapterPosition();
      if (position == RecyclerView.NO_POSITION) return 0;
      int directions = directionProvider.getMovementFlags(position);
      return directions == 0 ? 0 : makeMovementFlags(0, directions);
    }

    @Override
    public boolean onMove(
        @NonNull RecyclerView rv,
        @NonNull RecyclerView.ViewHolder viewHolder,
        @NonNull RecyclerView.ViewHolder target) {
      return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
      int position = viewHolder.getAbsoluteAdapterPosition();
      if (position == RecyclerView.NO_POSITION) return;
      onSwipeAction.onSwiped(position, direction);
    }

    @Override
    public void onChildDraw(
        @NonNull Canvas c,
        @NonNull RecyclerView rv,
        @NonNull RecyclerView.ViewHolder viewHolder,
        float dX,
        float dY,
        int actionState,
        boolean isCurrentlyActive) {
      View item = viewHolder.itemView;
      if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && Math.abs(dX) > 4) {
        boolean swipingRight = dX > 0;
        float gapWidth = Math.abs(dX);
        float gapMargin = dpToPx(6);
        float top = item.getTop() + gapMargin;
        float bottom = item.getBottom() - gapMargin;
        float bubbleHeight = bottom - top;

        RectF bubbleRect =
            swipingRight
                ? new RectF(
                    item.getLeft() + gapMargin, top, item.getLeft() + gapWidth - gapMargin, bottom)
                : new RectF(
                    item.getRight() - gapWidth + gapMargin,
                    top,
                    item.getRight() - gapMargin,
                    bottom);

        float radius = Math.min(bubbleHeight / 2f, bubbleRect.width() / 2f);

        Paint bubblePaint = new Paint();
        bubblePaint.setAntiAlias(true);
        bubblePaint.setColor(
            ContextCompat.getColor(
                fragment.requireContext(), swipingRight ? rightBubbleColor : leftBubbleColor));
        c.drawRoundRect(bubbleRect, radius, radius, bubblePaint);

        float maxSwipe = item.getWidth() * 0.5f;
        float progress = Math.min(gapWidth / maxSwipe, 1f);
        float cx = bubbleRect.centerX();
        float cy = bubbleRect.centerY();

        if (bubbleRect.width() > dpToPx(40)) {
          Paint iconPaint = new Paint();
          iconPaint.setAntiAlias(true);
          iconPaint.setColor(
              ContextCompat.getColor(
                  fragment.requireContext(), swipingRight ? rightIconColor : leftIconColor));
          iconPaint.setStrokeWidth(2.5f * fragment.getResources().getDisplayMetrics().density);
          iconPaint.setStrokeCap(Paint.Cap.ROUND);
          iconPaint.setStyle(Paint.Style.STROKE);

          float iconSize = dpToPx(9) + dpToPx(3) * progress;

          if (swipingRight) {
            c.drawLine(cx - iconSize, cy, cx - iconSize * 0.25f, cy + iconSize * 0.8f, iconPaint);
            c.drawLine(
                cx - iconSize * 0.25f,
                cy + iconSize * 0.8f,
                cx + iconSize,
                cy - iconSize * 0.7f,
                iconPaint);
          } else {
            float s = iconSize * 0.85f;
            c.drawLine(cx - s, cy - s, cx + s, cy + s, iconPaint);
            c.drawLine(cx + s, cy - s, cx - s, cy + s, iconPaint);
          }
        }
      }
      super.onChildDraw(c, rv, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
  }
}
