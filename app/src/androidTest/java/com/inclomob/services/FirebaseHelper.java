package com.inclomob.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {
    private DatabaseReference db;

    public FirebaseHelper() {
        // Inicializa o Firebase Realtime Database
        db = FirebaseDatabase.getInstance().getReference();
    }

    // Atualiza localização do usuário
    public void updateUserLocation(double lat, double lon) {
        UserPos pos = new UserPos(lat, lon, System.currentTimeMillis());
        db.child("usuario").setValue(pos);
    }

    // Escuta a localização do ônibus
    public void trackBus(final BusCallback callback) {
        db.child("onibus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    BusData data = snapshot.getValue(BusData.class);
                    callback.onUpdate(data);
                } else {
                    callback.onError("Sem dados do ônibus");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public interface BusCallback {
        void onUpdate(BusData data);
        void onError(String error);
    }

    // Classes de Modelo (POJOs)
    public static class UserPos {
        public double lat, lon;
        public long ts;
        public UserPos() {}
        public UserPos(double lat, double lon, long ts) {
            this.lat = lat; this.lon = lon; this.ts = ts;
        }
    }

    public static class BusData {
        public double lat, lon, vel;
        public BusData() {}
    }
}
