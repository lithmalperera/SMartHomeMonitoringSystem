import { initializeApp } from "firebase/app";
import { getAuth, signInAnonymously, onAuthStateChanged } from "firebase/auth";
import { getDatabase, ref, onValue, update, serverTimestamp, onDisconnect, get, remove } from "firebase/database";

export const firebaseConfig = {
  apiKey: "AIzaSyDpPnLN6qgBuBDxednqajlK20L-cdW0rR8",
  authDomain: "smart-home-system-8b840.firebaseapp.com",
  databaseURL: "https://smart-home-system-8b840-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "smart-home-system-8b840",
  storageBucket: "smart-home-system-8b840.firebasestorage.app",
  messagingSenderId: "584770502755",
  appId: "1:584770502755:web:f5bfd9b478d0def1bf3856",
  measurementId: "G-W677C0LRHB"
};

export const HOME_ID = "home_001";

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getDatabase(app);

export { ref, onValue, update, serverTimestamp, onDisconnect, get, remove, signInAnonymously, onAuthStateChanged };
