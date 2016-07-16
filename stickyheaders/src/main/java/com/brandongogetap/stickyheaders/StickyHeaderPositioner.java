package com.brandongogetap.stickyheaders;

import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewTreeObserver;

import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

import java.util.List;
import java.util.Map;

final class StickyHeaderPositioner {

    private static final int INVALID_POSITION = -1;

    private final StickyHeaderHandler stickyHeaderHandler;

    private View currentHeader;
    private int lastBoundPosition = INVALID_POSITION;
    private int lastFirstVisiblePosition = INVALID_POSITION;
    private List<Integer> headerPositions;
    private int orientation;
    private boolean dirty;

    StickyHeaderPositioner(StickyHeaderHandler stickyHeaderHandler) {
        this.stickyHeaderHandler = stickyHeaderHandler;
    }

    void setHeaderPositions(List<Integer> headerPositions) {
        this.headerPositions = headerPositions;
    }

    void updateHeaderState(int firstVisiblePosition, ViewRetriever viewRetriever) {
        if (lastFirstVisiblePosition == firstVisiblePosition) {
            // Already checked header state for this effective scroll position
            return;
        }
        int headerPositionToShow = getHeaderPositionToShow(firstVisiblePosition);
        if (headerPositionToShow != lastBoundPosition) {
            if (headerPositionToShow == INVALID_POSITION) {
                detachHeader();
                lastBoundPosition = INVALID_POSITION;
            } else {
                View view = viewRetriever.getViewForPosition(headerPositionToShow);
                attachHeader(view);
                lastBoundPosition = headerPositionToShow;
            }
        }
        lastFirstVisiblePosition = firstVisiblePosition;
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
            break;
        }
        currentHeader.setVisibility(View.VISIBLE);
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

    private int getHeaderPositionToShow(int firstVisiblePosition) {
        int headerPositionToShow = INVALID_POSITION;
        for (Integer headerPosition : headerPositions) {
            if (headerPosition <= firstVisiblePosition) {
                headerPositionToShow = headerPosition;
            } else {
                break;
            }
        }
        return headerPositionToShow;
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
                        stickyHeaderHandler.getRecyclerParent().requestLayout();
                        checkHeaderPositions(visibleHeaders);
                    }
                });
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
        stickyHeaderHandler.getRecyclerParent().addView(view);
        dirty = false;
    }

    private void detachHeader() {
        if (currentHeader != null) {
            stickyHeaderHandler.getRecyclerParent().removeView(currentHeader);
            currentHeader = null;
        }
    }

    void reset(int orientation, int firstVisiblePosition) {
        this.orientation = orientation;
        // Don't reset/detach if we are going to reattach the same header
        if (getHeaderPositionToShow(firstVisiblePosition) == lastBoundPosition) return;

        dirty = true;
        safeDetachHeader();
        lastBoundPosition = INVALID_POSITION;
        lastFirstVisiblePosition = INVALID_POSITION;
    }

    /**
     * Detaching while {@link StickyLayoutManager} is laying out children can cause an inconsistent
     * state in the child count variable in {@link android.widget.FrameLayout} layoutChildren method
     */
    private void safeDetachHeader() {
        stickyHeaderHandler.getRecyclerParent().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            stickyHeaderHandler.getRecyclerParent().getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            stickyHeaderHandler.getRecyclerParent().getViewTreeObserver()
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
