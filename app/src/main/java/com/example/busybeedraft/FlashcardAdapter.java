package com.example.busybeedraft;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {
    // Changed from CourseDetailActivity.Flashcard to a standard Flashcard model
    private List<Flashcard> flashcardList;

    public FlashcardAdapter(List<Flashcard> list) {
        this.flashcardList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Use getBindingAdapterPosition() for modern RecyclerView implementations
        int currentPos = holder.getBindingAdapterPosition();
        if (currentPos == RecyclerView.NO_POSITION) return;

        Flashcard card = flashcardList.get(currentPos);

        // Prevent recursive triggers by removing listeners before setting text
        holder.etTerm.removeTextChangedListener(holder.termWatcher);
        holder.etDefinition.removeTextChangedListener(holder.defWatcher);

        holder.etTerm.setText(card.getTerm());
        holder.etDefinition.setText(card.getDefinition());

        // Update the watchers to target the specific card instance for this position
        holder.termWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) { card.setTerm(s.toString().trim()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        holder.defWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) { card.setDefinition(s.toString().trim()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        holder.etTerm.addTextChangedListener(holder.termWatcher);
        holder.etDefinition.addTextChangedListener(holder.defWatcher);

        holder.btnDelete.setOnClickListener(v -> {
            int deletePos = holder.getBindingAdapterPosition();
            if (deletePos != RecyclerView.NO_POSITION) {
                flashcardList.remove(deletePos);
                notifyItemRemoved(deletePos);
                notifyItemRangeChanged(deletePos, flashcardList.size());
            }
        });
    }

    @Override
    public int getItemCount() { return flashcardList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText etTerm, etDefinition;
        ImageView btnDelete;
        TextWatcher termWatcher, defWatcher;

        public ViewHolder(View itemView) {
            super(itemView);
            etTerm = itemView.findViewById(R.id.etTerm);
            etDefinition = itemView.findViewById(R.id.etDefinition);
            btnDelete = itemView.findViewById(R.id.btnDeleteCard);
        }
    }

    /**
     * Standalone Flashcard model to replace the one removed from CourseDetailActivity.
     * This ensures the adapter can still manage data independently.
     */
    public static class Flashcard {
        private String term, definition;
        public Flashcard(String t, String d) { this.term = t; this.definition = d; }
        public String getTerm() { return term != null ? term : ""; }
        public void setTerm(String t) { this.term = t; }
        public String getDefinition() { return definition != null ? definition : ""; }
        public void setDefinition(String d) { this.definition = d; }
    }
}