# HisabKhata Pro — Existing Backend Architecture

## 1. Cloudflare Workers Edge API

- **Base URL**: `https://backend.hisabkhata.sumanonline.com`
- **Framework**: Hono running on Cloudflare Workers Runtime V8 isolates.
- **CDN Base URL**: `https://cdn.backend.hisabkhata.sumanonline.com`

### Endpoints
1. `GET /`: Health status and service diagnostics.
2. `GET /api/health`: Service check and R2 bucket connectivity status.
3. `POST /api/upload`:
   - Accepts `multipart/form-data` or JSON with base64 payload.
   - Target folders: `cust_profile_pictures`, `payment_proof`, `transaction_attachments`.
   - Returns public CDN URL, file key, and metadata.
4. `POST /api/delete`:
   - Deletes media key from Cloudflare R2 bucket.
5. `POST /api/send-email`:
   - Dispatches transactional emails via configured SMTP server using templates: `WELCOME`, `TRANSACTION`, `CUSTOMER_ADDED`, `PAYMENT_REMINDER`, `OTP`.
6. `GET /api/email-status`:
   - Returns SMTP connection status and sender configuration.

## 2. Firebase Realtime Database Structure

- **Database URL**: `https://hisabkhata-sumanonline-default-rtdb.firebaseio.com`
- **Security**: Defined in `database.rules.json`

### Data Paths & Schema
```
root
├── users/
│   └── {userId}/
│       ├── name: string
│       ├── email: string
│       ├── phone: string
│       ├── businessName: string
│       ├── businessCategory: string
│       ├── businessType: string
│       ├── businessAddress: string
│       ├── upiId: string
│       ├── pan: string
│       ├── gst: string
│       ├── bankName: string
│       ├── accountNumber: string
│       ├── ifscCode: string
│       ├── photoURL: string
│       ├── role: "user" | "admin"
│       ├── reminderSettings: object
│       ├── reminderLogs/
│       └── preferences/
│
├── customers/
│   └── {customerId}/
│       ├── userId: string (indexed)
│       ├── name: string
│       ├── phone: string
│       ├── email: string
│       ├── balance: number (Negative = You'll Get, Positive = You'll Give, 0 = Settled)
│       ├── type: "Customer" | "Supplier"
│       ├── photoURL: string
│       ├── gst: string
│       ├── dueDate: string (YYYY-MM-DD)
│       ├── createdAt: number (epoch ms)
│       └── updatedAt: number (epoch ms)
│
├── transactions/
│   └── {transactionId}/
│       ├── userId: string (indexed)
│       ├── customerId: string (indexed)
│       ├── amount: number (Negative for GAVE, Positive for GOT)
│       ├── type: "GAVE" | "GOT"
│       ├── description: string
│       ├── date: string (YYYY-MM-DD)
│       ├── timestamp: number (epoch ms)
│       ├── balance: number (running balance after transaction)
│       ├── attachment: string (URL)
│       └── attachments: string[] (URLs)
│
└── pending_payments/
    └── {paymentId}/
        ├── merchantId: string
        ├── customerId: string
        ├── customerName: string
        ├── amount: number
        ├── transactionId: string (UTR)
        ├── screenshot: string (URL)
        ├── status: "pending" | "approved" | "rejected"
        └── timestamp: number
```
