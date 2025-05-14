package com.kalashianed.memeory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kalashianed.memeory.R;
import com.kalashianed.memeory.model.LeaderboardItem;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardItem> leaderboardItems;
    private Context context;

    public LeaderboardAdapter(Context context, List<LeaderboardItem> leaderboardItems) {
        this.context = context;
        this.leaderboardItems = leaderboardItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leader_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardItem item = leaderboardItems.get(position);
        
        holder.rankTextView.setText(String.valueOf(item.getRank()));
        holder.usernameTextView.setText(item.getUsername());
        holder.scoreTextView.setText(String.valueOf(item.getScore()));
        
        // Выделяем первые три места
        if (item.getRank() == 1) {
            holder.rankTextView.setBackgroundResource(R.drawable.rank_gold_background);
        } else if (item.getRank() == 2) {
            holder.rankTextView.setBackgroundResource(R.drawable.rank_silver_background);
        } else if (item.getRank() == 3) {
            holder.rankTextView.setBackgroundResource(R.drawable.rank_bronze_background);
        } else {
            holder.rankTextView.setBackgroundResource(R.drawable.rank_regular_background);
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardItems.size();
    }

    public void updateLeaderboard(List<LeaderboardItem> newItems) {
        this.leaderboardItems = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankTextView;
        TextView usernameTextView;
        TextView scoreTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
        }
    }
} 