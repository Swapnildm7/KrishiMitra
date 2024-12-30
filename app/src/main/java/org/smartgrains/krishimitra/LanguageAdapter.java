package org.smartgrains.krishimitra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    // Languages in their native scripts
    private final String[] languages = {"English", "हिन्दी", "ಕನ್ನಡ", "मराठी"};
    private final int[] flags = {R.drawable.ic_english, R.drawable.ic_hindi, R.drawable.ic_kannada, R.drawable.ic_marathi};
    private final String[] languageCodes = {"en", "hi", "kn", "mr"};

    private final OnLanguageSelectedListener listener;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String languageCode, int flagResId, String languageName);
    }

    public LanguageAdapter(OnLanguageSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        holder.tvLanguageName.setText(languages[position]); // Use native script names
        holder.ivLanguageFlag.setImageResource(flags[position]);

        holder.itemView.setOnClickListener(v ->
                listener.onLanguageSelected(languageCodes[position], flags[position], languages[position]));
    }

    @Override
    public int getItemCount() {
        return languages.length;
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLanguageFlag;
        TextView tvLanguageName;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLanguageFlag = itemView.findViewById(R.id.ivLanguageFlag);
            tvLanguageName = itemView.findViewById(R.id.tvLanguageName);
        }
    }
}
