package com.brandongogetap.stickyheaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

public class StickyGridLayoutManager extends GridLayoutManager {

    private LayoutManagerHelper layoutManagerHelper;

    public StickyGridLayoutManager(@NonNull final Context context, final int span, @NonNull final StickyHeaderHandler headerHandler) {
        super(context, span);
        init(headerHandler);
    }

    private void init(StickyHeaderHandler stickyHeaderHandler) {
        Preconditions.checkNotNull(stickyHeaderHandler, "StickyHeaderHandler == null");
        layoutManagerHelper = new LayoutManagerHelper(this, stickyHeaderHandler);
    }

    /**
     * Register a callback to be invoked when a header is attached/re-bound or detached.
     *
     * @param listener The callback that will be invoked, or null to unset.
     */
    public void setStickyHeaderListener(@Nullable StickyHeaderListener listener) {
        layoutManagerHelper.setStickyHeaderListener(listener);
    }

    /**
     * Enable or disable elevation for Sticky Headers.
     * <p>
     * If you want to specify a specific amount of elevation, use
     * {@link StickyGridLayoutManager#elevateHeaders(int)}
     *
     * @param elevateHeaders Enable Sticky Header elevation. Default is false.
     */
    public void elevateHeaders(boolean elevateHeaders) {
        layoutManagerHelper.elevateHeaders(elevateHeaders);
    }

    /**
     * Enable Sticky Header elevation with a specific amount.
     *
     * @param dp elevation in dp
     */
    public void elevateHeaders(int dp) {
        layoutManagerHelper.elevateHeaders(dp);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        layoutManagerHelper.cacheHeaderPositions();
        if (layoutManagerHelper != null) {
            layoutManagerHelper.runPositionerInit();
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        layoutManagerHelper.handleVerticalScroll(scroll);
        return scroll;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollHorizontallyBy(dx, recycler, state);
        layoutManagerHelper.handleHorizontalScroll(scroll);
        return scroll;
    }

    @Override
    public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
        super.removeAndRecycleAllViews(recycler);
        layoutManagerHelper.removeAndRecycleViews();
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        layoutManagerHelper.onAttachedToWindow(view);
        super.onAttachedToWindow(view);
    }
}
