package com.brandongogetap.stickyheaders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

interface ViewRetriever {

    View getViewForPosition(int position);

    final class RecyclerViewRetriever implements ViewRetriever {

        private RecyclerView.Recycler recycler;

        RecyclerViewRetriever() {
        }

        RecyclerViewRetriever setRecycler(RecyclerView.Recycler recycler) {
            this.recycler = recycler;
            return this;
        }

        @Override public View getViewForPosition(int position) {
            View view = recycler.getViewForPosition(position);
            recycler = null;
            return view;
        }
    }
}
