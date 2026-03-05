package com.inclomob.activities;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inclomob.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Pega o nome do usuário que veio do Login
        String fullName = getIntent().getStringExtra("USER_NAME");
        if (fullName == null || fullName.isEmpty()) fullName = "Diego";

        // Extrai apenas o primeiro nome
        String firstName = fullName.trim().split("\\s+")[0];

        TextView txtGreeting = findViewById(R.id.txt_user_greeting);
        TextView txtAvatar = findViewById(R.id.txt_avatar_initial);

        txtGreeting.setText(getString(R.string.greeting_hello, firstName));
        txtAvatar.setText(firstName.substring(0, 1).toUpperCase());

        MaterialButton btnTrack = findViewById(R.id.btn_track_bus);
        btnTrack.setOnClickListener(v -> {
            // Vai para a seleção de terminais
            Intent intent = new Intent(this, TerminalSelectionActivity.class);
            startActivity(intent);
        });

        FloatingActionButton fabVoice = findViewById(R.id.fab_voice);
        setupVoiceFAB(fabVoice);

        // Navegação Bottom
        LinearLayout navConfig = findViewById(R.id.nav_config);
        navConfig.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME"));
            startActivity(intent);
        });

        loadFavoritesAndRecents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritesAndRecents();
    }

    private void loadFavoritesAndRecents() {
        SharedPreferences prefs = getSharedPreferences("inclomob_prefs", MODE_PRIVATE);

        // Favoritos
        Set<String> favoritesSet = prefs.getStringSet("favorites", new HashSet<>());
        LinearLayout containerFavorites = findViewById(R.id.container_favorites);
        TextView labelFavorites = findViewById(R.id.label_favorites);
        containerFavorites.removeAllViews();

        if (favoritesSet.isEmpty()) {
            labelFavorites.setVisibility(View.GONE);
        } else {
            labelFavorites.setVisibility(View.VISIBLE);
            for (String fav : favoritesSet) {
                String[] parts = fav.split("\\|");
                if (parts.length >= 3) {
                    addRouteItem(containerFavorites, parts[0], parts[1], parts[2], R.layout.item_favorite);
                }
            }
        }

        // Recentes
        Set<String> recentsSet = prefs.getStringSet("recents", new HashSet<>());
        LinearLayout containerRecents = findViewById(R.id.container_recents);
        TextView labelRecents = findViewById(R.id.label_recents);
        containerRecents.removeAllViews();

        if (recentsSet.isEmpty()) {
            labelRecents.setVisibility(View.GONE);
        } else {
            labelRecents.setVisibility(View.VISIBLE);
            // Mostra os últimos 3 recentes
            List<String> recentsList = new ArrayList<>(recentsSet);
            int count = 0;
            for (int i = recentsList.size() - 1; i >= 0 && count < 3; i--) {
                String[] parts = recentsList.get(i).split("\\|");
                if (parts.length >= 3) {
                    addRouteItem(containerRecents, parts[0], parts[1], parts[2], R.layout.item_recent);
                    count++;
                }
            }
        }
    }

    private void addRouteItem(LinearLayout container, String id, String name, String terminal, int layoutId) {
        View view = LayoutInflater.from(this).inflate(layoutId, container, false);
        TextView txtId = view.findViewById(R.id.txt_line_id);
        TextView txtTerminal = view.findViewById(R.id.txt_terminal_name);

        txtId.setText("Linha " + id);
        txtTerminal.setText(terminal);

        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrackingActivity.class);
            intent.putExtra("LINE_ID", id);
            intent.putExtra("LINE_NAME", name);
            startActivity(intent);
        });

        container.addView(view);
    }
}
