package com.brandongogetap.stickyheaders.demo;

import android.content.Context;

import com.brandongogetap.stickyheaders.StickyLayoutManager;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

public final class TopSnappedStickyLayoutManager extends StickyLayoutManager {

    TopSnappedStickyLayoutManager(Context context, StickyHeaderHandler headerHandler, int headerMarginTop ) {
        super(context, headerHandler, headerMarginTop);
    }

    @Override public void scrollToPosition(int position) {
        super.scrollToPositionWithOffset(position, 0);
    }
}
