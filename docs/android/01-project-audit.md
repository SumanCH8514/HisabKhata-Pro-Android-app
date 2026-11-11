# HisabKhata Pro — Local Project Audit

## 1. Executive Summary

- **Product Name**: HisabKhata Pro (Digital Ledger & Customer Debt Recovery Platform)
- **Live Production URL**: `https://hisabkhata.sumanonline.com`
- **Source Code Repository**: `E:\Projects\HisabKhata`
- **Target Android Workspace**: `E:\Projects\Android Application Dev\HisabKhata-Pro Application`
- **Core Business Model**: Bahi-Khata ledger replacement for Indian MSMEs, retail merchants, and wholesalers. Tracks credit given (Gave / You'll Get), payment received (Got / You'll Give), customer dues, UPI QR payments, and statement generation.

## 2. Technology Stack Breakdown

| Tier | Existing Implementation | Details |
|---|---|---|
| **Frontend Web** | React 19, Vite 8, Tailwind CSS 3.4 | SPA with responsive drawers, glassmorphism UI, client-side PDF/Excel export |
| **Backend API** | Cloudflare Workers (Hono framework) | Edge REST API running on `https://backend.hisabkhata.sumanonline.com` |
| **Object Storage & CDN** | Cloudflare R2 (`hisabkhata` bucket) | Edge CDN on `https://cdn.backend.hisabkhata.sumanonline.com` |
| **Database** | Firebase Realtime Database (RTDB) | Project ID: `hisabkhata-sumanonline`, indexed on `userId`, `customerId` |
| **Authentication** | Firebase Authentication | Google OAuth 2.0, Email & Password, Email OTP, TOTP 2FA |
| **Email Delivery** | Direct SMTP & Cloudflare Worker Email Gateway | Transaction alerts, customer additions, daily/weekly reminders |

## 3. Configuration & Security Findings

- Strict separation of credentials: Firebase client tokens connect to production RTDB rules.
- Database access is partitioned per-merchant using Firebase Auth `auth.uid`.
- Sensitive operations (SMTP, storage deletions, bulk cron triggers) are encapsulated inside the Cloudflare Worker backend.
- Customer public share links (`/customer/share/:id`) allow read-only access to customer's own ledger without requiring merchant authentication.
- Zero breaking changes to existing web schemas or backend functions are permitted.
