package com.inclomob.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import com.inclomob.R;
import com.inclomob.utils.LocaleHelper;

public class SettingsActivity extends BaseActivity {

    private SwitchCompat switchDarkMode;
    private RelativeLayout layoutLanguage;
    private TextView txtSelectedLanguage;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Pega o nome do usuário (padrão Diego se não houver)
        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName == null || userName.isEmpty()) userName = "Diego";

        TextView txtUserName = findViewById(R.id.txt_user_name_settings);
        TextView txtAvatar = findViewById(R.id.txt_avatar_settings);

        txtUserName.setText(userName);
        txtAvatar.setText(userName.substring(0, 1).toUpperCase());

        // Modo Escuro
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Reinicia a activity para aplicar o tema
            recreate();
        });

        // Idioma
        layoutLanguage = findViewById(R.id.layout_language_selector);
        txtSelectedLanguage = findViewById(R.id.txt_selected_language);
        updateLanguageText();

        layoutLanguage.setOnClickListener(v -> showLanguageDialog());

        // Navegação Bottom
        LinearLayout navHome = findViewById(R.id.nav_home);
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME"));
            startActivity(intent);
            finish(); // Fecha a tela de config ao voltar para home
        });

        // Botão Sair
        TextView btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void updateLanguageText() {
        String lang = LocaleHelper.getLanguage(this);
        if (lang.equals("en")) {
            txtSelectedLanguage.setText(R.string.lang_en);
        } else if (lang.equals("es")) {
            txtSelectedLanguage.setText(R.string.lang_es);
        } else {
            txtSelectedLanguage.setText(R.string.lang_pt);
        }
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.lang_pt), getString(R.string.lang_en), getString(R.string.lang_es)};
        String[] codes = {"pt", "en", "es"};

        int current = 0;
        String currentCode = LocaleHelper.getLanguage(this);
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentCode)) {
                current = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.language_label)
                .setSingleChoiceItems(languages, current, (dialog, which) -> {
                    LocaleHelper.setLocale(this, codes[which]);
                    dialog.dismiss();
                    // Reinicia o app para aplicar o idioma em todas as telas
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME"));
                    startActivity(intent);
                })
                .show();
    }
}
