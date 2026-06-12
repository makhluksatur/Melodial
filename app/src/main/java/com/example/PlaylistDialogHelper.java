package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistDialogHelper {

    private static final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    public static void showAddToPlaylistDialog(final Context context, final Song song) {
        if (!UserPreferences.isLoggedIn(context)) {
            Toast.makeText(context, "Silakan login terlebih dahulu untuk menambah ke playlist", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, LoginActivity.class));
            return;
        }

        final String email = UserPreferences.getEmail(context);
        final DatabaseHelper databaseHelper = new DatabaseHelper(context);

        dbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Playlist> playlists = databaseHelper.getAllPlaylists(email);

                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Tambah ke Playlist");

                            if (playlists.isEmpty()) {
                                AlertDialog.Builder createBuilder = new AlertDialog.Builder(context);
                                android.view.View dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_create_playlist, null);
                                createBuilder.setView(dialogView);

                                final AlertDialog dialog = createBuilder.create();

                                final EditText etPlaylistName = dialogView.findViewById(R.id.etPlaylistName);
                                android.view.View btnCancel = dialogView.findViewById(R.id.btnCancel);
                                android.view.View btnCreate = dialogView.findViewById(R.id.btnCreate);

                                btnCancel.setOnClickListener(v -> dialog.dismiss());

                                btnCreate.setOnClickListener(v -> {
                                    final String name = etPlaylistName.getText().toString().trim();
                                    if (!name.isEmpty()) {
                                        dbExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                final long newId = databaseHelper.createPlaylist(email, name);
                                                if (newId != -1) {
                                                    databaseHelper.addSongToPlaylist(newId, song);
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(context, "Playlist '" + name + "' dibuat & lagu dimasukkan!", Toast.LENGTH_SHORT).show();
                                                            triggerLibraryRefresh(context);
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                } else {
                                                    ((Activity) context).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(context, "Tulis nama lain. Playlist mungkin sudah ada.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else {
                                        etPlaylistName.setError("Nama tidak boleh kosong");
                                    }
                                });

                                dialog.show();
                            } else {
                                String[] items = new String[playlists.size() + 1];
                                for (int i = 0; i < playlists.size(); i++) {
                                    items[i] = playlists.get(i).getName();
                                }
                                items[playlists.size()] = "[ + Buat Playlist Baru & Tambahkan ]";

                                builder.setItems(items, (dialog, which) -> {
                                    if (which == playlists.size()) {
                                        showCreateNewAndAddDialog(context, databaseHelper, song, email);
                                    } else {
                                        final Playlist selectedPlaylist = playlists.get(which);
                                        dbExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                final boolean success = databaseHelper.addSongToPlaylist(selectedPlaylist.getId(), song);
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (success) {
                                                            Toast.makeText(context, "Dimasukkan ke Playlist: " + selectedPlaylist.getName(), Toast.LENGTH_SHORT).show();
                                                            triggerLibraryRefresh(context);
                                                        } else {
                                                            Toast.makeText(context, "Lagu sudah ada di playlist ini!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton("Batal", null);
                                builder.show();
                            }
                        }
                    });
                }
            }
        });
    }

    private static void showCreateNewAndAddDialog(final Context context, final DatabaseHelper databaseHelper, final Song song, final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        android.view.View dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_create_playlist, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        final EditText etPlaylistName = dialogView.findViewById(R.id.etPlaylistName);
        android.view.View btnCancel = dialogView.findViewById(R.id.btnCancel);
        android.view.View btnCreate = dialogView.findViewById(R.id.btnCreate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v1 -> {
            final String name = etPlaylistName.getText().toString().trim();
            if (!name.isEmpty()) {
                dbExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        final long newId = databaseHelper.createPlaylist(email, name);
                        if (newId != -1) {
                            databaseHelper.addSongToPlaylist(newId, song);
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Playlist '" + name + "' berhasil dibuat dan ditambahkan!", Toast.LENGTH_SHORT).show();
                                    triggerLibraryRefresh(context);
                                    dialog.dismiss();
                                }
                            });
                        } else {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Tulis nama lain. Playlist mungkin sudah ada.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            } else {
                etPlaylistName.setError("Nama tidak boleh kosong");
            }
        });

        dialog.show();
    }

    private static void triggerLibraryRefresh(Context context) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).refreshLibraryFragmentIfExists();
        }
    }
}
