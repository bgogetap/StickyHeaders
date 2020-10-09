package com.brandongogetap.stickyheaders;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import java.util.List;
import java.util.Map;

final class StickyHeaderPositioner {

    static final int NO_ELEVATION = -1;
    static final int DEFAULT_ELEVATION = 5;

    private static final int INVALID_POSITION = -1;

    private final RecyclerView recyclerView;
    private final boolean checkMargins;
    private final ViewTreeObserver.OnGlobalLayoutListener visibilityObserver = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int visibility = StickyHeaderPositioner.this.recyclerView.getVisibility();
            if (currentHeader != null) {
                currentHeader.setVisibility(visibility);
            }
        }
    };

    private View currentHeader;
    private int lastBoundPosition = INVALID_POSITION;
    private List<Integer> headerPositions;
    private int orientation;
    private boolean dirty;
    private float headerElevation = NO_ELEVATION;
    private int cachedElevation = NO_ELEVATION;
    private RecyclerView.ViewHolder currentViewHolder;
    @Nullable private StickyHeaderListener listener;

    StickyHeaderPositioner(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        checkMargins = recyclerViewHasPadding();
        maybeRemoveStaleHeader();
    }

    void setHeaderPositions(List<Integer> headerPositions) {
        this.headerPositions = headerPositions;
    }

    void updateHeaderState(
            int firstVisiblePosition,
            Map<Integer, View> visibleHeaders,
            ViewRetriever viewRetriever,
            boolean atTop) {
        int headerPositionToShow = atTop ? INVALID_POSITION : getHeaderPositionToShow(
                firstVisiblePosition, visibleHeaders.get(firstVisiblePosition));
        View headerToCopy = visibleHeaders.get(headerPositionToShow);
        if (headerPositionToShow != lastBoundPosition) {
            if (headerPositionToShow == INVALID_POSITION ||
                    checkMargins && headerAwayFromEdge(headerToCopy)) { // We don't want to attach yet if header view is not at edge
                dirty = true;
                safeDetachHeader();
                lastBoundPosition = INVALID_POSITION;
            } else {
                lastBoundPosition = headerPositionToShow;
                RecyclerView.ViewHolder viewHolder =
                        viewRetriever.getViewHolderForPosition(headerPositionToShow);
                attachHeader(viewHolder, headerPositionToShow);
            }
        } else if (checkMargins) {
            /*
              This could still be our firstVisiblePosition even if another view is visible above it.
              See `#getHeaderPositionToShow` for explanation.
             */
            if (headerAwayFromEdge(headerToCopy)) {
                detachHeader(lastBoundPosition);
                lastBoundPosition = INVALID_POSITION;
            }
        }
        checkHeaderPositions(visibleHeaders);
        recyclerView.post(new Runnable() {
            @Override public void run() {
                checkElevation();
            }
        });
    }

    // This checks visible headers and their positions to determine if the sticky header needs
    // to be offset. In reality, only the header following the sticky header is checked. Some
    // optimization may be possible here (not storing all visible headers in map).
    void checkHeaderPositions(final Map<Integer, View> visibleHeaders) {
        if (currentHeader == null) return;
        // This can happen after configuration changes.
        if (currentHeader.getHeight() == 0) {
            currentHeader.setVisibility(View.VISIBLE);
            waitForLayoutAndRetry(visibleHeaders);
            return;
        }
        boolean reset = true;
        for (Map.Entry<Integer, View> entry : visibleHeaders.entrySet()) {
            if (entry.getKey() <= lastBoundPosition) {
                continue;
            }
            View nextHeader = entry.getValue();
            reset = offsetHeader(nextHeader) == -1;
            break;
        }
        if (reset) resetTranslation();
        currentHeader.setVisibility(View.VISIBLE);
    }

    void setElevateHeaders(int dpElevation) {
        if (dpElevation != NO_ELEVATION) {
            // Context may not be available at this point, so caching the dp value to be converted
            // into pixels after first header is attached.
            cachedElevation = dpElevation;
        } else {
            headerElevation = NO_ELEVATION;
            cachedElevation = NO_ELEVATION;
        }
    }

    void reset(int orientation) {
        this.orientation = orientation;
        lastBoundPosition = INVALID_POSITION;
        dirty = true;
        safeDetachHeader();
    }

    void clearHeader() {
        detachHeader(lastBoundPosition);
    }

    void clearVisibilityObserver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(visibilityObserver);
        } else {
            //noinspection deprecation
            recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(visibilityObserver);
        }
    }

    void setListener(@Nullable StickyHeaderListener listener) {
        this.listener = listener;
        if (currentHeader != null && listener != null) {
            listener.headerAttached(currentHeader, lastBoundPosition);
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
     * is a header (headerForPosition will not be null). If it is, we check its Y. If #getY is
     * greater than 0 then we know it is actually not the firstVisiblePosition, and return the
     * preceding header position (if available).
     */
    private int getHeaderPositionToShow(int firstVisiblePosition, @Nullable View headerForPosition) {
        int headerPositionToShow = INVALID_POSITION;
        if (headerIsOffset(headerForPosition)) {
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

    private boolean headerIsOffset(View headerForPosition) {
        if (headerForPosition != null) {
            return orientation == LinearLayoutManager.VERTICAL ?
                    headerForPosition.getY() > 0 : headerForPosition.getX() > 0;
        }
        return false;
    }

    @VisibleForTesting
    void attachHeader(RecyclerView.ViewHolder viewHolder, int headerPosition) {
        if (currentViewHolder == viewHolder) {
            callDetach(lastBoundPosition);
            //noinspection unchecked
            recyclerView.getAdapter().onBindViewHolder(currentViewHolder, headerPosition);
            currentViewHolder.itemView.requestLayout();
            checkTranslation();
            callAttach(headerPosition);
            dirty = false;
            return;
        }
        detachHeader(lastBoundPosition);
        this.currentViewHolder = viewHolder;
        //noinspection unchecked
        recyclerView.getAdapter().onBindViewHolder(currentViewHolder, headerPosition);
        this.currentHeader = currentViewHolder.itemView;
        callAttach(headerPosition);
        resolveElevationSettings(currentHeader.getContext());
        // Set to Invisible until we position it in #checkHeaderPositions.
        currentHeader.setVisibility(View.INVISIBLE);
        currentHeader.setId(R.id.header_view);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(visibilityObserver);
        getRecyclerParent().addView(currentHeader);
        if (checkMargins) {
            updateLayoutParams(currentHeader);
        }
        dirty = false;
    }

    private int currentDimension() {
        if (currentHeader == null) {
            return 0;
        }
        if (orientation == LinearLayoutManager.VERTICAL) {
            return currentHeader.getHeight();
        } else {
            return currentHeader.getWidth();
        }
    }

    private boolean headerHasTranslation() {
        if (currentHeader == null) {
            return false;
        }
        if (orientation == LinearLayoutManager.VERTICAL) {
            return currentHeader.getTranslationY() < 0;
        } else {
            return currentHeader.getTranslationX() < 0;
        }
    }

    private void updateTranslation(int diff) {
        if (currentHeader == null) {
            return;
        }
        if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader.setTranslationY(currentHeader.getTranslationY() + diff);
        } else {
            currentHeader.setTranslationX(currentHeader.getTranslationX() + diff);
        }
    }

    /**
     * When a view is re-bound using the same view holder, the height may have changed. If the header has translation
     * applied, this could cause a flickering if the view's height has increased.
     */
    private void checkTranslation() {
        final View view = currentHeader;
        if (view == null) return;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int previous = currentDimension();

            @Override public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    //noinspection deprecation
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if (currentHeader == null) return;

                int newDimen = currentDimension();
                if (headerHasTranslation() && previous != newDimen) {
                    updateTranslation(previous - newDimen);
                }
            }
        });
    }

    private void checkElevation() {
        if (headerElevation != NO_ELEVATION && currentHeader != null) {
            if (orientation == LinearLayoutManager.VERTICAL && currentHeader.getTranslationY() == 0
                    || orientation == LinearLayoutManager.HORIZONTAL && currentHeader.getTranslationX() == 0) {
                elevateHeader();
            } else {
                settleHeader();
            }
        }
    }

    private void elevateHeader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (currentHeader.getTag() != null) {
                // Already elevated, bail out
                return;
            }
            currentHeader.setTag(true);
            currentHeader.animate().z(headerElevation);
        }
    }

    private void settleHeader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (currentHeader.getTag() != null) {
                currentHeader.setTag(null);
                currentHeader.animate().z(0);
            }
        }
    }

    private void detachHeader(int position) {
        if (currentHeader != null) {
            getRecyclerParent().removeView(currentHeader);
            callDetach(position);
            clearVisibilityObserver();
            currentHeader = null;
            currentViewHolder = null;
        }
    }

    private void callAttach(int position) {
        if (listener != null) {
            listener.headerAttached(currentHeader, position);
        }
    }

    private void callDetach(int position) {
        if (listener != null) {
            listener.headerDetached(currentHeader, position);
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

    private boolean recyclerViewHasPadding() {
        return recyclerView.getPaddingLeft() > 0
                || recyclerView.getPaddingRight() > 0
                || recyclerView.getPaddingTop() > 0;
    }

    private ViewGroup getRecyclerParent() {
        return (ViewGroup) recyclerView.getParent();
    }

    private void waitForLayoutAndRetry(final Map<Integer, View> visibleHeaders) {
        final View view = currentHeader;
        if (view == null) return;
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        // If header was removed during layout
                        if (currentHeader == null) return;
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
        final int cachedPosition = lastBoundPosition;
        getRecyclerParent().post(new Runnable() {
            @Override public void run() {
                if (dirty) {
                    detachHeader(cachedPosition);
                }
            }
        });
    }

    /**
     * If a header view is restored in the hierarchy, we just want to remove it to keep state in
     * this class consistent.
     */
    private void maybeRemoveStaleHeader() {
        View existingHeader = getRecyclerParent().findViewById(R.id.header_view);
        if (existingHeader != null) {
            getRecyclerParent().removeView(existingHeader);
        }
        currentHeader = null;
    }

    @VisibleForTesting
    int getLastBoundPosition() {
        return lastBoundPosition;
    }

    private void resolveElevationSettings(Context context) {
        if (cachedElevation != NO_ELEVATION && headerElevation == NO_ELEVATION) {
            headerElevation = pxFromDp(context, cachedElevation);
        }
    }

    private float pxFromDp(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale;
    }
}
