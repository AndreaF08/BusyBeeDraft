package com.example.busybeedraft;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<CalendarEvent> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CalendarEvent event, int position);
        void onItemLongClick(CalendarEvent event, int position);
    }

    public EventAdapter(List<CalendarEvent> eventList, OnItemClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent event = eventList.get(position);
        holder.tvTitle.setText(event.title);
        holder.tvTime.setText(event.time);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(event, position));
    }

    @Override
    public int getItemCount() { return eventList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs match your XML exactly
            tvTitle = itemView.findViewById(R.id.text_event_title);
            tvTime = itemView.findViewById(R.id.text_event_time);
        }
    }
}