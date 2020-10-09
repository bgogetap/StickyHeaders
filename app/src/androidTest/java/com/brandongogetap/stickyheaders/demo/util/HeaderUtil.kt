package com.brandongogetap.stickyheaders.demo.util

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.brandongogetap.stickyheaders.demo.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers

fun verifyHeaderVisible(headerText: String) {
    onView(withId(R.id.header_view)).check(matches(isCompletelyDisplayed()))
    onView(allOf(ViewMatchers.isDescendantOfA(withId(R.id.header_view)), withId(R.id.tv_title)))
            .check(matches(ViewMatchers.withText(Matchers.endsWith(headerText))))
}