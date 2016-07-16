package com.brandongogetap.stickyheaders.demo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class DemoStickyHeadersTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void headerIsStickyAfterScrollingPast() {
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(15));
        onView(withId(R.id.header_view)).check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void headerRemainsStickyAfterRotation() {
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(15));
        rotateScreen();
        onView(withId(R.id.header_view)).check(matches(isCompletelyDisplayed()));
    }

    private void rotateScreen() {
        Context context = InstrumentationRegistry.getTargetContext();
        int orientation = context.getResources().getConfiguration().orientation;

        Activity activity = activityRule.getActivity();
        activity.setRequestedOrientation((orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
