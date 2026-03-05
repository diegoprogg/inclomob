package com.inclomob.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.inclomob.utils.LocaleHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    protected SpeechRecognizer speechRecognizer;
    protected TextToSpeech tts;
    protected static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final Map<String, String> GLOBAL_BUS_LINES = new HashMap<>();

    static {
        // Pelópidas
        GLOBAL_BUS_LINES.put("1062", "TI Pelópidas / TI PE-15");
        GLOBAL_BUS_LINES.put("1906", "TI Pelópidas / TI Macaxeira");
        GLOBAL_BUS_LINES.put("1909", "TI Pelópidas / TI Joana Bezerra");
        GLOBAL_BUS_LINES.put("1922", "Pau Amarelo / TI Pelópidas");
        GLOBAL_BUS_LINES.put("1931", "Jardim Paulista Baixo / TI Pelópidas");
        GLOBAL_BUS_LINES.put("1932", "Jardim Paulista Alto / TI Pelópidas");
        GLOBAL_BUS_LINES.put("1934", "Arthur Lundgren 1 / TI Pelópidas");
        GLOBAL_BUS_LINES.put("1935", "Paratibe / TI Pelópidas");
        GLOBAL_BUS_LINES.put("1941", "Arthur Lundgren 2 / TI Pelópidas");
        // Macaxeira
        GLOBAL_BUS_LINES.put("645", "TI Macaxeira (Av. Norte)");
        GLOBAL_BUS_LINES.put("1964", "TI Macaxeira / TI Igarassu");
        GLOBAL_BUS_LINES.put("207", "TI Macaxeira / TI Barro (BR-101)");
        GLOBAL_BUS_LINES.put("202", "TI Macaxeira / TI Barro (Várzea)");
        GLOBAL_BUS_LINES.put("901", "TI Macaxeira / TI Abreu e Lima");
        GLOBAL_BUS_LINES.put("520", "TI Macaxeira / Parnamerim");
        GLOBAL_BUS_LINES.put("601", "TI Macaxeira / P. R. Bola na Rede");
        GLOBAL_BUS_LINES.put("604", "TI Macaxeira / Alto Burity");
        GLOBAL_BUS_LINES.put("2490", "TI Macaxeira / TI Camaragibe");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Aplica o tema (Modo Escuro) antes do super.onCreate
        applyTheme();
        super.onCreate(savedInstanceState);
        initVoiceEngine();
    }

    protected void setupVoiceFAB(View fab) {
        if (fab != null) {
            fab.setOnClickListener(v -> startVoiceRecognition());
        }
    }

    private void initVoiceEngine() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {
                    Toast.makeText(BaseActivity.this, "Ouvindo...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                }

                @Override
                public void onError(int error) {
                    if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                        speakError();
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        processVoiceInput(matches.get(0));
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pt", "BR"));
            }
        });
    }

    protected void startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        speechRecognizer.startListening(intent);
    }

    protected void processVoiceInput(String input) {
        input = input.toLowerCase();
        String busId = "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(input);
        if (matcher.find()) {
            busId = matcher.group();
        }

        if (!busId.isEmpty()) {
            String lineName = GLOBAL_BUS_LINES.get(busId);
            if (lineName != null) {
                Intent intent = new Intent(this, TrackingActivity.class);
                intent.putExtra("LINE_ID", busId);
                intent.putExtra("LINE_NAME", lineName);
                startActivity(intent);
            } else {
                speakError();
            }
        } else {
            speakError();
        }
    }

    protected void speakError() {
        String text = "Rota não encontrada, tente novamente";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void vibrateStrong() {
        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Aplica o idioma (Locale)
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    protected void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}