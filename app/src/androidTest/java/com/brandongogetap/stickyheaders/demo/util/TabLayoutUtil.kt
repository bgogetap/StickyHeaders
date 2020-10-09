package com.brandongogetap.stickyheaders.demo.util

import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.tabs.TabLayout
import org.hamcrest.CoreMatchers

fun onTab(withText: String): ViewInteraction {
    return Espresso.onView(
            CoreMatchers.allOf(
                    isDescendantOfA(isAssignableFrom(TabLayout::class.java)),
                    withChild(withText(withText))
            )
    )
}