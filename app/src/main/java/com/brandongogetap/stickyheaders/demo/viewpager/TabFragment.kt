package com.brandongogetap.stickyheaders.demo.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.brandongogetap.stickyheaders.demo.ItemGenerator
import com.brandongogetap.stickyheaders.demo.R
import com.brandongogetap.stickyheaders.demo.RecyclerAdapter
import com.brandongogetap.stickyheaders.demo.TopSnappedStickyLayoutManager

class TabFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            adapter = RecyclerAdapter().apply {
                setData(ItemGenerator.demoList())
            }
            layoutManager = TopSnappedStickyLayoutManager(context, adapter as RecyclerAdapter)
        }
    }
}