package com.inclomob.services;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Locale;

public class GeminiAssistant {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + API_KEY;

    private TextToSpeech tts;
    private OkHttpClient client;

    public GeminiAssistant(Context context) {
        client = new OkHttpClient();
        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(new Locale("pt", "BR"));
            }
        });
    }

    public void processVoiceCommand(String transcript, String context, final CommandCallback callback) {
        // Constrói o prompt do sistema (idêntico ao que usamos no React)
        String systemPrompt = "Você é a IA do Inclomob. Contexto atual: " + context + ". " +
                "Responda apenas em JSON com as chaves: action, payload, reply.";

        // Monta o JSON da requisição para o Gemini
        JSONObject requestBody = new JSONObject();
        try {
            JSONObject contents = new JSONObject();
            contents.put("parts", new JSONObject().put("text", transcript));
            requestBody.put("contents", contents);
            requestBody.put("systemInstruction", new JSONObject().put("parts", new JSONObject().put("text", systemPrompt)));
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder().url(MODEL_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseText = response.body().string();
                        // Aqui você parsearia o JSON retornado pelo Gemini
                        JSONObject json = new JSONObject(responseText);
                        String reply = json.getString("reply");
                        speak(reply);
                        callback.onCommandReceived(json);
                    } catch (Exception e) { callback.onError("Erro no parse"); }
                }
            }
        });
    }

    public void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public interface CommandCallback {
        void onCommandReceived(JSONObject command);
        void onError(String error);
    }
}
