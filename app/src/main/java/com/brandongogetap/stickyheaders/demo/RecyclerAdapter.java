package com.brandongogetap.stickyheaders.demo;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brandongogetap.stickyheaders.StickyHeader;

import java.util.List;

import static android.view.LayoutInflater.from;

final class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private final List<Item> data;

    RecyclerAdapter(List<Item> data) {
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        final MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // This is unsafe to do in OnClickListeners attached to sticky headers. The adapter
                // position of the holder will be out of sync if any items have been added/removed.
                int position = holder.getAdapterPosition();

                data.remove(position);
                notifyDataSetChanged();
            }
        });
        return holder;
    }

    @Override public void onBindViewHolder(MyViewHolder holder, int position) {
        Item item = data.get(position);
        holder.titleTextView.setText(item.title);
        holder.messageTextView.setText(item.message);
        if (position != 0 && position % 16 == 0) {
            holder.itemView.setPadding(0, 100, 0, 100);
        } else {
            holder.itemView.setPadding(0, 0, 0, 0);
        }
        if (item instanceof StickyHeader) {
            holder.itemView.setBackgroundColor(Color.CYAN);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override public int getItemCount() {
        return data.size();
    }

    static final class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView messageTextView;

        MyViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.tv_title);
            messageTextView = (TextView) itemView.findViewById(R.id.tv_message);
        }
    }
}
