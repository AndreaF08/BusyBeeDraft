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
    private List<CourseDetailActivity.Flashcard> flashcardList;

    public FlashcardAdapter(List<CourseDetailActivity.Flashcard> list) {
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
        CourseDetailActivity.Flashcard card = flashcardList.get(holder.getAdapterPosition());

        // Stop old listeners from firing while we set the text
        holder.etTerm.removeTextChangedListener(holder.termWatcher);
        holder.etDefinition.removeTextChangedListener(holder.defWatcher);

        holder.etTerm.setText(card.getTerm());
        holder.etDefinition.setText(card.getDefinition());

        // Setup new listeners for this specific card data
        holder.termWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) { card.setTerm(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        holder.defWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) { card.setDefinition(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        holder.etTerm.addTextChangedListener(holder.termWatcher);
        holder.etDefinition.addTextChangedListener(holder.defWatcher);

        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                flashcardList.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, flashcardList.size());
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
}