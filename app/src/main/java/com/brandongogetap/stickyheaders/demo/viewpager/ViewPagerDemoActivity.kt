package com.brandongogetap.stickyheaders.demo.viewpager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.brandongogetap.stickyheaders.demo.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_pager_activity)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val tabAdapter = TabbedFragmentStateAdapter(this)

        tabLayout.tabMode = TabLayout.MODE_FIXED
        viewPager.offscreenPageLimit = 1

        val titles: MutableList<String> = mutableListOf(
                "1",
                "2",
                "3",
                "4",
                "5",
        )
        tabAdapter.submitList(titles)
        viewPager.adapter = tabAdapter

        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabAdapter.tabTitles.get(position)
        }
        tabLayoutMediator.attach()
    }
}