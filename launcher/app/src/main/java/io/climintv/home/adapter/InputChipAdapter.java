package io.climintv.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.climintv.home.R;
import io.climintv.home.model.TvInputEntry;
import io.climintv.home.util.FocusAnimator;

public class InputChipAdapter extends RecyclerView.Adapter<InputChipAdapter.ViewHolder> {

    public interface OnInputClickListener {
        void onInputClick(TvInputEntry input);
    }

    private final List<TvInputEntry> items = new ArrayList<>();
    private final OnInputClickListener listener;

    public InputChipAdapter(OnInputClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TvInputEntry> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_m3_input_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TvInputEntry input = items.get(position);
        holder.bind(input, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView labelView;
        private final ImageView iconView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelView = itemView.findViewById(R.id.input_label);
            iconView = itemView.findViewById(R.id.input_icon);
            FocusAnimator.attach(itemView, 1.05f);
        }

        public void bind(TvInputEntry input, OnInputClickListener listener) {
            labelView.setText(input.getLabel());

            String idLower = input.getId().toLowerCase();
            if (idLower.contains("tuner") || idLower.contains("dtv")) {
                iconView.setImageResource(R.drawable.ic_livetv);
            } else if (idLower.contains("av") || idLower.contains("composite")) {
                iconView.setImageResource(R.drawable.ic_av);
            } else {
                iconView.setImageResource(R.drawable.ic_input);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onInputClick(input);
            });
        }
    }
}
