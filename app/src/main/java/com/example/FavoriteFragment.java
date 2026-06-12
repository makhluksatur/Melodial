package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.databinding.FragmentFavoriteBinding;
import com.example.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private SongAdapter songAdapter;
    private final List<Song> favoriteSongs = new ArrayList<>();
    private final Set<Long> favoriteIds = new HashSet<>();
    private DatabaseHelper databaseHelper;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = new DatabaseHelper(requireContext());

        setupRecyclerView();
        loadOfflineFavorites();
    }

    private void setupRecyclerView() {
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));

        songAdapter = new SongAdapter(
                favoriteSongs,
                favoriteIds,
                song -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).playSong(song);
                    }
                },
                (song, isFavorite) -> toggleFavoriteSong(song, isFavorite),
                song -> {
                    if (checkLogin()) {
                        PlaylistDialogHelper.showAddToPlaylistDialog(requireContext(), song);
                    }
                }
        );
        binding.rvFavorites.setAdapter(songAdapter);
    }

    private boolean checkLogin() {
        if (!UserPreferences.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return false;
        }
        return true;
    }

    public void loadOfflineFavorites() {
        if (getContext() == null) return;
        String email = UserPreferences.getEmail(requireContext());
        
        databaseExecutor.execute(() -> {
            List<Song> songs = databaseHelper.getAllFavorites(email);
            Set<Long> ids = new HashSet<>();
            for (Song s : songs) {
                ids.add(s.getTrackId());
            }

            mainThreadHandler.post(() -> {
                if (binding == null) return;
                favoriteSongs.clear();
                favoriteSongs.addAll(songs);

                favoriteIds.clear();
                favoriteIds.addAll(ids);

                if (favoriteSongs.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.rvFavorites.setVisibility(View.GONE);
                } else {
                    binding.layoutEmpty.setVisibility(View.GONE);
                    binding.rvFavorites.setVisibility(View.VISIBLE);
                }

                songAdapter.updateData(favoriteSongs, favoriteIds);
            });
        });
    }

    private void toggleFavoriteSong(Song song, boolean isFav) {
        if (!checkLogin()) return;
        String email = UserPreferences.getEmail(requireContext());

        databaseExecutor.execute(() -> {
            if (isFav) {
                databaseHelper.removeFavorite(email, song.getTrackId());
            } else {
                databaseHelper.addFavorite(email, song);
            }

            mainThreadHandler.post(() -> {
                if (getContext() == null) return;
                Toast.makeText(
                        requireContext(),
                        isFav ? "Dihapus dari Favorit" : "Disimpan ke Favorit",
                        Toast.LENGTH_SHORT
                ).show();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateFavoriteIcon(song.getTrackId());
                }

                loadOfflineFavorites();
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOfflineFavorites();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}
