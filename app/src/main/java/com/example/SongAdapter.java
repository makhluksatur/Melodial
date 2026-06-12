package com.example;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import coil.ImageLoader;
import coil.request.ImageRequest;
import com.example.databinding.ItemSongBinding;
import java.util.List;
import java.util.Set;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Song song, boolean isFavorite);
    }

    public interface OnAddToPlaylistClickListener {
        void onAddToPlaylistClick(Song song);
    }

    public interface OnSongLongClickListener {
        boolean onSongLongClick(Song song);
    }

    private List<Song> songs;
    private Set<Long> favoriteTrackIds;
    private final OnSongClickListener onSongClickListener;
    private final OnFavoriteClickListener onFavoriteClickListener;
    private final OnAddToPlaylistClickListener onAddToPlaylistClickListener;
    private final OnSongLongClickListener onSongLongClickListener;

    public SongAdapter(
            List<Song> songs,
            Set<Long> favoriteTrackIds,
            OnSongClickListener onSongClickListener,
            OnFavoriteClickListener onFavoriteClickListener,
            OnAddToPlaylistClickListener onAddToPlaylistClickListener,
            OnSongLongClickListener onSongLongClickListener) {
        this.songs = songs;
        this.favoriteTrackIds = favoriteTrackIds;
        this.onSongClickListener = onSongClickListener;
        this.onFavoriteClickListener = onFavoriteClickListener;
        this.onAddToPlaylistClickListener = onAddToPlaylistClickListener;
        this.onSongLongClickListener = onSongLongClickListener;
    }

    public SongAdapter(
            List<Song> songs,
            Set<Long> favoriteTrackIds,
            OnSongClickListener onSongClickListener,
            OnFavoriteClickListener onFavoriteClickListener,
            OnAddToPlaylistClickListener onAddToPlaylistClickListener) {
        this(songs, favoriteTrackIds, onSongClickListener, onFavoriteClickListener, onAddToPlaylistClickListener, null);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSongBinding binding = ItemSongBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SongViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(songs.get(position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateData(List<Song> newSongs, Set<Long> newFavoriteIds) {
        this.songs = newSongs;
        this.favoriteTrackIds = newFavoriteIds;
        notifyDataSetChanged();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        final ItemSongBinding binding;

        SongViewHolder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Song song) {
            binding.tvSongTitle.setText(song.getTrackName() != null ? song.getTrackName() : "Tanpa Judul");
            binding.tvArtistName.setText(song.getArtistName() != null ? song.getArtistName() : "Artis Tidak Diketahui");
            binding.tvAlbumName.setText(song.getCollectionName() != null ? song.getCollectionName() : "Album Tidak Diketahui");

            String artworkUrl = song.getArtworkUrl100();
            if (artworkUrl != null && !artworkUrl.isEmpty()) {
                ImageLoader imageLoader = coil.Coil.imageLoader(binding.getRoot().getContext());
                ImageRequest request = new ImageRequest.Builder(binding.getRoot().getContext())
                        .data(artworkUrl)
                        .crossfade(true)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .target(binding.ivAlbumCover)
                        .build();
                imageLoader.enqueue(request);
            } else {
                binding.ivAlbumCover.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            final boolean isFav = favoriteTrackIds.contains(song.getTrackId());
            binding.btnFavorite.setImageResource(
                    isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
            );

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSongClickListener != null) {
                        onSongClickListener.onSongClick(song);
                    }
                }
            });

            binding.btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onFavoriteClickListener != null) {
                        onFavoriteClickListener.onFavoriteClick(song, isFav);
                    }
                }
            });

            binding.btnAddToPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onAddToPlaylistClickListener != null) {
                        onAddToPlaylistClickListener.onAddToPlaylistClick(song);
                    }
                }
            });

            if (onSongLongClickListener != null) {
                binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return onSongLongClickListener.onSongLongClick(song);
                    }
                });
            } else {
                binding.getRoot().setOnLongClickListener(null);
            }
        }
    }
}
