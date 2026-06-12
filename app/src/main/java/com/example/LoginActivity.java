package com.example;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private boolean isSignUpMode = false;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDark = ThemePreferences.isDarkMode(this);
        ThemePreferences.applyTheme(isDark);

        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        updateUiMode();

        binding.btnToggleMode.setOnClickListener(v -> {
            isSignUpMode = !isSignUpMode;
            updateUiMode();
        });

        binding.btnBackLogin.setOnClickListener(v -> finish());
        binding.btnSubmitAuth.setOnClickListener(v -> performAuth());
    }

    private void updateUiMode() {
        if (isSignUpMode) {
            binding.tvLoginSubtitle.setText("Daftar gratis untuk mulai mendengarkan musik");
            binding.tvFormHeading.setText("Buat Akun Baru");
            binding.tilFullName.setVisibility(View.VISIBLE);
            binding.btnSubmitAuth.setText("Daftar Sekarang");
            binding.btnToggleMode.setText("Sudah memiliki akun? Masuk disini");
        } else {
            binding.tvLoginSubtitle.setText("Masuk untuk mempersonalisasi musikmu");
            binding.tvFormHeading.setText("Form Masuk");
            binding.tilFullName.setVisibility(View.GONE);
            binding.btnSubmitAuth.setText("Masuk");
            binding.btnToggleMode.setText("Belum punya akun? Daftar gratis");
        }
    }

    private void performAuth() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Alamat email diperlukan");
            binding.etEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Sandi minimal berisikan 6 karakter");
            binding.etPassword.requestFocus();
            return;
        }

        if (isSignUpMode) {
            String fullName = binding.etFullName.getText().toString().trim();
            if (fullName.isEmpty()) {
                binding.etFullName.setError("Nama Lengkap diperlukan");
                binding.etFullName.requestFocus();
                return;
            }

            if (databaseHelper.isEmailRegistered(email)) {
                Toast.makeText(this, "Email sudah terdaftar. Silakan masuk.", Toast.LENGTH_SHORT).show();
                isSignUpMode = false;
                updateUiMode();
                return;
            }

            boolean success = databaseHelper.registerUser(email, fullName, password);
            if (success) {
                UserPreferences.saveProfile(this, fullName, email);
                UserPreferences.setLoggedIn(this, true);
                Toast.makeText(this, "Registrasi Akun berhasil!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Registrasi gagal, coba lagi.", Toast.LENGTH_SHORT).show();
            }

        } else {
            if (!databaseHelper.isEmailRegistered(email)) {
                Toast.makeText(this, "Email tidak terdaftar. Silakan daftar akun baru.", Toast.LENGTH_LONG).show();
                return;
            }

            Cursor cursor = databaseHelper.getUser(email);
            if (cursor != null && cursor.moveToFirst()) {
                int passIndex = cursor.getColumnIndex("password");
                int nameIndex = cursor.getColumnIndex("name");
                int avatarIndex = cursor.getColumnIndex("avatar_uri");
                
                String storedPassword = (passIndex >= 0) ? cursor.getString(passIndex) : "";
                String storedName = (nameIndex >= 0) ? cursor.getString(nameIndex) : "";
                String storedAvatar = (avatarIndex >= 0) ? cursor.getString(avatarIndex) : "";
                cursor.close();

                if (password.equals(storedPassword)) {
                    UserPreferences.saveProfile(this, storedName, email);
                    if (storedAvatar != null && !storedAvatar.isEmpty()) {
                        UserPreferences.saveAvatarUri(this, storedAvatar);
                    }
                    UserPreferences.setLoggedIn(this, true);
                    Toast.makeText(this, "Berhasil masuk! Selamat datang kembali, " + storedName, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    binding.etPassword.setError("Sandi salah");
                    binding.etPassword.requestFocus();
                }
            } else {
                if (cursor != null) cursor.close();
                Toast.makeText(this, "Terjadi kesalahan saat memuat data.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
