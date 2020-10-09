package com.brandongogetap.stickyheaders.demo.viewpager

import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.integration.testapp.test.util.ViewPagerIdleWatcher
import com.brandongogetap.stickyheaders.demo.R
import com.brandongogetap.stickyheaders.demo.util.onTab
import com.brandongogetap.stickyheaders.demo.util.scrollRecyclerViewToPosition
import com.brandongogetap.stickyheaders.demo.util.verifyHeaderVisible
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StickyHeadersViewPagerTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(ViewPagerDemoActivity::class.java)

    private lateinit var viewPagerIdleWatcher: ViewPagerIdleWatcher

    @Before
    fun setUp() {
        activityRule.scenario.onActivity {
            viewPagerIdleWatcher = ViewPagerIdleWatcher(it.findViewById(R.id.view_pager))
        }
    }

    @After
    fun tearDown() {
        viewPagerIdleWatcher.unregister()
    }

    @Test
    fun headerIsReStickiedWhenReturningToOffscreenTab() {
        // Trigger sticky header on first tab
        val expectedFirstHeader = "2"
        scrollRecyclerViewToPosition(position = 3)
        verifyHeaderVisible(headerText = expectedFirstHeader)

        // Move to third tab and trigger another sticky header
        onTab(withText = "4").perform(click())
        viewPagerIdleWatcher.waitForIdle()
        scrollRecyclerViewToPosition(position = 41)
        verifyHeaderVisible(headerText = "40")

        // Move back to first tab
        onTab(withText = "1").perform(click())
        viewPagerIdleWatcher.waitForIdle()

        // Verify original stickied header is still sticky
        verifyHeaderVisible(headerText = expectedFirstHeader)

        // Scroll and verify another sticky header becomes sticky
        // Side effect of this check also verifies we don't mistakenly have multiple header views
        // added
        scrollRecyclerViewToPosition(position = 9)
        verifyHeaderVisible(headerText = "8")
    }
}

