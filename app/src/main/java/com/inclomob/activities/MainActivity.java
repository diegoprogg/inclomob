package com.inclomob.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.inclomob.R;

public class MainActivity extends BaseActivity {
    private EditText editNome;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editNome = findViewById(R.id.edit_nome);
        btnEntrar = findViewById(R.id.btn_entrar);

        btnEntrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString();
            if (!nome.isEmpty()) {
                // Navega para a tela Home (Dashboard)
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("USER_NAME", nome);
                startActivity(intent);
            }
        });
    }
}
