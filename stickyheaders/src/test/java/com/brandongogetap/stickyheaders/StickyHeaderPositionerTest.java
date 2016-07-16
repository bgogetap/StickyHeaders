package com.brandongogetap.stickyheaders;

import org.junit.Test;

import java.util.Arrays;

public final class StickyHeaderPositionerTest {

    @Test
    public void correctHeaderToShowSelected() {
        StickyHeaderPositionerRobot.create()
                .withHeaderPositions(Arrays.asList(4, 6, 8))
                .setupPosition(5)
                .checkLastBoundHeaderPositionEquals(4);
    }

    @Test
    public void resetClearsStateUnlessSameHeaderToBeReBound() {
        StickyHeaderPositionerRobot.create()
                .withHeaderPositions(Arrays.asList(4, 6, 8))
                .setupPosition(5)
                .reset(3)
                .checkLastBoundHeaderPositionEquals(-1) // INVALID_POSITION
                .setupPosition(5)
                .reset(5)
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
}
