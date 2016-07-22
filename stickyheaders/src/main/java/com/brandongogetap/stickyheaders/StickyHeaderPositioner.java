package com.brandongogetap.stickyheaders;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;

import java.util.List;
import java.util.Map;

final class StickyHeaderPositioner {

    private static final int INVALID_POSITION = -1;

    private final RecyclerView recyclerView;

    private View currentHeader;
    private int lastBoundPosition = INVALID_POSITION;
    private List<Integer> headerPositions;
    private int orientation;
    private boolean dirty;
    private final boolean checkMargins;

    StickyHeaderPositioner(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        checkMargins = recyclerViewHasPadding();
    }

    void setHeaderPositions(List<Integer> headerPositions) {
        this.headerPositions = headerPositions;
    }

    void updateHeaderState(int firstVisiblePosition, Map<Integer, View> visibleHeaders, ViewRetriever viewRetriever) {
        int headerPositionToShow = getHeaderPositionToShow(firstVisiblePosition, visibleHeaders.get(firstVisiblePosition));
        View headerToCopy = visibleHeaders.get(headerPositionToShow);
        if (headerPositionToShow != lastBoundPosition) {
            if (headerPositionToShow == INVALID_POSITION) {
                detachHeader();
                lastBoundPosition = INVALID_POSITION;
            } else {
                // We don't want to attach yet if header view is not at edge
                if (checkMargins && headerAwayFromEdge(headerToCopy)) return;
                View view = viewRetriever.getViewForPosition(headerPositionToShow);
                attachHeader(view);
                lastBoundPosition = headerPositionToShow;
            }
        } else if (checkMargins) {
            /**
             * This could still be our firstVisiblePosition even if another view is visible above it.
             * See {@link #getHeaderPositionToShow(int, View)} for explanation.
             */
            if (headerAwayFromEdge(headerToCopy)) {
                detachHeader();
                lastBoundPosition = INVALID_POSITION;
            }
        }
        checkHeaderPositions(visibleHeaders);
    }

    // This checks visible headers and their positions to determine if the sticky header needs
    // to be offset. In reality, only the header following the sticky header is checked. Some
    // optimization may be possible here (not storing all visible headers in map).
    void checkHeaderPositions(final Map<Integer, View> visibleHeaders) {
        if (currentHeader == null) return;

        // This can happen after configuration changes.
        if (currentHeader.getHeight() == 0) {
            waitForLayoutAndRetry(visibleHeaders);
            return;
        }
        for (Map.Entry<Integer, View> entry : visibleHeaders.entrySet()) {
            if (entry.getKey() == lastBoundPosition) continue;
            View nextHeader = entry.getValue();
            if (offsetHeader(nextHeader) == -1) {
                resetTranslation();
            }
            currentHeader.setVisibility(View.VISIBLE);
            break;
        }
    }

    private float offsetHeader(View nextHeader) {
        boolean shouldOffsetHeader = shouldOffsetHeader(nextHeader);
        float offset = -1;
        if (shouldOffsetHeader) {
            if (orientation == LinearLayoutManager.VERTICAL) {
                offset = -(currentHeader.getHeight() - nextHeader.getY());
                currentHeader.setTranslationY(offset);
            } else {
                offset = -(currentHeader.getWidth() - nextHeader.getX());
                currentHeader.setTranslationX(offset);
            }
        }
        return offset;
    }

