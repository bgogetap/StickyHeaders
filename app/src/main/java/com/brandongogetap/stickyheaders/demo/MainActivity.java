package com.brandongogetap.stickyheaders.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.brandongogetap.stickyheaders.StickyHeaderHandler;
import com.brandongogetap.stickyheaders.StickyLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StickyHeaderHandler {

    private List<Item> items;
    private FrameLayout recyclerParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerParent = (FrameLayout) findViewById(R.id.fl_parent);

        items = compileItems();
        RecyclerAdapter adapter = new RecyclerAdapter(items);
        StickyLayoutManager layoutManager = new StickyLayoutManager(this, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private List<Item> compileItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            if (i % 4 == 0 && i > 0) {
                items.add(new HeaderItem("Header at " + i, ""));
            } else {
                items.add(new Item("Item at " + i, "Item description at " + i));
            }
        }
        return items;
    }

    @Override public List<?> getAdapterData() {
        return items;
    }

    @Override public ViewGroup getRecyclerParent() {
        return recyclerParent;
    }
}
