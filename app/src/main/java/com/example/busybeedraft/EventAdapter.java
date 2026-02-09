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

    public EventAdapter(List<CalendarEvent> eventList) {
        this.eventList = eventList;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent event = eventList.get(position);
        holder.time.setText(event.time);
        holder.title.setText(event.title);

        // Logic: Hide course tag if it's just a personal event
        if (event.course == null || event.course.equals("Personal") || event.course.isEmpty()) {
            holder.course.setVisibility(View.GONE);
        } else {
            holder.course.setVisibility(View.VISIBLE);
            holder.course.setText(event.course);
        }
    }

    @Override public int getItemCount() { return eventList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView time, title, course;
        ViewHolder(View v) {
            super(v);
            time = v.findViewById(R.id.tvEventTimeBubble);
            title = v.findViewById(R.id.tvEventTitleDisplay);
            course = v.findViewById(R.id.tvCourseTag);
        }
    }
}