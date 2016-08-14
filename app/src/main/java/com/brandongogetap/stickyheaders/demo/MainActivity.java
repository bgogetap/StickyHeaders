package com.brandongogetap.stickyheaders.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.brandongogetap.stickyheaders.StickyLayoutManager;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StickyHeaderHandler {

    private List<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        items = compileItems();
        RecyclerAdapter adapter = new RecyclerAdapter(items);
        StickyLayoutManager layoutManager = new StickyLayoutManager(this, this);
        layoutManager.elevateHeaders(true); // Default elevation of 5dp
        // You can also specify a specific dp for elevation
//        layoutManager.elevateHeaders(10);
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
}
