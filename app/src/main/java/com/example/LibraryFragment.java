package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.databinding.FragmentLibraryBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;

    private PlaylistAdapter playlistAdapter;
    private final List<Playlist> playlistsList = new ArrayList<>();
    private final Map<Long, Integer> trackCountsMap = new HashMap<>();

    private SongAdapter songsAdapter;
    private final List<Song> currentPlaylistSongs = new ArrayList<>();
    private final Set<Long> favoriteIds = new HashSet<>();

    private Playlist selectedPlaylist = null;
    private DatabaseHelper databaseHelper;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = new DatabaseHelper(requireContext());

        setupAdapters();
        setupListeners();
        loadPlaylists();
        loadFavorites();
    }

    private void setupAdapters() {
        binding.rvPlaylists.setLayoutManager(new LinearLayoutManager(requireContext()));
        playlistAdapter = new PlaylistAdapter(
                playlistsList,
                trackCountsMap,
                playlist -> openPlaylistDetail(playlist),
                playlist -> {
                    if (checkLogin()) {
                        confirmDeletePlaylist(playlist);
                    }
                }
        );
        binding.rvPlaylists.setAdapter(playlistAdapter);

        binding.rvPlaylistSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        songsAdapter = new SongAdapter(
                currentPlaylistSongs,
                favoriteIds,
                song -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).playSong(song);
                    }
                },
                (song, isFavorite) -> toggleSongFavorite(song, isFavorite),
                song -> {
                    if (checkLogin()) {
                        PlaylistDialogHelper.showAddToPlaylistDialog(requireContext(), song);
                    }
                },
                song -> {
                    if (checkLogin() && selectedPlaylist != null) {
                        confirmRemoveSongFromPlaylist(selectedPlaylist, song);
                    }
                    return true;
                }
        );
        binding.rvPlaylistSongs.setAdapter(songsAdapter);
    }

    private void setupListeners() {
        binding.btnCreatePlaylistTop.setOnClickListener(v -> {
            if (checkLogin()) showCreatePlaylistDialog();
        });
        binding.btnCreatePlaylistEmpty.setOnClickListener(v -> {
            if (checkLogin()) showCreatePlaylistDialog();
        });
        binding.btnBackToPlaylists.setOnClickListener(v -> closePlaylistDetail());
        binding.btnDeletePlaylist.setOnClickListener(v -> {
            if (checkLogin() && selectedPlaylist != null) {
                confirmDeletePlaylist(selectedPlaylist);
            }
        });
    }

    private boolean checkLogin() {
        if (!UserPreferences.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return false;
        }
        return true;
    }

    public void loadPlaylists() {
        if (binding == null || getContext() == null) return;
        String email = UserPreferences.getEmail(requireContext());

        databaseExecutor.execute(() -> {
            List<Playlist> playlists = databaseHelper.getAllPlaylists(email);
            Map<Long, Integer> counts = new HashMap<>();
            for (Playlist playlist : playlists) {
                List<Song> songsInList = databaseHelper.getSongsInPlaylist(playlist.getId());
                counts.put(playlist.getId(), songsInList.size());
            }

            mainThreadHandler.post(() -> {
                if (binding == null) return;

                playlistsList.clear();
                playlistsList.addAll(playlists);
                trackCountsMap.clear();
                trackCountsMap.putAll(counts);

                if (playlistsList.isEmpty()) {
                    binding.layoutEmptyPlaylists.setVisibility(View.VISIBLE);
                    binding.rvPlaylists.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyPlaylists.setVisibility(View.GONE);
                    binding.rvPlaylists.setVisibility(View.VISIBLE);
                }

                playlistAdapter.updateData(playlistsList, trackCountsMap);

                if (selectedPlaylist != null) {
                    Playlist refreshedActive = null;
                    for (Playlist p : playlistsList) {
                        if (p.getId() == selectedPlaylist.getId()) {
                            refreshedActive = p;
                            break;
                        }
                    }
                    if (refreshedActive != null) {
                        loadPlaylistSongsOnly(refreshedActive);
                    } else {
                        closePlaylistDetail();
                    }
                }
            });
        });
    }

    private void loadFavorites() {
        if (getContext() == null) return;
        String email = UserPreferences.getEmail(requireContext());
        databaseExecutor.execute(() -> {
            List<Song> favorites = databaseHelper.getAllFavorites(email);
            Set<Long> ids = new HashSet<>();
            for (Song s : favorites) {
                ids.add(s.getTrackId());
            }
            mainThreadHandler.post(() -> {
                if (binding == null) return;
                favoriteIds.clear();
                favoriteIds.addAll(ids);
                songsAdapter.updateData(currentPlaylistSongs, favoriteIds);
            });
        });
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etPlaylistName = dialogView.findViewById(R.id.etPlaylistName);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);
        View btnCreate = dialogView.findViewById(R.id.btnCreate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etPlaylistName.getText().toString().trim();
            if (!name.isEmpty()) {
                String email = UserPreferences.getEmail(requireContext());
                databaseExecutor.execute(() -> {
                    long resultId = databaseHelper.createPlaylist(email, name);
                    mainThreadHandler.post(() -> {
                        if (getContext() == null) return;
                        if (resultId != -1) {
                            Toast.makeText(requireContext(), "Playlist '" + name + "' berhasil dibuat!", Toast.LENGTH_SHORT).show();
                            loadPlaylists();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Nama sudah digunakan atau terjadi error", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                etPlaylistName.setError("Nama tidak boleh kosong");
            }
        });

        dialog.show();
    }

    private void confirmDeletePlaylist(Playlist playlist) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Playlist")
                .setMessage("Apakah Anda yakin ingin menghapus playlist '" + playlist.getName() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    databaseExecutor.execute(() -> {
                        boolean success = databaseHelper.deletePlaylist(playlist.getId());
                        mainThreadHandler.post(() -> {
                            if (getContext() == null) return;
                            if (success) {
                                Toast.makeText(requireContext(), "Playlist '" + playlist.getName() + "' berhasil dihapus", Toast.LENGTH_SHORT).show();
                                if (selectedPlaylist != null && selectedPlaylist.getId() == playlist.getId()) {
                                    closePlaylistDetail();
                                }
                                loadPlaylists();
                            } else {
                                Toast.makeText(requireContext(), "Gagal menghapus playlist", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void openPlaylistDetail(Playlist playlist) {
        selectedPlaylist = playlist;
        binding.layoutPlaylistList.setVisibility(View.GONE);
        binding.layoutPlaylistDetail.setVisibility(View.VISIBLE);

        binding.tvDetailPlaylistTitle.setText(playlist.getName());
        loadPlaylistSongsOnly(playlist);
    }

    private void loadPlaylistSongsOnly(Playlist playlist) {
        databaseExecutor.execute(() -> {
            List<Song> songs = databaseHelper.getSongsInPlaylist(playlist.getId());
            mainThreadHandler.post(() -> {
                if (binding == null) return;
                currentPlaylistSongs.clear();
                currentPlaylistSongs.addAll(songs);

                binding.tvDetailPlaylistSubtitle.setText(songs.size() + " Lagu • Tahan lagu untuk menghapus");

                if (songs.isEmpty()) {
                    binding.tvEmptyPlaylistSongs.setVisibility(View.VISIBLE);
                    binding.rvPlaylistSongs.setVisibility(View.GONE);
                } else {
                    binding.tvEmptyPlaylistSongs.setVisibility(View.GONE);
                    binding.rvPlaylistSongs.setVisibility(View.VISIBLE);
                }

                songsAdapter.updateData(currentPlaylistSongs, favoriteIds);
            });
        });
    }

    private void confirmRemoveSongFromPlaylist(Playlist playlist, Song song) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus dari Playlist")
                .setMessage("Apakah Anda yakin ingin menghapus '" + song.getTrackName() + "' dari playlist '" + playlist.getName() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    databaseExecutor.execute(() -> {
                        boolean success = databaseHelper.removeSongFromPlaylist(playlist.getId(), song.getTrackId());
                        mainThreadHandler.post(() -> {
                            if (getContext() == null) return;
                            if (success) {
                                Toast.makeText(requireContext(), "'" + song.getTrackName() + "' dihapus dari playlist", Toast.LENGTH_SHORT).show();
                                loadPlaylists();
                            } else {
                                Toast.makeText(requireContext(), "Gagal menghapus lagu", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void closePlaylistDetail() {
        selectedPlaylist = null;
        binding.layoutPlaylistDetail.setVisibility(View.GONE);
        binding.layoutPlaylistList.setVisibility(View.VISIBLE);
    }

    private void toggleSongFavorite(Song song, boolean isFav) {
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
                loadFavorites();
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlaylists();
        loadFavorites();
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