    private boolean shouldOffsetHeader(View nextHeader) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            return nextHeader.getY() < currentHeader.getHeight();
        } else {
            return nextHeader.getX() < currentHeader.getWidth();
        }
    }

    private void resetTranslation() {
        if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader.setTranslationY(0);
        } else {
            currentHeader.setTranslationX(0);
        }
    }

    /**
     * In case of padding, first visible position may not be accurate.
     * <p>
     * Example: RecyclerView has padding of 10dp. With clipToPadding set to false, a visible view
     * above the 10dp threshold will not be recognized as firstVisiblePosition by the LayoutManager.
     * <p>
     * To remedy this, we are checking if the firstVisiblePosition (according to the LayoutManager)
     * is a header (headerForPosition will not be null). If it is, we check it's Y. If #getY is
     * greater than 0 then we know it is actually not the firstVisiblePosition, and return the
     * preceding header position (if available).
     */
    private int getHeaderPositionToShow(int firstVisiblePosition, @Nullable View headerForPosition) {
        int headerPositionToShow = INVALID_POSITION;
        if (headerForPosition != null && headerForPosition.getY() > 0) {
            int offsetHeaderIndex = headerPositions.indexOf(firstVisiblePosition);
            if (offsetHeaderIndex > 0) {
                return headerPositions.get(offsetHeaderIndex - 1);
            }
        }
        for (Integer headerPosition : headerPositions) {
            if (headerPosition <= firstVisiblePosition) {
                headerPositionToShow = headerPosition;
            } else {
                break;
            }
        }
        return headerPositionToShow;
    }

    private void attachHeader(View view) {
        detachHeader();
        this.currentHeader = view;
        // Prevents accessibility events being propagated to the RecyclerView which is no longer the
        // parent of this view.
        currentHeader.setAccessibilityDelegate(null);
        // Set to Invisible until we position it in #checkHeaderPositions.
        currentHeader.setVisibility(View.INVISIBLE);
        currentHeader.setId(R.id.header_view);
        getRecyclerParent().addView(currentHeader);
        if (checkMargins) {
            updateLayoutParams(currentHeader);
        }
        dirty = false;
    }

    private void detachHeader() {
        if (currentHeader != null) {
            getRecyclerParent().removeView(currentHeader);
            currentHeader = null;
        }
    }

    /**
     * Adds margins to left/right (or top/bottom in horizontal orientation)
     * <p>
     * Top padding (or left padding in horizontal orientation) with clipToPadding = true is not
     * supported. If you need to offset the top (or left in horizontal orientation) and do not
     * want scrolling children to be visible, use margins.
     */
    private void updateLayoutParams(View currentHeader) {
        MarginLayoutParams params = (MarginLayoutParams) currentHeader.getLayoutParams();
        matchMarginsToPadding(params);
        currentHeader.setLayoutParams(params);
    }

    private void matchMarginsToPadding(MarginLayoutParams layoutParams) {
        @Px int leftMargin = orientation == LinearLayoutManager.VERTICAL ?
                recyclerView.getPaddingLeft() : 0;
        @Px int topMargin = orientation == LinearLayoutManager.VERTICAL ?
                0 : recyclerView.getPaddingTop();
        @Px int rightMargin = orientation == LinearLayoutManager.VERTICAL ?
                recyclerView.getPaddingRight() : 0;
        layoutParams.setMargins(leftMargin, topMargin, rightMargin, 0);
    }

    private boolean headerAwayFromEdge(View headerToCopy) {
        if (headerToCopy != null) {
            return orientation == LinearLayoutManager.VERTICAL ?
                    headerToCopy.getY() > 0 : headerToCopy.getX() > 0;
        }
        return false;
    }

    void reset(int orientation, int firstVisiblePosition) {
        this.orientation = orientation;
        // Don't reset/detach if we are going to reattach the same header
        if (getHeaderPositionToShow(firstVisiblePosition, null) == lastBoundPosition) return;

        dirty = true;
        safeDetachHeader();
        lastBoundPosition = INVALID_POSITION;
    }

    private boolean recyclerViewHasPadding() {
        return recyclerView.getPaddingLeft() > 0
                || recyclerView.getPaddingRight() > 0
                || recyclerView.getPaddingTop() > 0;
    }

    private ViewGroup getRecyclerParent() {
        return (ViewGroup) recyclerView.getParent();
    }

    private void waitForLayoutAndRetry(final Map<Integer, View> visibleHeaders) {
        currentHeader.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        // If header was removed during layout
                        if (currentHeader == null) return;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            currentHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            currentHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        getRecyclerParent().requestLayout();
                        checkHeaderPositions(visibleHeaders);
                    }
                });
    }

    /**
     * Detaching while {@link StickyLayoutManager} is laying out children can cause an inconsistent
     * state in the child count variable in {@link android.widget.FrameLayout} layoutChildren method
     */
    private void safeDetachHeader() {
        getRecyclerParent().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            getRecyclerParent().getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            getRecyclerParent().getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                        }
                        if (dirty) {
                            detachHeader();
                        }
                    }
                });
    }

    @VisibleForTesting int getLastBoundPosition() {
        return lastBoundPosition;
    }
}
