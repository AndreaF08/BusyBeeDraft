package com.example.busybeedraft;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {
    private List<Flashcard> flashcardList;

    public FlashcardAdapter(List<Flashcard> list) {
        this.flashcardList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the display item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flashcard card = flashcardList.get(position);

        // Sets text to TextViews instead of EditTexts to avoid casting errors
        holder.tvTerm.setText(card.getTerm());
        holder.tvDefinition.setText(card.getDefinition());

        // Standard click listener to edit the card
        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() instanceof CourseDetailActivity) {
                ((CourseDetailActivity) v.getContext()).showFlashcardDialog(card, position);
            }
        });
    }

    @Override
    public int getItemCount() { return flashcardList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Changed to TextView to match item_flashcard.xml layout tags
        TextView tvTerm, tvDefinition;

        public ViewHolder(View itemView) {
            super(itemView);
            // Uses IDs that exist in your item_flashcard.xml
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
        }
    }

    // Flashcard model with necessary getters and setters
    public static class Flashcard {
        private String term, definition;
        public Flashcard(String t, String d) { this.term = t; this.definition = d; }
        public String getTerm() { return term != null ? term : ""; }
        public void setTerm(String t) { this.term = t; }
        public String getDefinition() { return definition != null ? definition : ""; }
        public void setDefinition(String d) { this.definition = d; }
    }
}