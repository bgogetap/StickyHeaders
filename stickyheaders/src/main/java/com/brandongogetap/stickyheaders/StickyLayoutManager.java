package com.brandongogetap.stickyheaders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.brandongogetap.stickyheaders.ViewRetriever.RecyclerViewRetriever;
import com.brandongogetap.stickyheaders.exposed.StickyHeader;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StickyLayoutManager extends LinearLayoutManager {

    private StickyHeaderPositioner positioner;
    private StickyHeaderHandler headerHandler;
    private List<Integer> headerPositions;
    private RecyclerViewRetriever viewRetriever;

    public StickyLayoutManager(Context context, StickyHeaderHandler headerHandler) {
        this(context, VERTICAL, false, headerHandler);
        setStickyHeaderHandler(headerHandler);
    }

    public StickyLayoutManager(Context context, int orientation, boolean reverseLayout, StickyHeaderHandler headerHandler) {
        super(context, orientation, reverseLayout);
        setStickyHeaderHandler(headerHandler);
    }

    private void setStickyHeaderHandler(StickyHeaderHandler headerHandler) {
        Preconditions.checkNotNull(headerHandler, "StickyHeaderHandler == null");
        Preconditions.validateParentView(headerHandler);
        this.headerHandler = headerHandler;
        positioner = new StickyHeaderPositioner(headerHandler);
        headerPositions = new ArrayList<>();
        viewRetriever = new RecyclerViewRetriever();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        cacheHeaderPositions();
        positioner.reset(getOrientation(), findFirstVisibleItemPosition());
        positioner.updateHeaderState(
                findFirstVisibleItemPosition(), viewRetriever.setRecycler(recycler));
        positioner.checkHeaderPositions(getVisibleHeaders());
    }

    private void cacheHeaderPositions() {
        headerPositions.clear();
        for (int i = 0; i < headerHandler.getAdapterData().size(); i++) {
            if (headerHandler.getAdapterData().get(i) instanceof StickyHeader) {
                headerPositions.add(i);
            }
        }
        positioner.setHeaderPositions(headerPositions);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        if (Math.abs(scroll) > 0) {
            positioner.updateHeaderState(
                    findFirstVisibleItemPosition(), viewRetriever.setRecycler(recycler));
            positioner.checkHeaderPositions(getVisibleHeaders());
        }
        return scroll;
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

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollHorizontallyBy(dx, recycler, state);
        if (Math.abs(scroll) > 0) {
            positioner.updateHeaderState(
                    findFirstVisibleItemPosition(), viewRetriever.setRecycler(recycler));
            positioner.checkHeaderPositions(getVisibleHeaders());
        }
        return scroll;
    }
}
