import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";
import { getAnalytics } from "firebase/analytics";

// Firebase web configuration for project: bca-tuition-portal
export const firebaseConfig = {
  apiKey: "AIzaSyAyGTzuCABZCxBXTE7qIBa0MOcALUJ_aYU",
  authDomain: "bca-tuition-portal.firebaseapp.com",
  projectId: "bca-tuition-portal",
  storageBucket: "bca-tuition-portal.firebasestorage.app",
  messagingSenderId: "277483881388",
  appId: "1:277483881388:web:8026c0dc822690f3b4f009",
  measurementId: "G-F31PJMWT18"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);
export const analytics = typeof window !== "undefined" ? getAnalytics(app) : null;

export default app;