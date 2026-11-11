# HisabKhata Pro — Native Android Migration Plan

## 1. Architectural Strategy

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3 Design Tokens
- **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **Dependency Injection**: Hilt
- **Persistence**: Room Database (SQLite) with encrypted preferences for keys
- **Network Layer**: OkHttp 4 + Retrofit 2 + Firebase Android SDK (Auth & Realtime Database)
- **Background Synchronization**: AndroidX WorkManager with network constraints
- **Security**: Android Keystore, EncryptedSharedPreferences, ProGuard / R8 minification

## 2. Target Project Structure

```
android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/sumanonline/hisabkhata/
│           ├── HisabKhataApp.kt
│           ├── MainActivity.kt
│           ├── navigation/
│           │   ├── AppNavHost.kt
│           │   └── Screen.kt
│           ├── core/
│           │   ├── common/
│           │   │   ├── Result.kt
│           │   │   ├── Constants.kt
│           │   │   └── Extensions.kt
│           │   ├── designsystem/
│           │   │   ├── theme/
│           │   │   │   ├── Color.kt
│           │   │   │   ├── Theme.kt
│           │   │   │   └── Type.kt
│           │   │   └── components/
│           │   │       ├── AppTopBar.kt
│           │   │       ├── AppBottomBar.kt
│           │   │       ├── LoadingOverlay.kt
│           │   │       ├── EmptyState.kt
│           │   │       └── StatCard.kt
│           │   ├── database/
│           │   │   ├── HisabKhataDatabase.kt
│           │   │   ├── entity/
│           │   │   │   ├── CustomerEntity.kt
│           │   │   │   ├── TransactionEntity.kt
│           │   │   │   └── OutboxOperationEntity.kt
│           │   │   └── dao/
│           │   │       ├── CustomerDao.kt
│           │   │       ├── TransactionDao.kt
│           │   │       └── OutboxDao.kt
│           │   ├── network/
│           │   │   ├── BackendApi.kt
│           │   │   ├── FirebaseService.kt
│           │   │   └── NetworkMonitor.kt
│           │   ├── sync/
│           │   │   ├── SyncManager.kt
│           │   │   └── SyncWorker.kt
│           │   └── security/
│           │       └── SecureStorage.kt
│           └── feature/
│               ├── auth/
│               │   ├── AuthViewModel.kt
│               │   ├── LoginScreen.kt
│               │   ├── RegisterScreen.kt
│               │   └── ForgotPasswordScreen.kt
│               ├── dashboard/
│               │   ├── DashboardViewModel.kt
│               │   └── DashboardScreen.kt
│               ├── customer/
│               │   ├── CustomerListViewModel.kt
│               │   ├── CustomerListScreen.kt
│               │   ├── CustomerDetailScreen.kt
│               │   ├── AddEditCustomerScreen.kt
│               │   └── AddTransactionDialog.kt
│               ├── reports/
│               │   ├── ReportsViewModel.kt
│               │   ├── ReportsScreen.kt
│               │   └── PdfReportGenerator.kt
│               ├── payments/
│               │   ├── PaymentsViewModel.kt
│               │   └── PaymentsScreen.kt
│               ├── reminders/
│               │   ├── RemindersViewModel.kt
│               │   └── RemindersScreen.kt
│               └── profile/
│                   ├── ProfileViewModel.kt
│                   └── ProfileScreen.kt
```

## 3. Milestones & Implementation Stages

1. **Setup Core Android Structure**: Gradle config, versions catalog, manifest, permissions, Hilt DI.
2. **Core Theme & Design System**: Material 3 theme matching `#0057BB` Brand Blue and Red/Green Give/Get tokens.
3. **Room Database & Offline Outbox**: Schema definitions for Customer, Transaction, OutboxOperation with complete DAOs.
4. **Network & Backend Client**: Retrofit for Cloudflare Worker (`/api/upload`, `/api/send-email`) and Firebase RTDB service.
5. **Sync Engine**: WorkManager periodic & one-time sync workers with transactional rollback and duplicate safeguards.
6. **Feature Screens**:
   - Authentication (Login, Register, Forgot Password)
   - Dashboard & Overview Metrics
   - Customer Directory (Search, Sort, Filters for You'll Get / You'll Give / Settled / Due)
   - Customer Ledger (Give / Get Drawers, Entry Details, Due Date Chips)
   - PDF & Excel Statements
   - Instant UPI QR & Payment Verification Queue
   - Profile & Business Settings
7. **Testing & Quality Assurance**: Unit tests for financial calculations, balance formulas, and outbox operations.
