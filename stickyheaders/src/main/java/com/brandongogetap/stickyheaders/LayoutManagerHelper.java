package com.brandongogetap.stickyheaders;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.brandongogetap.stickyheaders.exposed.StickyHeader;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class LayoutManagerHelper {

    private int headerElevation = StickyHeaderPositioner.NO_ELEVATION;
    private StickyHeaderHandler headerHandler;
    private List<Integer> headerPositions = new ArrayList<>();
    private final LinearLayoutManager layoutManager;
    private ViewRetriever.RecyclerViewRetriever viewRetriever;
    private StickyHeaderPositioner positioner;
    private StickyHeaderListener stickyHeaderListener;
    private int left;
    private int top;
    private int right;
    private int bottom;

    LayoutManagerHelper(@NonNull final LinearLayoutManager layoutManager, StickyHeaderHandler stickyHeaderHandler) {
        this.layoutManager = layoutManager;
        this.headerHandler = stickyHeaderHandler;
    }

    void handleVerticalScroll(final int scroll) {
        if (Math.abs(scroll) > 0) {
            if (positioner != null) {
                positioner.updateHeaderState(
                        layoutManager.findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever, layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        }
    }

    public void onAttachedToWindow(RecyclerView view) {
        Preconditions.validateParentView(view);
        viewRetriever = new ViewRetriever.RecyclerViewRetriever(view);
        positioner = new StickyHeaderPositioner(view);
        positioner.setElevateHeaders(headerElevation);
        positioner.setListener(stickyHeaderListener);
        if (headerPositions.size() > 0) {
            // Layout has already happened and header positions are cached. Catch positioner up.
            positioner.setHeaderPositions(headerPositions);
            runPositionerInit();
        }
    }

    void handleHorizontalScroll(int scroll) {
        if (Math.abs(scroll) > 0) {
            if (positioner != null) {
                positioner.updateHeaderState(
                        layoutManager.findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever, layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        }
    }

    void runPositionerInit() {
        positioner.reset(layoutManager.getOrientation());
        positioner.updateHeaderState(layoutManager.findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever, layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
    }

    private Map<Integer, View> getVisibleHeaders() {
        Map<Integer, View> visibleHeaders = new LinkedHashMap<>();

        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            View view = layoutManager.getChildAt(i);
            int dataPosition = layoutManager.getPosition(view);
            if (headerPositions.contains(dataPosition)) {
                visibleHeaders.put(dataPosition, view);
            }
        }
        return visibleHeaders;
    }

    void cacheHeaderPositions() {
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

    void setStickyHeaderListener(StickyHeaderListener stickyHeaderListener) {
        if (positioner != null) {
            positioner.setListener(stickyHeaderListener);
        }
        this.stickyHeaderListener = stickyHeaderListener;
    }

    /**
     * Enable or disable elevation for Sticky Headers.
     * <p>
     * If you want to specify a specific amount of elevation, use
     * {@link StickyLayoutManager#elevateHeaders(int)}
     *
     * @param elevateHeaders Enable Sticky Header elevation. Default is false.
     */
    void elevateHeaders(boolean elevateHeaders) {
        this.headerElevation = elevateHeaders ?
                StickyHeaderPositioner.DEFAULT_ELEVATION : StickyHeaderPositioner.NO_ELEVATION;
        elevateHeaders(headerElevation);
    }

    /**
     * Enable Sticky Header elevation with a specific amount.
     *
     * @param dp elevation in dp
     */
    void elevateHeaders(int dp) {
        this.headerElevation = dp;
        if (positioner != null) {
            positioner.setElevateHeaders(dp);
        }
    }

    void removeAndRecycleViews() {
        if (positioner != null) {
            positioner.clearHeader();
        }
    }
}
