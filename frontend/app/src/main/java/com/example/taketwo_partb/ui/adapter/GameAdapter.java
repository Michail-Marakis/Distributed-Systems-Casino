package com.example.taketwo_partb.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taketwo_partb.R;
import com.example.taketwo_partb.domain.Game;

import java.util.List;

/**
 * The type Game adapter.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    /**
     * The interface On game click listener.
     */
    public interface OnGameClickListener {
        /**
         * On game click.
         *
         * @param game the game
         */
        void onGameClick(Game game);
    }

    private final List<Game> games;
    private final OnGameClickListener listener;

    /**
     * Instantiates a new Game adapter.
     *
     * @param games    the games
     * @param listener the listener
     */
    public GameAdapter(List<Game> games, OnGameClickListener listener) {
        this.games = games;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);

        holder.tvGameName.setText(game.getGameName());
        holder.tvProvider.setText("Provider: " + game.getProviderName());
        holder.tvRisk.setText("Risk: " + game.getRiskLevel());
        holder.tvBet.setText("Bet: " + game.getMinBet() + " - " + game.getMaxBet() + " (" + game.getBetLevel() + ")");
        holder.tvStars.setText("Stars: " + game.getStars() + " | Jackpot: " + game.getJackpot());

        holder.itemView.setOnClickListener(v -> listener.onGameClick(game));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    /**
     * The type Game view holder.
     */
    static class GameViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Tv game name.
         */
        TextView tvGameName, /**
         * The Tv provider.
         */
        tvProvider, /**
         * The Tv risk.
         */
        tvRisk, /**
         * The Tv bet.
         */
        tvBet, /**
         * The Tv stars.
         */
        tvStars;

        /**
         * Instantiates a new Game view holder.
         *
         * @param itemView the item view
         */
        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGameName = itemView.findViewById(R.id.tvGameName);
            tvProvider = itemView.findViewById(R.id.tvProvider);
            tvRisk = itemView.findViewById(R.id.tvRisk);
            tvBet = itemView.findViewById(R.id.tvBet);
            tvStars = itemView.findViewById(R.id.tvStars);
        }
    }
}