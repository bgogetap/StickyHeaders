package com.brandongogetap.stickyheaders.exposed;

import android.support.v7.widget.RecyclerView;

import java.util.List;

public interface StickyHeaderHandler {

    /**
     * @return The dataset supplied to the {@link RecyclerView.Adapter}
     */
    List<?> getAdapterData();
}
