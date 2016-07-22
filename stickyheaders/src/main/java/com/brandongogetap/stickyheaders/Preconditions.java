package com.brandongogetap.stickyheaders;

import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.FrameLayout;

final class Preconditions {

    private Preconditions() {

    }

    static <T> T checkNotNull(T item, String message) {
        if (item == null) {
            throw new NullPointerException(message);
        }
        return item;
    }

    static void validateParentView(View recyclerView) {
        View parentView = (View) recyclerView.getParent();
        if (!(parentView instanceof FrameLayout) && !(parentView instanceof CoordinatorLayout)) {
            throw new IllegalArgumentException("RecyclerView parent must be either a FrameLayout or CoordinatorLayout");
        }
    }
}
