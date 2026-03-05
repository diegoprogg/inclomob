package com.inclomob.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.inclomob.R;
import com.inclomob.services.FirebaseHelper;

public class TrackingActivity extends BaseActivity {
    private FirebaseHelper firebaseHelper;
    private FusedLocationProviderClient fusedLocationClient;

    // Agora guardamos as posições que vêm do Firebase
    private FirebaseHelper.UserPos remoteUserPos = null;
    private FirebaseHelper.BusData remoteBusData = null;

    private TextView txtBusInfo, txtEta, txtLineId;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        txtBusInfo = findViewById(R.id.txt_bus_info);
        txtEta = findViewById(R.id.txt_eta);
        txtLineId = findViewById(R.id.txt_line_id);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        findViewById(R.id.nav_config).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        String lineId = getIntent().getStringExtra("LINE_ID");
        if (lineId != null) {
            txtLineId.setText(lineId);
        }

        firebaseHelper = new FirebaseHelper();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkLocationPermission();
        setupVoiceFAB(findViewById(R.id.fab_voice));
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates(); // Envia GPS real para o Firebase
            startSync(); // Escuta o Firebase para atualizar a tela
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location loc = locationResult.getLastLocation();
                    if (loc != null) {
                        // Atualiza o banco. O listener do startSync() vai detectar isso e atualizar a tela.
                        firebaseHelper.updateUserLocation(loc.getLatitude(), loc.getLongitude());
                    }
                }
            }, Looper.getMainLooper());
        }
    }

    private void startSync() {
        // ESCUTA O ÔNIBUS
        firebaseHelper.trackBus(new FirebaseHelper.BusCallback() {
            @Override
            public void onUpdate(FirebaseHelper.BusData data) {
                remoteBusData = data;
                refreshUI();
            }
            @Override
            public void onError(String error) { Toast.makeText(TrackingActivity.this, error, Toast.LENGTH_SHORT).show(); }
        });

        // ESCUTA O USUÁRIO (Permite que você mude manualmente no console do Firebase!)
        firebaseHelper.trackUser(new FirebaseHelper.UserCallback() {
            @Override
            public void onUpdate(FirebaseHelper.UserPos data) {
                remoteUserPos = data;
                refreshUI();
            }
        });
    }

    private void refreshUI() {
        if (remoteUserPos == null || remoteBusData == null) return;

        float[] results = new float[1];
        Location.distanceBetween(remoteUserPos.lat, remoteUserPos.lon, remoteBusData.lat, remoteBusData.lon, results);
        float distance = results[0];

        txtBusInfo.setText(String.format("%.0fm de distância • %.1f km/h", distance, remoteBusData.vel * 3.6));

        if (remoteBusData.vel > 0.5) {
            int seconds = (int) (distance / (remoteBusData.vel));
            txtEta.setText(String.format("%d min %d s", seconds / 60, seconds % 60));
        } else {
            txtEta.setText("Ônibus parado");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
            startSync();
        }
    }
}
