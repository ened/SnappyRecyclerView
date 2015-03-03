package com.sebroth.snappyrecyclerview;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RecyclerView with a few extra features.
 * <p/>
 * Overview:
 * <ol>
 * <li>"snap-to-column" which automatically adjusts the scroll destination to always snap to a column on the begin. Note: To enable the
 * snap
 * feature, ensure to use a fully configured {@link com.sebroth.snappyrecyclerview.SnappyLayoutManager} implementation as
 * layout manager</li>
 * <li>Supports the display of empty views as a replacement, if the adapter becomes empty.</li>
 * </ol>
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 * @see #setLayoutManager(android.support.v7.widget.RecyclerView.LayoutManager)
 */
public class SnappyRecyclerView extends RecyclerView {

    @SuppressWarnings("UnusedDeclaration")
    private static Logger log = Logger.getLogger(SnappyRecyclerView.class.getName());
    private int emptyViewId = -1;

    public SnappyRecyclerView(Context context) {
        super(context);
    }

    public SnappyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SnappyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private AdapterDataObserver emptyViewDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            toggleEmptyView();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
//            toggleEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            toggleEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            toggleEmptyView();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
//            toggleEmptyView();
        }

        private void toggleEmptyView() {
            final int count = getAdapter().getItemCount();
            final View emptyView = ((ViewGroup) getParent()).findViewById(emptyViewId);

            log.log(Level.FINEST, "emptyView: " + emptyView);

            if (emptyView == null) {
                return;
            }

//            final int visibilityStart = count > 0 ? View.VISIBLE : View.GONE;
            final int visibilityEnd = count > 0 ? View.GONE : View.VISIBLE;
//            final int animationId = count > 0 ? R.anim.empty_view_disappear : R.anim.empty_view_appear;

            if (emptyView.getVisibility() == visibilityEnd) {
                return;
            }

            emptyView.setVisibility(visibilityEnd);
        }
    };

    /**
     * Sets the empty view ID, which will be shown, once the adapter becomes empty.
     *
     * @param emptyViewId Resource ID for the view to show.
     */
    public void setEmptyViewId(@IdRes int emptyViewId) {
        this.emptyViewId = emptyViewId;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if (getAdapter() != null) {
            getAdapter().registerAdapterDataObserver(emptyViewDataObserver);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(emptyViewDataObserver);
        }
    }

    /**
     * Overrides the standard fling procedure, if the layout manager is a {@link com.sebroth.snappyrecyclerview.SnappyLayoutManager} and
     * calculates a new target position.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (getLayoutManager() instanceof SnappyLayoutManager) {
            final SnappyLayoutManager layoutManager = (SnappyLayoutManager) getLayoutManager();

            if (Math.abs(velocityX) > 400) {
                super.smoothScrollToPosition(layoutManager.getPositionForVelocity(velocityX, velocityY));
            } else {
                smoothScrollToPosition(layoutManager.getFixScrollPos());
            }

            return true;
        }

        return super.fling(velocityX, velocityY);
    }

    /**
     * Overrides the standard touch event handling, if the layout manager is a {@link com.sebroth.snappyrecyclerview.SnappyLayoutManager}:
     * If the touch is UP or cancelled or when the scroll state is idle, then view will scroll to the nearest column.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // We want the parent to handle all touch events--there's a lot going on there,
        // and there is no reason to overwrite that functionality--bad things will happen.
        final boolean ret = super.onTouchEvent(e);

        if (getLayoutManager() instanceof SnappyLayoutManager && (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) && getScrollState() == SCROLL_STATE_IDLE) {
            final SnappyLayoutManager layoutManager = (SnappyLayoutManager) getLayoutManager();

            // The layout manager is a SnappyLayoutManager, which means that the
            // children should be snapped to a grid at the end of a drag or
            // fling. The motion event is either a user lifting their finger or
            // the cancellation of a motion events, so this is the time to take
            // over the scrolling to perform our own functionality.
            // Finally, the scroll state is idle--meaning that the resultant
            // velocity after the user's gesture was below the threshold, and
            // no fling was performed, so the view may be in an unaligned state
            // and will not be flung to a proper state.
            smoothScrollToPosition(layoutManager.getFixScrollPos());
        }

        return ret;
    }
}