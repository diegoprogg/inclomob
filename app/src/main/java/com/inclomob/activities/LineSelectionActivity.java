package com.inclomob.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inclomob.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LineSelectionActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private LineAdapter adapter;
    private List<BusLine> lines;
    private Set<Integer> selectedPositions = new HashSet<>();
    private ImageView btnFavorite;
    private ImageView btnBack;
    private String terminalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_selection);

        terminalName = getIntent().getStringExtra("TERMINAL_NAME");
        TextView txtTerminalName = findViewById(R.id.txt_terminal_name);
        txtTerminalName.setText(terminalName);

        btnFavorite = findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(v -> saveFavorites());

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            if (!selectedPositions.isEmpty()) {
                clearSelection();
            } else {
                finish();
            }
        });

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        findViewById(R.id.nav_config).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        setupVoiceFAB(findViewById(R.id.fab_voice));

        recyclerView = findViewById(R.id.recycler_lines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lines = new ArrayList<>();
        if (terminalName != null && terminalName.contains("Pelópidas")) {
            lines.add(new BusLine("1062", "TI Pelópidas / TI PE-15", "Em operação"));
            lines.add(new BusLine("1906", "TI Pelópidas / TI Macaxeira", "Em operação"));
            lines.add(new BusLine("1909", "TI Pelópidas / TI Joana Bezerra", "Em operação"));
            lines.add(new BusLine("1922", "Pau Amarelo / TI Pelópidas", "Em operação"));
            lines.add(new BusLine("1931", "Jardim Paulista Baixo / TI Pelópidas", "Em operação"));
            lines.add(new BusLine("1932", "Jardim Paulista Alto / TI Pelópidas", "Em operação"));
            lines.add(new BusLine("1934", "Arthur Lundgren 1 / TI Pelópidas", "Em operação"));
            lines.add(new BusLine("1935", "Paratibe / TI Pelópidas", "Em operação"));
            lines.add(new BusLine("1941", "Arthur Lundgren 2 / TI Pelópidas", "Em operação"));
        } else if (terminalName != null && terminalName.contains("Macaxeira")) {
            lines.add(new BusLine("645", "TI Macaxeira (Av. Norte)", "Em operação"));
            lines.add(new BusLine("1906", "TI Macaxeira / TI Pelópidas", "Em operação"));
            lines.add(new BusLine("1964", "TI Macaxeira / TI Igarassu", "Em operação"));
            lines.add(new BusLine("207", "TI Macaxeira / TI Barro (BR-101)", "Em operação"));
            lines.add(new BusLine("202", "TI Macaxeira / TI Barro (Várzea)", "Em operação"));
            lines.add(new BusLine("901", "TI Macaxeira / TI Abreu e Lima", "Em operação"));
            lines.add(new BusLine("520", "TI Macaxeira / Parnamerim", "Em operação"));
            lines.add(new BusLine("601", "TI Macaxeira / P. R. Bola na Rede", "Em operação"));
            lines.add(new BusLine("604", "TI Macaxeira / Alto Burity", "Em operação"));
            lines.add(new BusLine("2490", "TI Macaxeira / TI Camaragibe", "Em operação"));
        }

        checkFavorites();
        sortLines();

        adapter = new LineAdapter(lines, new LineAdapter.OnLineClickListener() {
            @Override
            public void onLineClick(BusLine line, int position) {
                if (!selectedPositions.isEmpty()) {
                    toggleSelection(position);
                } else {
                    addToRecents(line);
                    Intent intent = new Intent(LineSelectionActivity.this, TrackingActivity.class);
                    intent.putExtra("LINE_ID", line.id);
                    intent.putExtra("LINE_NAME", line.name);
                    startActivity(intent);
                }
            }

            @Override
            public void onLineLongClick(BusLine line, int position) {
                toggleSelection(position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
            vibrateStrong();
        }
        updateSelectionUI();
    }

    private void clearSelection() {
        selectedPositions.clear();
        updateSelectionUI();
    }

    private void updateSelectionUI() {
        adapter.setSelectedPositions(selectedPositions);
        btnFavorite.setVisibility(selectedPositions.isEmpty() ? View.GONE : View.VISIBLE);

        // Se todos selecionados forem favoritos, muda o ícone para indicar "remover"
        if (!selectedPositions.isEmpty()) {
            boolean allFavorites = true;
            for (int pos : selectedPositions) {
                if (!lines.get(pos).isFavorite) {
                    allFavorites = false;
                    break;
                }
            }
            if (allFavorites) {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
        }

        // Troca ícone de voltar por X
        if (selectedPositions.isEmpty()) {
            btnBack.setImageResource(android.widget.ImageView.ScaleType.CENTER != null ? android.R.drawable.ic_menu_revert : android.R.drawable.ic_menu_revert);
            // Revert icon
            btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        } else {
            btnBack.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }

        TextView txtTitle = findViewById(R.id.txt_title);
        if (selectedPositions.isEmpty()) {
            txtTitle.setText(R.string.select_line);
        } else {
            txtTitle.setText(selectedPositions.size() + " selecionado(s)");
        }
    }

    private void checkFavorites() {
        SharedPreferences prefs = getSharedPreferences("inclomob_prefs", MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("favorites", new HashSet<>());
        for (BusLine line : lines) {
            String entry = line.id + "|" + line.name + "|" + terminalName;
            line.isFavorite = favorites.contains(entry);
        }
    }

    private void sortLines() {
        Collections.sort(lines, (l1, l2) -> {
            if (l1.isFavorite && !l2.isFavorite) return -1;
            if (!l1.isFavorite && l2.isFavorite) return 1;
            return l1.id.compareTo(l2.id);
        });
    }

    private void saveFavorites() {
        SharedPreferences prefs = getSharedPreferences("inclomob_prefs", MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("favorites", new HashSet<>());
        Set<String> newFavorites = new HashSet<>(favorites);

        boolean anyAdded = false;
        boolean anyRemoved = false;

        for (int pos : selectedPositions) {
            BusLine line = lines.get(pos);
            String entry = line.id + "|" + line.name + "|" + terminalName;
            if (newFavorites.contains(entry)) {
                newFavorites.remove(entry);
                line.isFavorite = false;
                anyRemoved = true;
            } else {
                newFavorites.add(entry);
                line.isFavorite = true;
                anyAdded = true;
            }
        }

        prefs.edit().putStringSet("favorites", newFavorites).apply();

        sortLines();
        clearSelection();

        String msg = anyAdded && anyRemoved ? "Favoritos atualizados!" :
                anyAdded ? "Adicionado aos favoritos!" : "Removido dos favoritos!";
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void addToRecents(BusLine line) {
        SharedPreferences prefs = getSharedPreferences("inclomob_prefs", MODE_PRIVATE);
        List<String> recents = new ArrayList<>(prefs.getStringSet("recents", new HashSet<>()));

        String entry = line.id + "|" + line.name + "|" + terminalName;
        recents.remove(entry); // Remove se já existir para mover para o topo
        recents.add(0, entry);

        if (recents.size() > 10) recents = recents.subList(0, 10);

        prefs.edit().putStringSet("recents", new HashSet<>(recents)).apply();
    }

    // Classe de modelo simples
    public static class BusLine {
        public String id, name, status;
        public boolean isFavorite;
        public BusLine(String id, String name, String status) {
            this.id = id; this.name = name; this.status = status;
        }
    }

    // Adapter para o RecyclerView
    private static class LineAdapter extends RecyclerView.Adapter<LineAdapter.ViewHolder> {
        private List<BusLine> lines;
        private OnLineClickListener listener;
        private Set<Integer> selectedPositions = new HashSet<>();

        public interface OnLineClickListener {
            void onLineClick(BusLine line, int position);
            void onLineLongClick(BusLine line, int position);
        }

        public LineAdapter(List<BusLine> lines, OnLineClickListener listener) {
            this.lines = lines;
            this.listener = listener;
        }

        public void setSelectedPositions(Set<Integer> positions) {
            this.selectedPositions = new HashSet<>(positions);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_line, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BusLine line = lines.get(position);
            holder.txtId.setText(line.id);
            holder.txtName.setText(line.name);
            holder.txtStatus.setText(line.status);

            boolean isSelected = selectedPositions.contains(position);
            boolean isSelectionMode = !selectedPositions.isEmpty();

            if (isSelected) {
                holder.itemView.setBackgroundResource(line.isFavorite ? R.drawable.bg_favorite_selected_item : R.drawable.bg_selected_item);
            } else {
                holder.itemView.setBackgroundResource(line.isFavorite ? R.drawable.bg_favorite_item : R.drawable.bg_line_item);
            }

            // Efeito acinzentado (alpha reduzido) se estiver em modo de seleção e não estiver selecionado
            if (isSelectionMode && !isSelected) {
                holder.itemView.setAlpha(0.4f);
            } else {
                holder.itemView.setAlpha(1.0f);
            }

            holder.itemView.setOnClickListener(v -> listener.onLineClick(line, position));
            holder.itemView.setOnLongClickListener(v -> {
                listener.onLineLongClick(line, position);
                return true;
            });
        }

        @Override
        public int getItemCount() { return lines.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtId, txtName, txtStatus;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtId = itemView.findViewById(R.id.txt_line_id);
                txtName = itemView.findViewById(R.id.txt_line_name);
                txtStatus = itemView.findViewById(R.id.txt_line_status);
            }
        }
    }
}
