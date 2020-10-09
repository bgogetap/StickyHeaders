package com.brandongogetap.stickyheaders.demo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.brandongogetap.stickyheaders.demo.util.HeaderUtilKt.verifyHeaderVisible;
import static com.brandongogetap.stickyheaders.demo.util.RecyclerViewUtilKt.scrollRecyclerViewToPosition;

final class StickyHeadersTestRobot {

    private final ActivityScenario<MainActivity> activityScenario;

    StickyHeadersTestRobot(ActivityScenario<MainActivity> activityScenario) {
        this.activityScenario = activityScenario;
    }

    StickyHeadersTestRobot scrollTo(int position) {
        scrollRecyclerViewToPosition(position);
        return this;
    }

    StickyHeadersTestRobot verifyHeaderExists(String headerId) {
        verifyHeaderVisible(headerId);
        return this;
    }

    StickyHeadersTestRobot verifyHeaderDoesNotExist() {
        onView(withId(R.id.header_view)).check(doesNotExist());
        return this;
    }

    StickyHeadersTestRobot updateData(final List<Item> data) throws Throwable {
        activityScenario.onActivity(activity -> activity.setItems(data));
        return this;
    }

    StickyHeadersTestRobot rotate() {
        rotateScreen();
        return this;
    }

    StickyHeadersTestRobot setVisibility(int visibility) throws Throwable {
        activityScenario.onActivity(activity -> activity.recyclerView.setVisibility(visibility));
        return this;
    }

    StickyHeadersTestRobot verifyHeaderVisibility(ViewMatchers.Visibility visibility) {
        onView(withId(R.id.header_view)).check(matches(withEffectiveVisibility(visibility)));
        return this;
    }

    StickyHeadersTestRobot updatePadding(int padding) throws Throwable {
        activityScenario.onActivity(activity -> activity.recyclerView.setPadding(padding, padding, padding, padding));
        return this;
    }

    private void rotateScreen() {
        Context context = InstrumentationRegistry.getTargetContext();
        int orientation = context.getResources().getConfiguration().orientation;

        activityScenario.onActivity(activity -> {
            activity.setRequestedOrientation((orientation == Configuration.ORIENTATION_PORTRAIT) ?
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

    }
}
