# HisabKhata Pro - Android Application

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![UI Toolkit](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVI%2FMVVM%20%2B%20Offline--First-orange.svg)](https://developer.android.com/topic/architecture)

**HisabKhata Pro** is an enterprise-grade, offline-first digital ledger, credit management, and billing application for merchants, small businesses, and retail stores. Built completely with modern Android development best practices using Jetpack Compose, Material 3, Room, Kotlin Coroutines & Flow, and Hilt dependency injection.

---

## 📱 Features

- **📒 Customer & Supplier Ledgers**: Complete debit/credit ledger tracking with real-time balance computation and running balance history.
- **⚡ Offline-First Architecture**: Work seamlessly without internet connectivity; changes are committed locally to Room DB and synchronized in the background via Outbox Queue and WorkManager.
- **🔄 Cloud Sync**: Bi-directional real-time sync with Firebase Realtime Database and Edge REST APIs.
- **📎 Invoice & Receipt Attachments**: Attach bill receipts and payment proofs directly to transactions with full-screen zoomable preview.
- **💬 Smart Payment Reminders**:
  - **WhatsApp**: Instant reminder messages with formatted digital statement links.
  - **SMS**: Pre-filled SMS templates with recipient and balance information.
  - **Direct Email**: Dispatches transactional email reminders via Cloudflare Worker edge backend.
  - **Pay Link**: Quick one-tap copy of customer digital statement URL.
- **📊 Business Reports & Statements**: Generate GST-ready PDF account statements and CSV exports.
- **🔐 Enterprise Security**: Encrypted session storage (`EncryptedSharedPreferences`), biometric authentication, and strict multi-merchant data isolation.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose & Material 3
- **Architecture**: Clean Architecture + MVI/MVVM with Unidirectional Data Flow (UDF)
- **Local Persistence**: Room SQLite with reactive Flow streams
- **Dependency Injection**: Dagger Hilt
- **Asynchronous & Concurrency**: Kotlin Coroutines & StateFlow / SharedFlow
- **Networking**: Retrofit 2 + OkHttp + Kotlinx Serialization
- **Image Loading**: Coil Compose
- **Background Tasks**: AndroidX WorkManager
- **Backend Services**: Cloudflare Workers (Hono framework) + Cloudflare R2 Storage + Firebase Realtime Database

---

## 📦 Download APK

Click the button below to download the latest APK directly to your device:

[![Download APK](https://img.shields.io/badge/⬇️%20Direct%20Download-app--debug.apk-2563EB?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/raw/main/apk/app-debug.apk)

> 🔗 **Direct Download Link**: [**Click here to download `app-debug.apk`**](https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/raw/main/apk/app-debug.apk)

### Installation
```bash
adb install -r apk/app-debug.apk
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17 or JDK 21
- Android SDK 35+

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/SumanCH8514/HisabKhata-Pro-Android-app.git
   cd HisabKhata-Pro-Android-app
   ```
2. Open the project in Android Studio.
3. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 👨‍💻 Author

**Suman Chakrabortty**  
Email: [dev.suman.ch@gmail.com](mailto:dev.suman.ch@gmail.com)  
GitHub: [@SumanCH8514](https://github.com/SumanCH8514)
