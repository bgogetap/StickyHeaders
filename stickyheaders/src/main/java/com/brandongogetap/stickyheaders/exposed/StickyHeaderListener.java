package com.brandongogetap.stickyheaders.exposed;

import android.view.View;

import com.brandongogetap.stickyheaders.StickyLayoutManager;

/**
 * A listener that can be set by calling {@link StickyLayoutManager#setStickyHeaderListener(StickyHeaderListener)}
 * <p>
 * As Sticky Header views are new instances of the same views that are shown in the RecyclerView,
 * this listener can be useful if there is state that needs to be transferred to the actual list
 * item view counterparts.
 */
public interface StickyHeaderListener {

    /**
     * Called when a Sticky Header has been attached or rebound.
     *
     * @param headerView      The view that is currently attached as the sticky header
     * @param adapterPosition The position in the adapter data set that this view represents
     */
    void headerAttached(View headerView, int adapterPosition);

    /**
     * Called when a Sticky Header has been detached or is about to be re-bound.
     * <p>
     * For performance reasons, if the new Sticky Header that will be replacing the current one is
     * of the same view type, the view is reused. In that case, this call will be immediately followed
     * by a call to {@link StickyHeaderListener#headerAttached(View, int)} with the same view instance,
     * but after the view is re-bound with the new adapter data.
     * <p>
     * <b>Important</b><br/>
     * {@code adapterPosition} cannot be guaranteed to be the position in the current adapter
     * data set that this view represents. The data may have changed after this view was bound, but
     * before it was detached.
     * <p>
     * It is also possible for {@code adapterPosition} to be -1, though this should be a rare case.
     * <p>
     * In short, be wary about relying on the adapter position provided by this callback unless you are
     * working with a data set that is completely under your control (no user-initiated changes).
     *
     * @param headerView      The view that will be removed from the sticky header position, or soon to be re-bound
     * @param adapterPosition The position in the adapter data set that the header view was created from when originally bound
     */
    void headerDetached(View headerView, int adapterPosition);
}
