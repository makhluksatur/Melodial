package com.example;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import coil.ImageLoader;
import coil.request.ImageRequest;
import com.example.databinding.FragmentPlayerBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Locale;

public class PlayerBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentPlayerBottomSheetBinding binding;
    private MainActivity mainActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateProgressRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlayerBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            setupUI();
            startProgressUpdate();
        }
    }

    private void setupUI() {
        Song currentSong = mainActivity.getCurrentSong();
        if (currentSong == null) return;

        binding.tvExpandedSongTitle.setText(currentSong.getTrackName());
        binding.tvExpandedArtist.setText(currentSong.getArtistName());

        String artworkUrl = currentSong.getArtworkUrl100();
        if (artworkUrl != null && !artworkUrl.isEmpty()) {
            String highResUrl = artworkUrl.replace("100x100bb", "600x600bb");
            ImageLoader imageLoader = coil.Coil.imageLoader(requireContext());
            ImageRequest request = new ImageRequest.Builder(requireContext())
                    .data(highResUrl)
                    .placeholder(android.R.drawable.ic_media_play)
                    .error(android.R.drawable.ic_media_play)
                    .target(binding.ivExpandedAlbumArt)
                    .build();
            imageLoader.enqueue(request);
        }

        updateFavoriteUI();
        updatePlayPauseButton();

        binding.fabExpandedPlayPause.setOnClickListener(v -> {
            mainActivity.togglePlayPause();
            updatePlayPauseButton();
        });

        binding.btnExpandedFavorite.setOnClickListener(v -> {
            mainActivity.toggleFavoriteSong(currentSong);
            updateFavoriteUI();
        });

        binding.expandedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mainActivity.getMediaPlayer() != null) {
                    binding.tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateProgressRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mainActivity.getMediaPlayer() != null) {
                    mainActivity.getMediaPlayer().seekTo(seekBar.getProgress());
                }
                startProgressUpdate();
            }
        });
    }

    private void updatePlayPauseButton() {
        MediaPlayer mp = mainActivity.getMediaPlayer();
        if (mp != null && mp.isPlaying()) {
            binding.fabExpandedPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            binding.fabExpandedPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void updateFavoriteUI() {
        Song currentSong = mainActivity.getCurrentSong();
        String email = UserPreferences.getEmail(requireContext());
        if (currentSong != null && !email.isEmpty()) {
            boolean isFav = mainActivity.getDatabaseHelper().isFavorite(email, currentSong.getTrackId());
            binding.btnExpandedFavorite.setImageResource(
                    isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
            );
        }
    }

    private void startProgressUpdate() {
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                MediaPlayer mp = mainActivity.getMediaPlayer();
                if (mp != null) {
                    int currentPos = mp.getCurrentPosition();
                    int totalPos = mp.getDuration();

                    binding.expandedSeekBar.setMax(totalPos);
                    binding.expandedSeekBar.setProgress(currentPos);
                    binding.tvCurrentTime.setText(formatTime(currentPos));
                    binding.tvTotalTime.setText(formatTime(totalPos));

                    updatePlayPauseButton();
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateProgressRunnable);
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateProgressRunnable);
        binding = null;
    }
}
