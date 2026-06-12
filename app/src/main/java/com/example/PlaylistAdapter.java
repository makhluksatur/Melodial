package com.example;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.databinding.ItemPlaylistBinding;
import java.util.List;
import java.util.Map;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Playlist playlist);
    }

    private List<Playlist> playlists;
    private Map<Long, Integer> trackCounts;
    private final OnPlaylistClickListener onPlaylistClickListener;
    private final OnDeleteClickListener onDeleteClickListener;

    public PlaylistAdapter(
            List<Playlist> playlists,
            Map<Long, Integer> trackCounts,
            OnPlaylistClickListener onPlaylistClickListener,
            OnDeleteClickListener onDeleteClickListener) {
        this.playlists = playlists;
        this.trackCounts = trackCounts;
        this.onPlaylistClickListener = onPlaylistClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlaylistBinding binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaylistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.bind(playlists.get(position));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void updateData(List<Playlist> newPlaylists, Map<Long, Integer> newTrackCounts) {
        this.playlists = newPlaylists;
        this.trackCounts = newTrackCounts;
        notifyDataSetChanged();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        final ItemPlaylistBinding binding;

        PlaylistViewHolder(ItemPlaylistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Playlist playlist) {
            binding.tvPlaylistTitle.setText(playlist.getName());

            Integer count = trackCounts.get(playlist.getId());
            if (count == null) {
                count = 0;
            }
            binding.tvPlaylistTrackCount.setText(count + " lagu");

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPlaylistClickListener != null) {
                        onPlaylistClickListener.onPlaylistClick(playlist);
                    }
                }
            });

            binding.btnDeletePlaylistIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDeleteClickListener != null) {
                        onDeleteClickListener.onDeleteClick(playlist);
                    }
                }
            });
        }
    }
}
