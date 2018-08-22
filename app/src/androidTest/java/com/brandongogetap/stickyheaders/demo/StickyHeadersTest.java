package com.brandongogetap.stickyheaders.demo;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static com.brandongogetap.stickyheaders.demo.ItemGenerator.largeListWithHeadersAt;
import static com.brandongogetap.stickyheaders.demo.ItemGenerator.twoWithHeader;

@RunWith(AndroidJUnit4.class)
public class StickyHeadersTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
    private StickyHeadersTestRobot robot;

    @Before
    public void setUp() {
        robot = new StickyHeadersTestRobot(activityRule);
    }

    @Test
    public void headerIsStickyAfterScrollingPast() throws Throwable {
        robot.updateData(largeListWithHeadersAt(2, 5, 8, 10))
                .scrollTo(4)
                .verifyHeaderExists("2");
    }

    @Test
    public void headerRemainsStickyAfterRotation() throws Throwable {
        robot.updateData(largeListWithHeadersAt(2, 5, 8, 10))
                .scrollTo(3)
                .rotate()
                .verifyHeaderExists("2")
                // This extra rotation/check isn't really necessary, however leaving the device in landscape (after
                // first call to rotation) can cause some flakiness in the subsequent test, so this call gets the device
                // back to the default orientation.
                .rotate()
                .verifyHeaderExists("2");
    }

    @Test
    public void headerRemovedWhenDataCleared() throws Throwable {
        robot.updateData(largeListWithHeadersAt(2, 5, 8, 10))
                .scrollTo(5)
                .updateData(Collections.emptyList())
                .verifyHeaderDoesNotExist();
    }

    @Test
    public void headerRemovedWhenDataChanged() throws Throwable {
        robot.updateData(largeListWithHeadersAt(2, 5, 8, 10))
                .scrollTo(3)
                .updateData(largeListWithHeadersAt(5, 8, 10, 12))
                .verifyHeaderDoesNotExist()
                .updateData(largeListWithHeadersAt(2, 5, 8, 10))
                .scrollTo(3)
                .verifyHeaderExists("2")
                .updateData(largeListWithHeadersAt(30))
                .verifyHeaderDoesNotExist();
    }

    @Test
    public void headerRemainsWhenDataChanged() throws Throwable {
        robot.updateData(largeListWithHeadersAt(2, 5, 8, 10))
                .scrollTo(4)
                .verifyHeaderExists("2")
                .updateData(largeListWithHeadersAt(2, 5, 8, 10)) // Same List
                .verifyHeaderExists("2")
                .updateData(largeListWithHeadersAt(3, 5, 8, 10)) // Different first header position
                .verifyHeaderExists("3");
    }

    @Test
    public void headerRemovedWhenListNotScrollable() throws Throwable {
        robot.updateData(largeListWithHeadersAt(2, 5, 8, 10))
                .scrollTo(5)
                .updateData(twoWithHeader())
                .verifyHeaderDoesNotExist();
    }

    @Test
    public void headerVisibilityFollowsRecyclerView() throws Throwable {
        robot.updateData(largeListWithHeadersAt(2, 5, 8))
                .scrollTo(3)
                .setVisibility(View.GONE)
                .verifyHeaderVisibility(ViewMatchers.Visibility.GONE)
                .setVisibility(View.VISIBLE)
                .verifyHeaderVisibility(ViewMatchers.Visibility.VISIBLE);
    }

    @Test
    public void firstItemNotStickiedWhenAtTop() throws Throwable {
        robot.updateData(largeListWithHeadersAt(0, 3, 5))
                .updatePadding(0)
                .verifyHeaderDoesNotExist()
                .scrollTo(2)
                .verifyHeaderExists("0")
                .scrollTo(0)
                .verifyHeaderDoesNotExist()
                .rotate()
                .updatePadding(0)
                .verifyHeaderDoesNotExist()
                // Prevents flaky tests by getting orientation back to Portrait
                .rotate();
    }
}
