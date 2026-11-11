# HisabKhata Pro — Feature Inventory & Traceability Matrix

## 1. Feature Map

| Feature ID | Feature Name | Web Route / Drawer | Source of Truth / DB Node | Backend / Services | Android Implementation |
|---|---|---|---|---|---|
| **AUTH-01** | Merchant Sign In (Email/Pass) | `/login` | `users/$userId` | Firebase Auth | Jetpack Compose Login Screen, Keystore Session |
| **AUTH-02** | Google Sign In | `/login`, `/signup` | `users/$userId` | Firebase Auth (Google Provider) | Google Credential Manager API |
| **AUTH-03** | Merchant Registration | `/signup` | `users/$userId` | Firebase Auth + RTDB record creation | Register Screen with Validation & Welcome Mail |
| **AUTH-04** | Password Reset | `/forgot-password` | Firebase Auth | `sendPasswordResetEmail` | Reset Password Dialog & Flow |
| **AUTH-05** | Email OTP & TOTP 2FA | `/login` | `users/$userId/loginOtp`, `users/$userId/twoFactorSecret` | Backend Email API / TOTP engine | 6-Digit Verification Screen |
| **CUST-01** | Customer List | `/customers` | `customers` (indexed on `userId`) | Realtime listener query | LazyColumn with Search, Sort, Filter, Summary Cards |
| **CUST-02** | Add / Edit Customer | `CustomerDrawer.jsx`, `PartyProfileDrawer.jsx` | `customers/$customerId` | RTDB `push` / `update` | Add Customer Bottom Sheet / Modal with Photo Picker |
| **CUST-03** | Customer Ledger & History | `CustomerLedgerDetail.jsx` | `transactions` (indexed on `customerId`) | RTDB query sorted by timestamp | Grouped Date-wise Ledger List, Running Balances |
| **CUST-04** | Customer Due Date Tracking | `CustomerLedgerDetail.jsx` | `customers/$customerId/dueDate` | RTDB update | 7/14/30 day quick chips, custom date picker, due alerts |
| **CUST-05** | Delete Customer | `PartyProfileDrawer.jsx` | `customers/$customerId`, `transactions` | RTDB cascaded delete + R2 cleanup | Confirmation dialog, local cascade + cloud sync |
| **TX-01** | Record Credit (Gave / Red) | `TransactionDrawer.jsx` | `transactions`, updates customer balance | RTDB atomic multi-path update | Floating Action Button (Red "You Gave ₹") |
| **TX-02** | Record Payment (Got / Green) | `TransactionDrawer.jsx` | `transactions`, updates customer balance | RTDB atomic multi-path update | Floating Action Button (Green "You Got ₹") |
| **TX-03** | Edit / Delete Transaction | `EntryDetailsDrawer.jsx` | `transactions/$txId`, customer balance adjustment | RTDB update / delete | Entry detail sheet, recalculation of balances |
| **TX-04** | Bill / Attachment Upload | `TransactionDrawer.jsx` | Cloudflare R2 (`transaction_attachments`) | Edge Worker `/api/upload` | Camera / Gallery capture, compression, outbox upload |
| **PAY-01** | Instant UPI QR Generator | `Payments.jsx`, `CustomerShareableView.jsx` | Merchant profile `upiId`, customer balance | `upi://pay` standard intent | Native Android UPI Apps launch & QR rendering |
| **PAY-02** | Payment Proof Verification | `PaymentVerification.jsx` | `pending_payments/$paymentId` | RTDB query & approve/reject | Pending payments review card with 1-tap ledger approval |
| **REM-01** | Payment Reminders & WhatsApp | `PaymentReminders.jsx`, `Customers.jsx` | `users/$userId/reminderLogs` | WhatsApp URI intent, Worker `/api/send-email` | Direct WhatsApp Intent launcher & Email reminder dispatch |
| **REP-01** | Ledger PDF Statement | `Reports.jsx`, `CustomerReport.jsx` | Aggregated transactions | Android PdfDocument generator | Native PDF generator, print service, file sharing |
| **REP-02** | Excel / CSV Statement Export | `Reports.jsx`, `Settings.jsx` | Aggregated transactions | Android Document Export / CSV writer | Standard Storage Access Framework export |
| **SET-01** | Merchant Profile & Shop Details | `Profile.jsx` | `users/$userId` | RTDB update + profile picture in R2 | Edit profile form, logo upload, bank & UPI fields |
| **SET-02** | Security & App Preferences | `Settings.jsx` | `users/$userId/preferences` | RTDB update | Biometric unlock toggle, notification preferences |

## 2. Offline-First Requirements Traceability

- All customer and transaction entities must persist locally in SQLite via Room.
- Adding a customer or transaction offline must immediately update local Room balances and display with `PENDING_SYNC` state.
- Background sync via WorkManager must guarantee FIFO ordered reconciliation to Firebase Realtime Database.
- Conflict resolution: Client operation UUIDs prevent duplicate transaction injection on network retries.
