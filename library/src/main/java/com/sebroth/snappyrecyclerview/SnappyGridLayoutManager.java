package com.sebroth.snappyrecyclerview;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.logging.Logger;

/**
 * Grid Layout Manager that supports snap to position
 * <p/>
 * This grid layout manager, in combination with modifications in {@link com.sebroth.snappyrecyclerview.SnappyRecyclerView} provides snapping
 * to positions
 * during fling or regular scroll movements.
 * <p/>
 * Original source: http://stackoverflow.com/q/26370289/375209
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public class SnappyGridLayoutManager extends GridLayoutManager implements SnappyLayoutManager {
    @SuppressWarnings("UnusedDeclaration")
    private static Logger log = Logger.getLogger(SnappyGridLayoutManager.class.getName());

    public SnappyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        calculateDeceleration(context);
    }

    public SnappyGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
        calculateDeceleration(context);
    }

    // These variables are from android.widget.Scroller, which is used, via ScrollerCompat, by
    // Recycler View. The scrolling distance calculation logic originates from the same place. Want
    // to use their variables so as to approximate the look of normal Android scrolling.
    // Find the Scroller fling implementation in android.widget.Scroller.fling().
    private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private static double FRICTION = 0.84;

    private double deceleration;

    private void calculateDeceleration(Context context) {
        deceleration = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.3700787 // inches per meter
                // pixels per inch. 160 is the "default" dpi, i.e. one dip is one pixel on a 160 dpi
                // screen
                * context.getResources().getDisplayMetrics().density * 160.0f * FRICTION;
    }

    @Override
    public int getPositionForVelocity(int velocityX, int velocityY) {
        if (getChildCount() == 0) {
            return 0;
        }

        if (getOrientation() == HORIZONTAL) {
            return calcPosForVelocity(velocityX, getChildAt(0).getLeft(), getChildAt(0).getWidth(), getPosition(getChildAt(0)));
        } else {
            return calcPosForVelocity(velocityY, getChildAt(0).getTop(), getChildAt(0).getHeight(), getPosition(getChildAt(0)));
        }
    }

    private int calcPosForVelocity(int velocity, int scrollPos, int childSize, int currPos) {
        final double v = Math.sqrt(velocity * velocity);
        final double dist = getSplineFlingDistance(v);

        final double tempScroll = scrollPos + (velocity > 0 ? dist : -dist);

//        log.log(Level.FINEST, "tempScroll: {0}, r: {1}, currPos: {2}", new Object[]{tempScroll, (int) Math.max(currPos + tempScroll / childSize, 0), currPos});

        int newPos;
        if (velocity < 0) {
            // Not sure if I need to lower bound this here.
            newPos = (int) Math.max(currPos + tempScroll / childSize + getSpanCount(), 0);
        } else {
            newPos = (int) (currPos + (tempScroll / childSize) + getSpanCount());
        }

//        log.log(Level.FINEST, "curr: {0}, vel: {1}, new: {2}", new Object[]{currPos, velocity, newPos});

        return newPos;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            // I want a behavior where the scrolling always snaps to the beginning of
            // the list. Snapping to end is also trivial given the default implementation.
            // If you need a different behavior, you may need to override more
            // of the LinearSmoothScrolling methods.
            protected int getHorizontalSnapPreference() {
                return SNAP_TO_START;
            }

            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return SnappyGridLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    private double getSplineFlingDistance(double velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return ViewConfiguration.getScrollFriction() * deceleration * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    private double getSplineDeceleration(double velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (ViewConfiguration.getScrollFriction() * deceleration));
    }

    /**
     * This implementation obviously doesn't take into account the direction of the
     * that preceded it, but there is no easy way to get that information without more
     * hacking than I was willing to put into it.
     */
    @Override
    public int getFixScrollPos() {
        if (this.getChildCount() == 0) {
            return 0;
        }

        final View child = getChildAt(0);
        final int childPos = getPosition(child);

        if (getOrientation() == HORIZONTAL && Math.abs(child.getLeft()) > child.getMeasuredWidth() / 2) {
            // Scrolled first view more than halfway offscreen
            return childPos + getSpanCount();
        } else if (getOrientation() == VERTICAL && Math.abs(child.getTop()) > child.getMeasuredWidth() / 2) {
            // Scrolled first view more than halfway offscreen
            return childPos + getSpanCount();
        }
        return childPos;
    }
}
