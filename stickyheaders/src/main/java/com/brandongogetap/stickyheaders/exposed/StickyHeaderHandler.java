package com.brandongogetap.stickyheaders.exposed;

import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import java.util.List;

public interface StickyHeaderHandler {

    /**
     * @return The dataset supplied to the {@link RecyclerView.Adapter}
     */
    List<?> getAdapterData();

    /**
     * <p>
     * This RecyclerView must have a parent that is either a FrameLayout or CoordinatorLayout,
     * otherwise an {@link IllegalArgumentException} will be thrown.
     * <p>
     * This is required because the sticky view will be added to the parent ViewGroup, so any sort
     * of automated child layout behavior (such as linear stacking from a {@link LinearLayout} would
     * disrupt the desired position of the header view.
     */
    RecyclerView getRecyclerView();
}
