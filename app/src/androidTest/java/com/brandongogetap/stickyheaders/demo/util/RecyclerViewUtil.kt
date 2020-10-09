package com.brandongogetap.stickyheaders.demo.util

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.brandongogetap.stickyheaders.demo.R
import org.hamcrest.CoreMatchers.allOf

fun scrollRecyclerViewToPosition(position: Int) {
    onView(allOf(withId(R.id.recycler_view), isCompletelyDisplayed()))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
}