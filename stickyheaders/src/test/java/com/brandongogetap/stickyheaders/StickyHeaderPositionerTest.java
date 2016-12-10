package com.brandongogetap.stickyheaders;

import org.junit.Test;

import java.util.Arrays;

import static com.google.common.primitives.Ints.asList;

public final class StickyHeaderPositionerTest {

    @Test
    public void correctHeaderToShowSelected() {
        StickyHeaderPositionerRobot.create()
                .withHeaderPositions(Arrays.asList(4, 6, 8))
                .setupPosition(5)
                .checkLastBoundHeaderPositionEquals(4);
    }

    @Test
    public void offsetIsCalculatedCorrectly() {
        StickyHeaderPositionerRobot.create()
                .withHeaderPositions(Arrays.asList(4, 6, 8))
                .setupPosition(5)
                .checkOffsetCalculation(100, -100)
                .checkOffsetCalculation(150, -50)
                .checkOffsetCalculation(200, 0);
    }

    @Test
    public void listenerCallbacksInvoked() {
        StickyHeaderPositionerRobot.create()
                .withHeaderPositions(asList(1, 3, 5))
                .setupPosition(2)
                .attachWithSameViewHolder(1, 3)
                .setupPosition(4)
                .attachWithDifferentViewHolder(3, 5);
    }
}
