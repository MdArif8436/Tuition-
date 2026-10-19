BCA Tuition & HOD Management Platform
A secure, scalable, multi-college education platform for HODs, teachers, students, and parents. It supports Google/email sign-in, attendance, fees and payments, assignments, quizzes, marks, announcements, AI doubt assistance, and more.

Features
Accounts and Security
Google Sign-In

Email/password sign-in

Email verification

Forgot password / password reset via Gmail

Role-based access: Super Admin, HOD, Teacher, Student, Parent

Secure role switching with individual password/PIN

App lock using fingerprint/PIN for HOD, Teacher, and Parent

Firebase App Check, Crashlytics, and audit logs

Academic Management
Multi-college, multi-batch, multi-subject support

Student attendance tracking with QR check-in

Current study topics and syllabus progress

Online quizzes with timer and auto-scoring

Test marks entry and CSV/Excel bulk upload

Homework/assignment submission with deadlines

Student discussion forum with moderation

Class calendar with reminders

Daily updates with announcements, code snippets, PDFs, and photos

Fees and Payments
Fee structure and installment plans

UPI Intent payments (Google Pay, PhonePe, Paytm, BHIM, Navi)

UPI QR code

Credit/debit card, net banking, and wallets via payment gateway

Payment verification and PDF receipts

Automatic fee due and overdue reminders

Teacher Management
Teacher registration with CV/resume upload

HOD review and approval workflow

Teacher profiles with qualifications, subjects, and experience

Subject and batch assignment

Extra Features
AI Doubt Assistant trained only on approved study material

Digital student ID card with verification QR

Announcements with read status

Student leaderboard

Class/test feedback and ratings

Referral system

HOD analytics, backup, restore, and export

Offline caching and automatic data sync

In-app updates via Google Play

Tech Stack
Android: Kotlin, Jetpack Compose, Material 3, Hilt, Coroutines, Flow, Room

Backend: Firebase Authentication, Cloud Firestore, Firebase Storage, Cloud Functions

Messaging: Firebase Cloud Messaging

Security: Firebase App Check, Firestore Security Rules, Storage Rules

Payments: Razorpay or PayU (UPI, cards, net banking, wallets)

AI: RAG-based doubt assistant using approved notes/PDFs

Emails: Firebase Auth templates plus Trigger Email/SendGrid/Resend

Project Structure
text
app/
  src/main/java/com/example/
    ui/                 # Compose screens and navigation
    viewmodel/          # ViewModels
    data/               # Repositories, Firestore, Room
    model/              # Data classes
    util/               # Helpers, FirebaseInitializer
    di/                 # Hilt modules
cloud-functions/        # Server-side logic, payment webhooks, emails
firestore.rules         # Firestore security rules
storage.rules           # Firebase Storage rules
Setup
1. Firebase Setup
Create a Firebase project.

Add an Android app with your exact applicationId.

Download google-services.json.

Place it at:

text
app/google-services.json
Enable:

Firebase Authentication

Cloud Firestore

Firebase Storage

Firebase Cloud Messaging

Firebase App Check

Firebase Crashlytics

2. Google Sign-In
Enable Google as a sign-in provider in Firebase Authentication. Add your debug and release SHA-1 fingerprints in Firebase project settings.

3. Email Verification and Password Reset
Firebase Authentication sends verification and password-reset emails automatically. Customize the templates from:

text
Firebase Console → Authentication → Templates
4. Build and Run
bash
./gradlew clean
./gradlew :app:assembleDebug
./gradlew :app:installDebug
Security Rules
Deploy Firestore and Storage rules:

bash
firebase deploy --only firestore:rules,storage
Never use public rules such as:

js
allow read, write: if true;
Use role-based rules so:

Students access only their own data

Parents access only linked child data

Teachers access only assigned colleges/batches/subjects

HODs access only their own college

Sensitive actions use Cloud Functions

API Keys and Secrets
Item	Location
Firebase Android config	app/google-services.json
Firebase Web config	Frontend environment/config file
Payment gateway public key	Frontend/environment config
Payment gateway secret key	Cloud Functions environment/secret manager
AI API key	Cloud Functions environment/secret manager
SMTP/SendGrid/Resend key	Cloud Functions environment/secret manager
Firebase service account	Server/Cloud Functions secret manager
UPI ID	Firebase Remote Config or Firestore app settings
Never commit secrets, service-account JSON files, payment secret keys, or AI API keys to GitHub.

Payment Flow
Student/parent opens Fees → Pay Now.

Selects full payment, installment, or custom amount.

Chooses UPI app, card, net banking, or wallet.

Completes payment.

App shows Success, Pending, Failed, or Cancelled.

Payment is verified server-side before fee status becomes Paid.

PDF receipt and notification are generated.

Testing
bash
./gradlew test
./gradlew connectedAndroidTest
Test checklist:

Google sign-in and email verification

Password reset via Gmail

Student registration with unique roll number

Teacher CV upload and HOD approval

Role-based dashboards

Attendance and QR check-in

Fee payment and installment reminders

Assignment submission and grading

Quiz attempt and scoring

Announcements with read status

AI assistant restricted to uploaded notes

App lock, backup, export, and notifications

Deployment
Android
bash
./gradlew :app:bundleRelease
Upload the generated .aab to Google Play Console.

Cloud Functions
bash
cd cloud-functions
npm install
firebase deploy --only functions
Support
For issues, check Logcat for Firebase, Firestore, Storage, and HWUI messages. HWUI/EGL warnings such as EGL_SWAP_BEHAVIOR_PRESERVED and Unknown dataspace 0 are often harmless emulator graphics fallback messages if the UI renders correctly.
