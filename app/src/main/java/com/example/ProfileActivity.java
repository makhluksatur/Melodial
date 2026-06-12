package com.example;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.databinding.ActivityProfileBinding;
import coil.ImageLoader;
import coil.request.ImageRequest;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseHelper databaseHelper;

    private final androidx.activity.result.ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveAndApplySelectedAvatar(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDark = ThemePreferences.isDarkMode(this);
        ThemePreferences.applyTheme(isDark);

        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        loadUserProfile();

        binding.layoutAvatarContainer.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
        binding.btnUploadAvatarText.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
        binding.switchProfileDarkMode.setChecked(isDark);
        binding.switchProfileDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> ThemePreferences.setDarkMode(this, isChecked));
        binding.btnSaveProfile.setOnClickListener(v -> saveUserProfile());
        binding.btnProfileLogout.setOnClickListener(v -> logoutUser());
        binding.btnBackProfile.setOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        String currentName = UserPreferences.getName(this);
        String currentEmail = UserPreferences.getEmail(this);

        if (currentName.isEmpty()) currentName = "Sobat Melodi";
        if (currentEmail.isEmpty()) currentEmail = "-";

        binding.etProfileName.setText(currentName);
        binding.etProfileEmail.setText(currentEmail);
        binding.tvProfileAccentName.setText(currentName);
        binding.tvProfileAccentEmail.setText(currentEmail);

        String avatarUri = UserPreferences.getAvatarUri(this);
        if (avatarUri != null && !avatarUri.isEmpty()) {
            ImageLoader imageLoader = coil.Coil.imageLoader(this);
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(avatarUri)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .target(binding.ivLargeAvatar)
                    .build();
            imageLoader.enqueue(request);
        } else {
            binding.ivLargeAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    private void saveAndApplySelectedAvatar(android.net.Uri selectedImageUri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            if (inputStream == null) return;
            java.io.File file = new java.io.File(getFilesDir(), "profile_" + System.currentTimeMillis() + ".jpg");
            java.io.OutputStream outputStream = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            String localPath = file.getAbsolutePath();
            UserPreferences.saveAvatarUri(this, localPath);
            
            String email = UserPreferences.getEmail(this);
            if (!email.isEmpty()) {
                databaseHelper.updateUserAvatar(email, localPath);
            }

            ImageLoader imageLoader = coil.Coil.imageLoader(this);
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(localPath)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .target(binding.ivLargeAvatar)
                    .build();
            imageLoader.enqueue(request);

            Toast.makeText(this, "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memproses foto profil", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserProfile() {
        String newName = binding.etProfileName.getText().toString().trim();
        String currentEmail = UserPreferences.getEmail(this);

        if (newName.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        UserPreferences.saveProfile(this, newName, currentEmail);
        if (!currentEmail.isEmpty()) {
            databaseHelper.updateUser(currentEmail, newName);
        }

        binding.tvProfileAccentName.setText(newName);
        Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
    }

    private void logoutUser() {
        UserPreferences.clearSession(this);
        Toast.makeText(this, "Berhasil logout.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
