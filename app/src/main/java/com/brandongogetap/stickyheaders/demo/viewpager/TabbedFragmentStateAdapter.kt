package com.brandongogetap.stickyheaders.demo.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class TabbedFragmentStateAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    var tabTitles: List<String> = ArrayList()

    fun submitList(newTitles: List<String>) {
        if (tabTitles != newTitles) {
            tabTitles = newTitles
            notifyDataSetChanged()
        }
    }

    override fun createFragment(position: Int): Fragment {
        return TabFragment()
    }

    override fun getItemCount(): Int {
        return tabTitles.size
    }

    override fun getItemId(position: Int): Long {
        return tabTitles[position].hashCode().toLong()
    }
}