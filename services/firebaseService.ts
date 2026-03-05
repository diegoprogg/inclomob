import { UserPosition, BusData } from '../types';

declare const firebase: any;

// --- Firebase configuration (from user prompt)
const firebaseConfig = {
  apiKey: "AIzaSyCl4dRZxhg9gRvHTbLBmugAC1oo75EiYnM",
  authDomain: "inclomob.firebaseapp.com",
  databaseURL: "https://inclomob-default-rtdb.firebaseio.com",
  projectId: "inclomob",
  storageBucket: "inclomob.firebasestorage.app",
  messagingSenderId: "867202100738",
  appId: "1:867202100738:web:e89f815f778decfbfec11b",
  measurementId: "G-C8RT7KLF3Q"
};

// Initialize Firebase only if it hasn't been initialized yet
if (!firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}
const db = firebase.database();

// Function to save user's location to /usuario
export function updateUserLocation(userPos: UserPosition): void {
  db.ref("usuario").set({
    lat: userPos.lat,
    lon: userPos.lon,
    ts: Date.now()
  });
}

// Haversine function: returns distance in meters
function haversine(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371e3; // Earth radius in meters
  const toRad = (v: number) => (v * Math.PI) / 180;
  const φ1 = toRad(lat1);
  const φ2 = toRad(lat2);
  const dφ = toRad(lat2 - lat1);
  const dλ = toRad(lon2 - lon1);
  const a = Math.sin(dφ/2)**2 + Math.cos(φ1)*Math.cos(φ2)*Math.sin(dλ/2)**2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// Real-time listener for the bus location
// Returns a cleanup function to detach the listener
export function trackBus(userPos: UserPosition, onUpdate: (data: BusData) => void): () => void {
  const ref = db.ref('onibus'); // Corrected path to /onibus
  
  const listener = (snapshot: any) => {
    const v = snapshot.val();
    if (!v || typeof v.lat !== 'number' || typeof v.lon !== 'number') {
      onUpdate({ error: 'Sem dados do ônibus' });
      return;
    }
    const busPos = { lat: v.lat, lon: v.lon };
    const distM = haversine(userPos.lat, userPos.lon, busPos.lat, busPos.lon);
    const vel = typeof v.vel === 'number' ? v.vel : 0; // m/s
    let etaText = '—';
    if (vel > 0.5) {
      const tempoSeg = distM / vel;
      const min = Math.floor(tempoSeg / 60);
      const seg = Math.round(tempoSeg % 60);
      etaText = `${min} min ${seg} s`;
    }
    onUpdate({
      distanceMeters: Math.round(distM),
      velocityKmh: (vel * 3.6).toFixed(1),
      eta: etaText,
      rawBusData: v
    });
  };
  
  ref.on('value', listener);
  
  // Return a cleanup function to be called on component unmount
  return () => ref.off('value', listener);
}