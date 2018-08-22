package com.brandongogetap.stickyheaders.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.brandongogetap.stickyheaders.StickyLayoutManager;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        adapter = new RecyclerAdapter();
        adapter.setData(ItemGenerator.demoList());
        StickyLayoutManager layoutManager = new TopSnappedStickyLayoutManager(this, adapter);
        layoutManager.elevateHeaders(true); // Default elevation of 5dp
        // You can also specify a specific dp for elevation
//        layoutManager.elevateHeaders(10);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        layoutManager.setStickyHeaderListener(new StickyHeaderListener() {
            @Override
            public void headerAttached(View headerView, int adapterPosition) {
                Log.d("Listener", "Attached with position: " + adapterPosition);
            }

            @Override
            public void headerDetached(View headerView, int adapterPosition) {
                Log.d("Listener", "Detached with position: " + adapterPosition);
            }
        });
        findViewById(R.id.visibility_button).setOnClickListener(v ->
                recyclerView.setVisibility(recyclerView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
    }

    void setItems(List<Item> items) {
        if (adapter != null) {
            adapter.setData(items);
        }
    }
}
