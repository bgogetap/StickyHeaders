package com.brandongogetap.stickyheaders.demo;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brandongogetap.stickyheaders.exposed.StickyHeader;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.view.LayoutInflater.from;

final class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.BaseViewHolder>
        implements StickyHeaderHandler {

    private final List<Item> data;

    RecyclerAdapter(List<Item> data) {
        this.data = data;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        final BaseViewHolder viewHolder;
        if (viewType == 0) {
            viewHolder = new MyViewHolder(view);
        } else {
            viewHolder = new MyOtherViewHolder(view);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // This is unsafe to do in OnClickListeners attached to sticky headers. The adapter
                // position of the holder will be out of sync if any items have been added/removed.
                // If a click listener needs to be set on a sticky header, it is recommended to identify the header
                // based on its backing model, rather than position in the data set.
                int position = viewHolder.getAdapterPosition();
                if (position != NO_POSITION) {
                    data.remove(position);
                    notifyDataSetChanged();
                }
            }
        });
        return viewHolder;
    }

    @Override public void onBindViewHolder(BaseViewHolder holder, int position) {
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
        return data != null ? data.size() : 0;
    }

    @Override public int getItemViewType(int position) {
        if (position != 0 && position % 16 == 0) {
            return 1;
        }
        return 0;
    }

    @Override public List<?> getAdapterData() {
        return data;
    }

    private static final class MyViewHolder extends BaseViewHolder {

        MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static final class MyOtherViewHolder extends BaseViewHolder {

        MyOtherViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView messageTextView;

        BaseViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.tv_title);
            messageTextView = (TextView) itemView.findViewById(R.id.tv_message);
        }
    }
}
