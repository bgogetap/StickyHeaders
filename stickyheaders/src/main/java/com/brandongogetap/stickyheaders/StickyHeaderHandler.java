package com.brandongogetap.stickyheaders;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

public interface StickyHeaderHandler {

    /**
     * @return The dataset supplied to the {@link RecyclerView.Adapter}
     */
    List<?> getAdapterData();

    /**
     * The {@link ViewGroup} hosting the {@link RecyclerView}.
     * <p>
     * This ViewGroup must be a FrameLayout or CoordinatorLayout, otherwise an
     * {@link IllegalArgumentException} will be thrown.
     * <p>
     * This is required because the sticky view will be added to this ViewGroup, so any sort of
     * automated child layout behavior (such as linear stacking from a {@link LinearLayout} would
     * disrupt the desired position of the header view.
     *
     * @return The {@link ViewGroup} hosting the {@link RecyclerView}
     */
    ViewGroup getRecyclerParent();
}
