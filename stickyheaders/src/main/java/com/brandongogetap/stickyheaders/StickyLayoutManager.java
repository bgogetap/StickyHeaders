package com.brandongogetap.stickyheaders;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brandongogetap.stickyheaders.ViewRetriever.RecyclerViewRetriever;
import com.brandongogetap.stickyheaders.exposed.StickyHeader;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StickyLayoutManager extends LinearLayoutManager {

    private StickyHeaderPositioner positioner;
    private StickyHeaderHandler headerHandler;
    private List<Integer> headerPositions = new ArrayList<>();
    private RecyclerViewRetriever viewRetriever;
    private int headerElevation = StickyHeaderPositioner.NO_ELEVATION;
    /**
     * If {@code false}, the {@link android.view.ViewParent} of this layout manager's {@link RecyclerView}
     * will not be validated against supported types.
     * <p>
     * See {@link #disableParentViewRestrictions()}
     */
    private boolean enforceParentViewRestrictions = true;
    @Nullable
    private StickyHeaderListener listener;

    public StickyLayoutManager(Context context, StickyHeaderHandler headerHandler) {
        this(context, VERTICAL, false, headerHandler);
        init(headerHandler);
    }

    public StickyLayoutManager(Context context, int orientation, boolean reverseLayout, StickyHeaderHandler headerHandler) {
        super(context, orientation, reverseLayout);
        init(headerHandler);
    }

    private void init(StickyHeaderHandler stickyHeaderHandler) {
        Preconditions.checkNotNull(stickyHeaderHandler, "StickyHeaderHandler == null");
        this.headerHandler = stickyHeaderHandler;
    }

    /**
     * Register a callback to be invoked when a header is attached/re-bound or detached.
     *
     * @param listener The callback that will be invoked, or null to unset.
     */
    public void setStickyHeaderListener(@Nullable StickyHeaderListener listener) {
        this.listener = listener;
        if (positioner != null) {
            positioner.setListener(listener);
        }
    }

    /**
     * Enable or disable elevation for Sticky Headers.
     * <p>
     * If you want to specify a specific amount of elevation, use
     * {@link StickyLayoutManager#elevateHeaders(int)}
     *
     * @param elevateHeaders Enable Sticky Header elevation. Default is false.
     */
    public void elevateHeaders(boolean elevateHeaders) {
        this.headerElevation = elevateHeaders ?
                StickyHeaderPositioner.DEFAULT_ELEVATION : StickyHeaderPositioner.NO_ELEVATION;
        elevateHeaders(headerElevation);
    }

    /**
     * Enable Sticky Header elevation with a specific amount.
     *
     * @param dp elevation in dp
     */
    public void elevateHeaders(int dp) {
        this.headerElevation = dp;
        if (positioner != null) {
            positioner.setElevateHeaders(dp);
        }
    }

    /**
     * Used to override the default behavior of validating supported
     * {@link android.view.ViewParent}s of the {@link RecyclerView}.
     * <p>
     * This is opt-in because of broken behavior that may occur when, for example, the RecyclerView
     * parent is a LinearLayout.
     */
    public void disableParentViewRestrictions() {
        this.enforceParentViewRestrictions = false;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        cacheHeaderPositions();
        if (positioner != null) {
            runPositionerInit();
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        if (Math.abs(scroll) > 0) {
            if (positioner != null) {
                positioner.updateHeaderState(
                        findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever, findFirstCompletelyVisibleItemPosition() == 0);
            }
        }
        return scroll;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollHorizontallyBy(dx, recycler, state);
        if (Math.abs(scroll) > 0) {
            if (positioner != null) {
                positioner.updateHeaderState(
                        findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever, findFirstCompletelyVisibleItemPosition() == 0);
            }
        }
        return scroll;
    }

    @Override
    public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
        super.removeAndRecycleAllViews(recycler);
        if (positioner != null) {
            positioner.clearHeader();
        }
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        if (enforceParentViewRestrictions) {
            Preconditions.validateParentView(view);
        }
        viewRetriever = new RecyclerViewRetriever(view);
        positioner = new StickyHeaderPositioner(view);
        positioner.setElevateHeaders(headerElevation);
        positioner.setListener(listener);
        if (headerPositions.size() > 0) {
            // Layout has already happened and header positions are cached. Catch positioner up.
            positioner.setHeaderPositions(headerPositions);
            runPositionerInit();
        }
        super.onAttachedToWindow(view);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        if (positioner != null) {
            positioner.clearVisibilityObserver();
        }
        super.onDetachedFromWindow(view, recycler);
    }

    private void runPositionerInit() {
        positioner.reset(getOrientation());
        positioner.updateHeaderState(findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever, findFirstCompletelyVisibleItemPosition() == 0);
    }

    private Map<Integer, View> getVisibleHeaders() {
        Map<Integer, View> visibleHeaders = new LinkedHashMap<>();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int dataPosition = getPosition(view);
            if (headerPositions.contains(dataPosition)) {
                visibleHeaders.put(dataPosition, view);
            }
        }
        return visibleHeaders;
    }

    private void cacheHeaderPositions() {
        headerPositions.clear();
        List<?> adapterData = headerHandler.getAdapterData();
        if (adapterData == null) {
            if (positioner != null) {
                positioner.setHeaderPositions(headerPositions);
            }
            return;
        }

        for (int i = 0; i < adapterData.size(); i++) {
            if (adapterData.get(i) instanceof StickyHeader) {
                headerPositions.add(i);
            }
        }
        if (positioner != null) {
            positioner.setHeaderPositions(headerPositions);
        }
    }
}
