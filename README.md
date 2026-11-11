<div align="center">

# 📒 HisabKhata Pro

**Modern, Offline-First Digital Ledger & Smart Credit Management Platform for Android**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVI--MVVM-FF6F00?style=flat-square)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

[**Download Latest APK**](https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/raw/main/apk/app-debug.apk) • [**Web Platform**](https://hisabkhata.sumanonline.com/) • [**Report Issue**](https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/issues)

</div>

---

## 🌟 Overview

**HisabKhata Pro** replaces paper accounting notebooks (*Bahi-Khata*) with a resilient, offline-first mobile ledger application tailored for merchants, retail shops, and small businesses. 

Engineered with Android's modern toolkit (Jetpack Compose, Room SQLite, Coroutines, and WorkManager), it enables business owners to record credit and debit entries with zero latency, even during broadband blackouts. When network connectivity is restored, an autonomous outbox synchronization engine reconciles local transactions with cloud databases seamlessly.

---

## ✨ Features

- **⚡ High-Velocity Ledger Management**:
  - Record **You Gave (Credit / Red)** and **You Got (Payment / Green)** entries in seconds.
  - Automatic running balance calculation with strict decimal precision.
- **📴 100% Offline-First Architecture**:
  - Full local functionality powered by Room SQLite.
  - Transactions save instantly to an internal durable Outbox queue with idempotent collision guards (`clientTxId`).
  - Automatic background reconciliation via AndroidX WorkManager upon network reconnect.
- **💳 Instant UPI Payment QR**:
  - Dynamic `upi://pay` QR code generation for any customer outstanding due or custom amount.
  - One-tap intent to launch installed UPI apps (Google Pay, PhonePe, Paytm).
- **📊 Business Reports & Statements**:
  - Generate GST-compliant PDF account statements with opening and closing balances.
  - Share statements directly via WhatsApp or standard Android Sharesheet.
  - Spreadsheet (CSV) export for audits and tax accounting.
- **💬 Smart Payment Reminders**:
  - Personalized WhatsApp reminder templates with customer digital statement links.
  - Transactional reminder emails dispatched via Cloudflare Worker edge API.
- **🔐 Enterprise Security**:
  - Hardware-backed Android Keystore credential encryption (`EncryptedSharedPreferences`).
  - Optional biometric (fingerprint / face unlock) security barrier.

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Language** | Kotlin 2.0+ | Modern, expressive, and type-safe language |
| **UI Toolkit** | Jetpack Compose, Material 3 | Declarative, smooth, responsive native UI |
| **Architecture** | Clean Architecture, MVVM / MVI | Unidirectional Data Flow (UDF) & separation of concerns |
| **Local Database** | Room (SQLite) | Reactive offline persistence with Kotlin Flow |
| **Background Sync** | AndroidX WorkManager | Battery-conscious background synchronization |
| **Networking** | Retrofit 2, OkHttp 4, Kotlinx Serialization | REST API integration with Cloudflare Workers |
| **Security** | AndroidX Biometric, Security-Crypto | Hardware-backed session and biometric authentication |
| **Image Loading** | Coil Compose | Asynchronous image loading and caching |
| **QR Engine** | ZXing Core | Instant vector-based UPI payment QR generation |

---

## 📂 Project Structure

```text
HisabKhata/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── res/                         # Vector drawables, themes, strings
│   │   └── java/com/sumanonline/hisabkhata/
│   │       ├── MainActivity.kt          # Single-activity container with Biometric gate
│   │       ├── HisabKhataApplication.kt # Application configuration & Hilt setup
│   │       ├── core/
│   │       │   ├── common/              # Math utilities, CurrencyFormatter, Result models
│   │       │   ├── database/            # Room Database, DAOs, and Entities
│   │       │   ├── designsystem/        # Color tokens, Typography, Theme, Shared components
│   │       │   ├── di/                  # Dagger Hilt injection modules
│   │       │   ├── network/             # Retrofit interfaces & data transfer objects
│   │       │   ├── security/            # Secure session manager (Keystore)
│   │       │   └── sync/                # SyncEngine & WorkManager background sync
│   │       └── feature/
│   │           ├── auth/                # Login, Register, Password Reset
│   │           ├── customer/            # Customer directory, Ledger timeline, Entry sheets
│   │           ├── navigation/          # Compose Navigation router & routes
│   │           ├── payments/            # Dynamic UPI QR generator & verification
│   │           ├── profile/             # Merchant settings, biometric toggle, shop profile
│   │           ├── reminders/           # WhatsApp & Email statement dispatchers
│   │           └── reports/             # PDF statement generator & CSV export
│   └── build.gradle.kts
├── docs/android/                        # Architecture & migration documentation
├── gradle/                              # Version catalog (libs.versions.toml) & wrapper
└── apk/                                 # Pre-compiled debug APK distributions
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1+) or newer
- **JDK**: Version 17 or 21
- **Android SDK**: Compile SDK 35 / Min SDK 26 (Android 8.0+)

### Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/SumanCH8514/HisabKhata-Pro-Android-app.git
   cd HisabKhata-Pro-Android-app
   ```

2. **Configure environment keys**:
   Create or edit `local.properties` in the project root:
   ```properties
   sdk.dir=YOUR_ANDROID_SDK_PATH
   FIREBASE_API_KEY=YOUR_FIREBASE_API_KEY
   ```

3. **Open and Sync**:
   Open the root directory in Android Studio and let Gradle resolve dependencies.

4. **Build from CLI**:
   ```bash
   # Assemble Debug APK
   ./gradlew assembleDebug

   # Run Unit Tests
   ./gradlew testDebugUnitTest
   ```

---

## 📱 Quick Usage

1. **Manage Parties**: Tap **+ Add Party** from the home screen to add customers or suppliers with optional opening balances.
2. **Record Entries**:
   - Tap **You Gave ₹** (Red) to record credit given to a customer.
   - Tap **You Got ₹** (Green) to record payment received.
3. **Send Reminders**: Open any customer ledger and tap **WhatsApp Reminder** to share an instant payment link.
4. **Generate Reports**: Navigate to **Reports**, pick a duration (This Month, Last 7 Days, or All), and tap **PDF Statement**.

---

## 📦 Direct APK Download

Ready-to-install debug builds are bundled directly with releases:

[![Download APK](https://img.shields.io/badge/⬇️%20Direct%20Download-app--debug.apk-2563EB?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/raw/main/apk/app-debug.apk)

Install directly to a connected device:
```bash
adb install -r apk/app-debug.apk
```

---

## 🤝 Contributing

Contributions make the open-source community an inspiring place to learn and build:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the **MIT License**. See `LICENSE` for more information.

---

## 👨‍💻 Author

**Suman Chakrabortty**
- GitHub: [@SumanCH8514](https://github.com/SumanCH8514)
- Email: [dev.suman.ch@gmail.com](mailto:dev.suman.ch@gmail.com)
- Live Web App: [hisabkhata.sumanonline.com](https://hisabkhata.sumanonline.com)
