package com.brandongogetap.stickyheaders;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class StickyHeaderPositionerRobot {

    private final StickyHeaderPositioner positioner;
    private final StickyHeaderListener listener;
    private final RecyclerView.ViewHolder viewHolder;
    private final View currentHeader;

    private StickyHeaderPositionerRobot() {
        RecyclerView recyclerView = mock(RecyclerView.class);
        listener = mock(StickyHeaderListener.class);
        ViewGroup parent = mock(ViewGroup.class);
        when(recyclerView.getParent()).thenReturn(parent);
        when(recyclerView.getAdapter()).thenReturn(mock(RecyclerView.Adapter.class));
        when(recyclerView.getViewTreeObserver()).thenReturn(mock(ViewTreeObserver.class));
        when(parent.getViewTreeObserver()).thenReturn(mock(ViewTreeObserver.class));
        positioner = new StickyHeaderPositioner(recyclerView);
        positioner.setHeaderPositions(new ArrayList<Integer>());
        positioner.reset(LinearLayoutManager.VERTICAL);

        currentHeader = mock(View.class);
        viewHolder = new RecyclerView.ViewHolder(currentHeader) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
        when(currentHeader.getViewTreeObserver()).thenReturn(mock(ViewTreeObserver.class));
    }

    static StickyHeaderPositionerRobot create() {
        return new StickyHeaderPositionerRobot();
    }

    StickyHeaderPositionerRobot withHeaderPositions(List<Integer> headerPositions) {
        positioner.setHeaderPositions(headerPositions);
        return this;
    }

    StickyHeaderPositionerRobot setupPosition(int firstVisiblePosition) {
        ViewRetriever viewRetriever = mock(ViewRetriever.class);
        when(currentHeader.getHeight()).thenReturn(200);
        when(viewRetriever.getViewHolderForPosition(anyInt())).thenReturn(viewHolder);
        positioner.updateHeaderState(firstVisiblePosition, Collections.<Integer, View>emptyMap(), viewRetriever, false);
        return this;
    }

    StickyHeaderPositionerRobot checkLastBoundHeaderPositionEquals(int position) {
        assertThat(positioner.getLastBoundPosition(), is(position));
        return this;
    }

    StickyHeaderPositionerRobot attachWithSameViewHolder(int lastPosition, int headerPosition, boolean checkListener) {
        positioner.attachHeader(viewHolder, headerPosition);
        if (checkListener) {
            InOrder inOrder = inOrder(listener);
            inOrder.verify(listener, times(1)).headerDetached(currentHeader, lastPosition);
            inOrder.verify(listener, times(1)).headerAttached(currentHeader, headerPosition);
        }
        return this;
    }

    StickyHeaderPositionerRobot attachWithDifferentViewHolder(int lastPosition, int headerPosition, boolean checkListener) {
        View otherView = mock(View.class);
        RecyclerView.ViewHolder otherViewHolder = new RecyclerView.ViewHolder(otherView) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
        positioner.attachHeader(otherViewHolder, headerPosition);
        if (checkListener) {
            InOrder inOrder = inOrder(listener);
            inOrder.verify(listener, times(1)).headerDetached(currentHeader, lastPosition);
            inOrder.verify(listener, times(1)).headerAttached(otherView, headerPosition);
        }
        return this;
    }

    StickyHeaderPositionerRobot checkOffsetCalculation(float nextHeaderTop, float expectedOffset) {
        Map<Integer, View> visibleHeaderMap = new LinkedHashMap<>();
        View nextHeader = mock(View.class);
        visibleHeaderMap.put(6, nextHeader);
        when(nextHeader.getY()).thenReturn(nextHeaderTop);
        positioner.checkHeaderPositions(visibleHeaderMap);
        verify(currentHeader, atLeastOnce()).setTranslationY(expectedOffset);
        return this;
    }

    StickyHeaderPositionerRobot setListener() {
        positioner.setListener(listener);
        return this;
    }

    StickyHeaderPositionerRobot validateListenerCalledAttach(int position) {
        verify(listener).headerAttached(currentHeader, position);
        return this;
    }
}
