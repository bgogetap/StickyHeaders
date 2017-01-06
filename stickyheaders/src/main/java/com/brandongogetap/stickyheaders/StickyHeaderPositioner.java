package com.brandongogetap.stickyheaders;

import android.content.Context;
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

import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import java.util.List;
import java.util.Map;

final class StickyHeaderPositioner {

    static final int NO_ELEVATION = -1;
    static final int DEFAULT_ELEVATION = 5;

    private static final int INVALID_POSITION = -1;

    private final RecyclerView recyclerView;
    private final boolean checkMargins;
    private final boolean fallbackReset;

    private View currentHeader;
    private int lastBoundPosition = INVALID_POSITION;
    private List<Integer> headerPositions;
    private int orientation;
    private boolean dirty;
    private float headerElevation = NO_ELEVATION;
    private int cachedElevation = NO_ELEVATION;
    private boolean updateCurrentHeader;
    private RecyclerView.ViewHolder currentViewHolder;
    @Nullable private StickyHeaderListener listener;

    StickyHeaderPositioner(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        checkMargins = recyclerViewHasPadding();
        if (recyclerView.getAdapter() != null) {
            fallbackReset = false;
            recyclerView.getAdapter().registerAdapterDataObserver(
                    new RecyclerView.AdapterDataObserver() {
                        @Override public void onChanged() {
                            updateCurrentHeader = true;
                        }
                    });
        } else {
            fallbackReset = true;
        }
    }

    void setHeaderPositions(List<Integer> headerPositions) {
        this.headerPositions = headerPositions;
    }

    void updateHeaderState(int firstVisiblePosition, Map<Integer, View> visibleHeaders,
            ViewRetriever viewRetriever) {
        int headerPositionToShow = getHeaderPositionToShow(
                firstVisiblePosition, visibleHeaders.get(firstVisiblePosition));
        View headerToCopy = visibleHeaders.get(headerPositionToShow);
        if (headerPositionToShow != lastBoundPosition || updateCurrentHeader) {
            if (headerPositionToShow == INVALID_POSITION) {
                dirty = true;
                safeDetachHeader();
                lastBoundPosition = INVALID_POSITION;
            } else {
                // We don't want to attach yet if header view is not at edge
                if (checkMargins && headerAwayFromEdge(headerToCopy)) return;
                RecyclerView.ViewHolder viewHolder =
                        viewRetriever.getViewHolderForPosition(headerPositionToShow);
                attachHeader(viewHolder, headerPositionToShow);
                lastBoundPosition = headerPositionToShow;
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
            waitForLayoutAndRetry(visibleHeaders);
            return;
        }
        boolean reset = false;
        for (Map.Entry<Integer, View> entry : visibleHeaders.entrySet()) {
            if (entry.getKey() == lastBoundPosition) {
                reset = true;
                continue;
            }
            View nextHeader = entry.getValue();
            reset = offsetHeader(nextHeader) == -1;
            break;
        }
        if (reset) resetTranslation();
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
            callAttach(headerPosition);
            updateCurrentHeader = false;
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
        getRecyclerParent().addView(currentHeader);
        if (checkMargins) {
            updateLayoutParams(currentHeader);
        }
        dirty = false;
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

    void reset(int orientation, int firstVisiblePosition) {
        this.orientation = orientation;
        // Don't reset/detach if same header position is to be attached
        if (getHeaderPositionToShow(firstVisiblePosition, null) == lastBoundPosition) {
            return;
        }
        if (fallbackReset) {
            lastBoundPosition = INVALID_POSITION;
        }
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
        final int cachedPosition = lastBoundPosition;
        getRecyclerParent().post(new Runnable() {
            @Override public void run() {
                if (dirty) {
                    detachHeader(cachedPosition);
                }
            }
        });
    }

    @VisibleForTesting int getLastBoundPosition() {
        return lastBoundPosition;
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

    private void resolveElevationSettings(Context context) {
        if (cachedElevation != NO_ELEVATION && headerElevation == NO_ELEVATION) {
            headerElevation = pxFromDp(context, cachedElevation);
        }
    }

    private float pxFromDp(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale;
    }

    void setListener(@Nullable StickyHeaderListener listener) {
        this.listener = listener;
    }
}
