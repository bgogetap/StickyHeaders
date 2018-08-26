package com.brandongogetap.stickyheaders.demo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

final class StickyHeadersTestRobot {

    private final ActivityTestRule<MainActivity> activityRule;

    StickyHeadersTestRobot(ActivityTestRule<MainActivity> activityRule) {
        this.activityRule = activityRule;
    }

    StickyHeadersTestRobot scrollTo(int position) {
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(position));
        return this;
    }

    StickyHeadersTestRobot verifyHeaderExists(String headerId) {
        onView(withId(R.id.header_view)).check(matches(isCompletelyDisplayed()));
        onView(allOf(isDescendantOfA(ViewMatchers.withId(R.id.header_view)), withId(R.id.tv_title)))
                .check(matches(withText(endsWith(" " + headerId))));
        return this;
    }

    StickyHeadersTestRobot verifyHeaderDoesNotExist() {
        onView(withId(R.id.header_view)).check(doesNotExist());
        return this;
    }

    StickyHeadersTestRobot updateData(final List<Item> data) throws Throwable {
        activityRule.runOnUiThread(() -> mainActivity().setItems(data));
        return this;
    }

    StickyHeadersTestRobot rotate() {
        rotateScreen();
        return this;
    }

    StickyHeadersTestRobot setVisibility(int visibility) throws Throwable {
        activityRule.runOnUiThread(() -> mainActivity().recyclerView.setVisibility(visibility));
        return this;
    }

    StickyHeadersTestRobot verifyHeaderVisibility(ViewMatchers.Visibility visibility) {
        onView(withId(R.id.header_view)).check(matches(withEffectiveVisibility(visibility)));
        return this;
    }

    StickyHeadersTestRobot updatePadding(int padding) throws Throwable {
        activityRule.runOnUiThread(() -> mainActivity().recyclerView.setPadding(padding, padding, padding, padding));
        return this;
    }

    private MainActivity mainActivity() {
        return activityRule.getActivity();
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
