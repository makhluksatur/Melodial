package com.example;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import coil.ImageLoader;
import coil.request.ImageRequest;
import com.example.databinding.ActivityMainBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding binding;
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private DatabaseHelper databaseHelper;

    private final ExecutorService playbackExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDark = ThemePreferences.isDarkMode(this);
        ThemePreferences.applyTheme(isDark);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        setupNavigation();
        setupMiniPlayer();

        binding.btnProfileAvatar.setOnClickListener(v -> {
            Intent intent;
            if (UserPreferences.isLoggedIn(this)) {
                intent = new Intent(this, ProfileActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
        });

        binding.etSearchMain.setOnEditorActionListener((v, actionId, event) -> {
            String query = binding.etSearchMain.getText().toString().trim();
            if (!query.isEmpty()) {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                if (navHostFragment != null) {
                    androidx.fragment.app.Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().isEmpty() ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);

                    if (currentFragment instanceof HomeFragment) {
                        ((HomeFragment) currentFragment).searchFromExternalQuery(query);
                    } else {
                        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
                        mainThreadHandler.postDelayed(() -> {
                            androidx.fragment.app.Fragment targetFragment = navHostFragment.getChildFragmentManager().getFragments().isEmpty() ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
                            if (targetFragment instanceof HomeFragment) {
                                ((HomeFragment) targetFragment).searchFromExternalQuery(query);
                            }
                        }, 200);
                    }
                }
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileAvatar();
    }

    private void loadProfileAvatar() {
        if (binding == null) return;
        
        if (!UserPreferences.isLoggedIn(this)) {
            binding.btnProfileAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            return;
        }

        String avatarUri = UserPreferences.getAvatarUri(this);
        if (avatarUri != null && !avatarUri.isEmpty()) {
            ImageLoader imageLoader = coil.Coil.imageLoader(this);
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(avatarUri)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .target(binding.btnProfileAvatar)
                    .build();
            imageLoader.enqueue(request);
        } else {
            binding.btnProfileAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavigationUI.setupWithNavController(binding.bottomNavigation, navHostFragment.getNavController());
        }
    }

    private void setupMiniPlayer() {
        binding.cardMiniPlayer.setVisibility(View.GONE);

        binding.cardMiniPlayer.setOnClickListener(v -> {
            if (currentSong != null) {
                showExpandedPlayer();
            }
        });

        binding.btnMiniPlayPause.setOnClickListener(v -> togglePlayPause());

        binding.btnMiniFavorite.setOnClickListener(v -> {
            if (currentSong != null) {
                toggleFavoriteSong(currentSong);
            }
        });
    }

    private void showExpandedPlayer() {
        PlayerBottomSheetFragment playerFragment = new PlayerBottomSheetFragment();
        playerFragment.show(getSupportFragmentManager(), "PlayerBottomSheet");
    }

    public void playSong(Song song) {
        if (!UserPreferences.isLoggedIn(this)) {
            Toast.makeText(this, "Silakan login terlebih dahulu untuk memutar lagu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        currentSong = song;
        UserPreferences.incrementPlayCount(this, song.getTrackId());
        binding.cardMiniPlayer.setVisibility(View.VISIBLE);

        binding.tvMiniSongTitle.setText(song.getTrackName() != null ? song.getTrackName() : "Tanpa Judul");
        binding.tvMiniArtist.setText(song.getArtistName() != null ? song.getArtistName() : "Artis Tidak Diketahui");

        String artworkUrl = song.getArtworkUrl100();
        if (artworkUrl != null && !artworkUrl.isEmpty()) {
            ImageLoader imageLoader = coil.Coil.imageLoader(this);
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(artworkUrl)
                    .placeholder(android.R.drawable.ic_media_play)
                    .error(android.R.drawable.ic_media_play)
                    .target(binding.ivMiniAlbumArt)
                    .build();
            imageLoader.enqueue(request);
        } else {
            binding.ivMiniAlbumArt.setImageResource(android.R.drawable.ic_media_play);
        }

        updateFavoriteIcon(song.getTrackId());

        playbackExecutor.execute(() -> {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(song.getPreviewUrl());
                mediaPlayer.prepare();

                mainThreadHandler.post(() -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        binding.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        startProgressTracker();
                    }
                });
            } catch (Exception e) {
                mainThreadHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Gagal menyiapkan audio: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                binding.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                binding.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                startProgressTracker();
            }
        }
    }

    private void startProgressTracker() {
        if (progressRunnable != null) {
            mainThreadHandler.removeCallbacks(progressRunnable);
        }

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    try {
                        int duration = mediaPlayer.getDuration();
                        int currentPos = mediaPlayer.getCurrentPosition();
                        if (duration > 0) {
                            int progress = (currentPos * 100) / duration;
                            binding.miniPlayerProgress.setProgress(progress);
                        }
                    } catch (Exception e) {
                    }
                    mainThreadHandler.postDelayed(this, 1000);
                }
            }
        };
        mainThreadHandler.post(progressRunnable);
    }

    public void toggleFavoriteSong(Song song) {
        if (!UserPreferences.isLoggedIn(this)) {
            Toast.makeText(this, "Silakan login terlebih dahulu untuk menambahkan favorit", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        String email = UserPreferences.getEmail(this);
        playbackExecutor.execute(() -> {
            boolean isCurrentlyFavorite = databaseHelper.isFavorite(email, song.getTrackId());
            if (isCurrentlyFavorite) {
                databaseHelper.removeFavorite(email, song.getTrackId());
            } else {
                databaseHelper.addFavorite(email, song);
            }
            boolean updatedFavoriteStatus = !isCurrentlyFavorite;

            mainThreadHandler.post(() -> {
                binding.btnMiniFavorite.setImageResource(
                        updatedFavoriteStatus ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
                );
                Toast.makeText(
                        MainActivity.this,
                        updatedFavoriteStatus ? "Disimpan ke Favorit" : "Dihapus dari Favorit",
                        Toast.LENGTH_SHORT
                ).show();

                for (androidx.fragment.app.Fragment parentFragment : getSupportFragmentManager().getFragments()) {
                    for (androidx.fragment.app.Fragment child : parentFragment.getChildFragmentManager().getFragments()) {
                        if (child instanceof FavoriteFragment) {
                            ((FavoriteFragment) child).loadOfflineFavorites();
                        } else if (child instanceof HomeFragment) {
                            ((HomeFragment) child).refreshFavoritesList();
                        }
                    }
                }
            });
        });
    }

    public void updateFavoriteIcon(long trackId) {
        String email = UserPreferences.getEmail(this);
        if (email.isEmpty()) return;
        
        playbackExecutor.execute(() -> {
            boolean isFavorite = databaseHelper.isFavorite(email, trackId);
            mainThreadHandler.post(() -> {
                binding.btnMiniFavorite.setImageResource(
                        isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
                );
            });
        });
    }

    public void refreshLibraryFragmentIfExists() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            androidx.fragment.app.Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().isEmpty() ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (currentFragment instanceof LibraryFragment) {
                ((LibraryFragment) currentFragment).loadPlaylists();
            }
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressRunnable != null) {
            mainThreadHandler.removeCallbacks(progressRunnable);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        playbackExecutor.shutdown();
    }
}
