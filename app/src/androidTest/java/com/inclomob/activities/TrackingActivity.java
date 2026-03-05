package com.inclomob.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.inclomob.R;
import com.inclomob.services.FirebaseHelper;

public class TrackingActivity extends AppCompatActivity {
    private FirebaseHelper firebaseHelper;
    private TextView txtDistancia, txtVelocidade, txtEta, txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        firebaseHelper = new FirebaseHelper();

        // Inicia o rastreamento
        startTracking();
    }

    private void startTracking() {
        firebaseHelper.trackBus(new FirebaseHelper.BusCallback() {
            @Override
            public void onUpdate(FirebaseHelper.BusData data) {
                // Aqui você usaria a localização atual do usuário (GPS do Android)
                // Para este exemplo, vamos assumir uma posição fixa ou vinda do GPS
                double userLat = -8.0476; // Exemplo: Recife
                double userLon = -34.8770;

                double distMeters = calculateDistance(userLat, userLon, data.lat, data.lon);
                double velKmh = data.vel * 3.6;

                updateUI(distMeters, velKmh);
            }

            @Override
            public void onError(String error) {
                txtStatus.setText("Erro: " + error);
            }
        });
    }

    private void updateUI(double dist, double vel) {
        txtDistancia.setText(String.format("%.0f metros", dist));
        txtVelocidade.setText(String.format("%.1f km/h", vel));

        // Cálculo simples de ETA (Tempo Estimado)
        if (vel > 2.0) { // Se o ônibus estiver se movendo
            double timeSeconds = dist / (vel / 3.6);
            int minutes = (int) (timeSeconds / 60);
            int seconds = (int) (timeSeconds % 60);
            txtEta.setText(String.format("%d min %d s", minutes, seconds));
        } else {
            txtEta.setText("Calculando...");
        }
    }

    // Fórmula de Haversine para calcular distância entre dois pontos GPS
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3; // Raio da Terra em metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
