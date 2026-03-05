package com.inclomob.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.inclomob.R;

public class TerminalSelectionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_selection);

        CardView btnPelopidas = findViewById(R.id.card_pelopidas);
        CardView btnMacaxeira = findViewById(R.id.card_macaxeira);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        findViewById(R.id.nav_config).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        setupVoiceFAB(findViewById(R.id.fab_voice));

        btnPelopidas.setOnClickListener(v -> openLineSelection("TI Pelópidas Silveira"));
        btnMacaxeira.setOnClickListener(v -> openLineSelection("TI Macaxeira"));
    }

    private void openLineSelection(String terminalName) {
        Intent intent = new Intent(this, LineSelectionActivity.class);
        intent.putExtra("TERMINAL_NAME", terminalName);
        startActivity(intent);
    }
}
