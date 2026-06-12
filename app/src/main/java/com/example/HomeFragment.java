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
import com.example.databinding.FragmentHomeBinding;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SongAdapter songAdapter;
    private final List<Song> songList = new ArrayList<>();
    private final Set<Long> favoriteIds = new HashSet<>();
    private DatabaseHelper databaseHelper;
    private boolean isShowingPopularFilter = false;
    private String selectedGenre = "pop";

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = new DatabaseHelper(requireContext());

        setupRecyclerView();
        setupListeners();
        styleAllGenreChips();

        loadSongsFromApi("pop");
        refreshFavoritesList();
    }

    private void setupRecyclerView() {
        binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

        songAdapter = new SongAdapter(
                songList,
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
        binding.rvSongs.setAdapter(songAdapter);
    }

    private void setupListeners() {
        binding.btnRefresh.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            String query = "";
            if (mainActivity != null && mainActivity.binding != null && mainActivity.binding.etSearchMain != null) {
                query = mainActivity.binding.etSearchMain.getText().toString().trim();
            }
            loadSongsFromApi(query.isEmpty() ? (isShowingPopularFilter ? "viral hits" : (selectedGenre != null && !selectedGenre.isEmpty() ? selectedGenre : "pop")) : query);
        });

        binding.bentoFavoritesCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.binding.bottomNavigation.setSelectedItemId(R.id.navigation_favorite);
            }
        });

        binding.bentoPopularCard.setOnClickListener(v -> {
            isShowingPopularFilter = !isShowingPopularFilter;
            if (isShowingPopularFilter) {
                Toast.makeText(requireContext(), "Menampilkan lagu terpopuler dan sering didengar", Toast.LENGTH_SHORT).show();
                binding.tvTrackListHeader.setText("Populer & Sering Didengar");
                selectedGenre = "";
                styleAllGenreChips();
                loadSongsFromApi("viral hits");
            } else {
                Toast.makeText(requireContext(), "Menampilkan rekomendasi default", Toast.LENGTH_SHORT).show();
                selectedGenre = "pop";
                binding.tvTrackListHeader.setText("Rekomendasi Lagu");
                styleAllGenreChips();
                loadSongsFromApi("pop");
            }
        });

        binding.btnGenrePop.setOnClickListener(v -> updateGenreSelection("pop"));
        binding.btnGenreRock.setOnClickListener(v -> updateGenreSelection("rock"));
        binding.btnGenreHipHop.setOnClickListener(v -> updateGenreSelection("hip hop"));
        binding.btnGenreRnB.setOnClickListener(v -> updateGenreSelection("rnb"));
        binding.btnGenreKPop.setOnClickListener(v -> updateGenreSelection("k-pop"));
        binding.btnGenreJazz.setOnClickListener(v -> updateGenreSelection("jazz"));
        binding.btnGenreCountry.setOnClickListener(v -> updateGenreSelection("country"));
        binding.btnGenreIndie.setOnClickListener(v -> updateGenreSelection("indie"));
    }

    private boolean checkLogin() {
        if (!UserPreferences.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return false;
        }
        return true;
    }

    public void searchFromExternalQuery(String query) {
        if (query != null && !query.isEmpty()) {
            isShowingPopularFilter = false;
            selectedGenre = "";
            styleAllGenreChips();
            binding.tvTrackListHeader.setText("Hasil Pencarian: " + query);
            loadSongsFromApi(query);
        }
    }

    private void loadSongsFromApi(String term) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvSongs.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        RetrofitClient.getInstance().searchSongs(term).enqueue(new Callback<ITunesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ITunesResponse> call, @NonNull Response<ITunesResponse> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ITunesResponse body = response.body();
                    songList.clear();
                    if (body.getResults() != null) {
                        List<Song> fetchedSongs = new ArrayList<>(body.getResults());
                        
                        if (selectedGenre != null && !selectedGenre.trim().isEmpty()) {
                            List<Song> genreFiltered = new ArrayList<>();
                            for (Song song : fetchedSongs) {
                                if (matchesGenre(song.getPrimaryGenreName(), selectedGenre)) {
                                    genreFiltered.add(song);
                                }
                            }
                            if (genreFiltered.size() >= 3) {
                                fetchedSongs = genreFiltered;
                            } else {
                                java.util.Collections.sort(fetchedSongs, (s1, s2) -> {
                                    boolean m1 = matchesGenre(s1.getPrimaryGenreName(), selectedGenre);
                                    boolean m2 = matchesGenre(s2.getPrimaryGenreName(), selectedGenre);
                                    if (m1 && !m2) return -1;
                                    if (!m1 && m2) return 1;
                                    return 0;
                                });
                            }
                        }

                        if (isShowingPopularFilter) {
                            java.util.Collections.sort(fetchedSongs, (s1, s2) -> {
                                int count1 = UserPreferences.getPlayCount(requireContext(), s1.getTrackId());
                                int count2 = UserPreferences.getPlayCount(requireContext(), s2.getTrackId());
                                return Integer.compare(count2, count1);
                            });
                        } else if (term != null && !term.trim().isEmpty() && (selectedGenre == null || selectedGenre.trim().isEmpty())) {
                            java.util.Collections.sort(fetchedSongs, (s1, s2) -> {
                                int score1 = calculateRelevanceScore(s1, term);
                                int score2 = calculateRelevanceScore(s2, term);
                                return Integer.compare(score2, score1);
                            });
                        }
                        
                        songList.addAll(fetchedSongs);
                    }
                    binding.rvSongs.setVisibility(View.VISIBLE);
                    songAdapter.updateData(songList, favoriteIds);
                } else {
                    showFallbackError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ITunesResponse> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                showFallbackError();
            }
        });
    }

    private void showFallbackError() {
        binding.rvSongs.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        
        final String email = UserPreferences.getEmail(requireContext());
        databaseExecutor.execute(() -> {
            List<Song> favorites = databaseHelper.getAllFavorites(email);
            mainThreadHandler.post(() -> {
                if (binding == null) return;
                if (favorites != null && !favorites.isEmpty()) {
                    songList.clear();
                    songList.addAll(favorites);
                    binding.tvTrackListHeader.setText("Musik Favorit Anda (Offline)");
                    binding.rvSongs.setVisibility(View.VISIBLE);
                    songAdapter.updateData(songList, favoriteIds);
                    Toast.makeText(requireContext(), "Koneksi offline. Memuat daftar musik favorit Anda.", Toast.LENGTH_SHORT).show();
                } else {
                    binding.tvTrackListHeader.setText("Rekomendasi Lagu");
                }
            });
        });
    }

    public void refreshFavoritesList() {
        if (getContext() == null) return;
        final String email = UserPreferences.getEmail(requireContext());
        databaseExecutor.execute(() -> {
            List<Song> favorites = databaseHelper.getAllFavorites(email);
            Set<Long> idsSet = new HashSet<>();
            for (Song f : favorites) {
                idsSet.add(f.getTrackId());
            }

            mainThreadHandler.post(() -> {
                if (binding == null) return;
                favoriteIds.clear();
                favoriteIds.addAll(idsSet);

                binding.tvBentoFavCount.setText(favorites.size() + " Lagu");
                songAdapter.updateData(songList, favoriteIds);
            });
        });
    }

    private void toggleFavoriteSong(Song song, boolean isFav) {
        if (!checkLogin()) return;
        final String email = UserPreferences.getEmail(requireContext());

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

                refreshFavoritesList();
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFavoritesList();
        updateGreeting();
    }

    private void updateGreeting() {
        if (binding == null || getContext() == null) return;
        String name = UserPreferences.getName(requireContext());
        if (!UserPreferences.isLoggedIn(requireContext()) || name == null || name.trim().isEmpty()) {
            binding.tvAwesomeUserName.setText("Halo, Sobat Melodi!");
        } else {
            binding.tvAwesomeUserName.setText("Halo, " + name + "!");
        }
    }

    private boolean matchesGenre(String primaryGenre, String targetGenre) {
        if (primaryGenre == null) return false;
        String p = primaryGenre.toLowerCase();
        String t = targetGenre.toLowerCase();
        if (t.equals("hip hop")) {
            return p.contains("hip hop") || p.contains("rap");
        }
        if (t.equals("rnb")) {
            return p.contains("r&b") || p.contains("soul");
        }
        return p.contains(t);
    }

    private int calculateRelevanceScore(Song song, String query) {
        if (query == null || query.trim().isEmpty()) {
            return 0;
        }
        String lowerQuery = query.toLowerCase().trim();
        String lowerTrack = song.getTrackName() != null ? song.getTrackName().toLowerCase() : "";
        String lowerArtist = song.getArtistName() != null ? song.getArtistName().toLowerCase() : "";
        String lowerCollection = song.getCollectionName() != null ? song.getCollectionName().toLowerCase() : "";

        if (lowerTrack.equals(lowerQuery)) return 100;
        else if (lowerTrack.startsWith(lowerQuery)) return 90;
        else if (lowerTrack.contains(lowerQuery)) return 80;

        if (lowerArtist.equals(lowerQuery)) return 70;
        else if (lowerArtist.startsWith(lowerQuery)) return 60;
        else if (lowerArtist.contains(lowerQuery)) return 50;

        if (lowerCollection.equals(lowerQuery)) return 40;
        else if (lowerCollection.startsWith(lowerQuery)) return 30;
        else if (lowerCollection.contains(lowerQuery)) return 20;

        return 0;
    }

    private void updateGenreSelection(String newGenre) {
        selectedGenre = newGenre.toLowerCase();
        isShowingPopularFilter = false;
        
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.binding != null && mainActivity.binding.etSearchMain != null) {
                mainActivity.binding.etSearchMain.setText("");
            }
        }

        String formattedTitle = newGenre.substring(0, 1).toUpperCase() + newGenre.substring(1);
        if (selectedGenre.equals("rnb")) formattedTitle = "R&B";
        else if (selectedGenre.equals("k-pop")) formattedTitle = "K-Pop";
        binding.tvTrackListHeader.setText("Rekomendasi: " + formattedTitle);
        
        styleAllGenreChips();
        loadSongsFromApi(selectedGenre);
    }

    private void styleAllGenreChips() {
        if (binding == null || getContext() == null) return;
        styleGenreCard(binding.btnGenrePop, binding.tvGenrePopText, "pop".equals(selectedGenre));
        styleGenreCard(binding.btnGenreRock, binding.tvGenreRockText, "rock".equals(selectedGenre));
        styleGenreCard(binding.btnGenreHipHop, binding.tvGenreHipHopText, "hip hop".equals(selectedGenre));
        styleGenreCard(binding.btnGenreRnB, binding.tvGenreRnBText, "rnb".equals(selectedGenre));
        styleGenreCard(binding.btnGenreKPop, binding.tvGenreKPopText, "k-pop".equals(selectedGenre));
        styleGenreCard(binding.btnGenreJazz, binding.tvGenreJazzText, "jazz".equals(selectedGenre));
        styleGenreCard(binding.btnGenreCountry, binding.tvGenreCountryText, "country".equals(selectedGenre));
        styleGenreCard(binding.btnGenreIndie, binding.tvGenreIndieText, "indie".equals(selectedGenre));
    }

    private void styleGenreCard(com.google.android.material.card.MaterialCardView card, android.widget.TextView text, boolean isSelected) {
        if (isSelected) {
            card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary)
            ));
            card.setStrokeColor(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary)
            ));
            text.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            android.util.TypedValue typedValue = new android.util.TypedValue();
            requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
            int colorSurface = typedValue.data;
            
            requireContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
            int textColorPrimary = typedValue.data;

            card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(colorSurface));
            card.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#40808080")));
            text.setTextColor(textColorPrimary);
        }
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
