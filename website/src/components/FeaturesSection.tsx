import React from 'react'
import { 
  Database, 
  WifiOff, 
  QrCode, 
  FileSpreadsheet, 
  MessageCircle, 
  Fingerprint,
  Layers
} from 'lucide-react'

export const FeaturesSection: React.FC = () => {
  const features = [
    {
      icon: Database,
      badge: "Accounting Engine",
      title: "Smart Credit & Debit Ledger",
      description: "Dual-column financial accounting specifically engineered for shopkeepers and small enterprises. Instantly calculates You'll Give (Udhar Diya) and You'll Get (Udhar Liya) with real-time balance aggregation.",
      points: [
        "Color-coded red & green balance identification",
        "Multi-party ledger history with chronological entries",
        "Instant transaction searching, filtering, and sorting"
      ],
      color: "text-brand-500",
      bg: "bg-brand-50 dark:bg-slate-800",
      border: "border-brand-200 dark:border-slate-800"
    },
    {
      icon: WifiOff,
      badge: "Zero Downtime",
      title: "100% Offline-First Outbox Sync",
      description: "Never lose a transaction due to bad network coverage. Every customer ledger record writes immediately to local Room SQLite. An automated WorkManager outbox syncs changes to the backend when connectivity returns.",
      points: [
        "Instant zero-latency UI response with Room SQLite",
        "Automatic outbox queue with exponential backoff",
        "Deterministic conflict resolution between client & server"
      ],
      color: "text-emerald-500",
      bg: "bg-emerald-50 dark:bg-emerald-950/60",
      border: "border-emerald-200 dark:border-emerald-900"
    },
    {
      icon: QrCode,
      badge: "Instant Collections",
      title: "Dynamic UPI QR Code Generator",
      description: "Generate customized UPI payment QR codes prefilled with the customer's pending amount. Supports all Indian UPI apps including Google Pay, PhonePe, Paytm, and BHIM UPI.",
      points: [
        "Embeds customer name, bill reference, and exact rupees",
        "Direct UPI deep-linking intent for same-device payments",
        "Save and share high-resolution QR codes via messaging"
      ],
      color: "text-indigo-500",
      bg: "bg-indigo-50 dark:bg-indigo-950/60",
      border: "border-indigo-200 dark:border-indigo-900"
    },
    {
      icon: MessageCircle,
      badge: "Smart Alerts",
      title: "WhatsApp Payment Reminders",
      description: "Send professional, polite payment reminder messages to customers in seconds. Prefilled with their exact outstanding balance, due date, and your UPI payment details.",
      points: [
        "One-tap WhatsApp & SMS customer messaging",
        "Professional templates in English and Regional languages",
        "Increases collection recovery speed by over 3x"
      ],
      color: "text-teal-500",
      bg: "bg-teal-50 dark:bg-teal-950/60",
      border: "border-teal-200 dark:border-teal-900"
    },
    {
      icon: FileSpreadsheet,
      badge: "Reports & Auditing",
      title: "GST-Ready PDF Statement Export",
      description: "Generate comprehensive customer ledger statements in PDF format entirely on-device using Android's native graphics engine, without depending on external web services.",
      points: [
        "Custom date range filtering (weekly, monthly, custom)",
        "Includes opening balance, running balances, and net dues",
        "One-click print, share via email or save to device storage"
      ],
      color: "text-amber-500",
      bg: "bg-amber-50 dark:bg-amber-950/60",
      border: "border-amber-200 dark:border-amber-900"
    },
    {
      icon: Fingerprint,
      badge: "Bank-Grade Security",
      title: "Biometric & Keystore Protection",
      description: "Your business books are safeguarded with Android Keystore hardware-backed encryption. Secure access using fingerprint, face unlock, or device PIN.",
      points: [
        "BiometricPrompt API integration for instant authentication",
        "EncryptedSharedPreferences for sensitive session tokens",
        "Zero plain-text credential storage on device or memory"
      ],
      color: "text-rose-500",
      bg: "bg-rose-50 dark:bg-rose-950/60",
      border: "border-rose-200 dark:border-rose-900"
    }
  ]

  return (
    <section id="features" className="py-20 bg-slate-50 dark:bg-slate-900/50 border-t border-slate-200/80 dark:border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold bg-brand-100 text-brand-700 dark:bg-slate-800 dark:text-brand-300 dark:border dark:border-slate-700 mb-3 shadow-xs">
            <Layers className="w-3.5 h-3.5" />
            <span>Engineered For Reliability</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Everything your business needs to manage credit effortlessly.
          </h2>
          <p className="mt-4 text-base text-slate-600 dark:text-slate-400">
            HisabKhata Pro replaces traditional paper khatas and slow cloud apps with an instant, battery-efficient native Android experience that never stops working.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature, idx) => {
            const Icon = feature.icon
            return (
              <div 
                key={idx}
                className="bg-white dark:bg-slate-900 rounded-3xl p-7 border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col justify-between group hover:border-brand-500/50"
              >
                <div>
                  <div className="flex items-center justify-between mb-5">
                    <div className={`w-12 h-12 rounded-2xl ${feature.bg} flex items-center justify-center ${feature.color}`}>
                      <Icon className="w-6 h-6" />
                    </div>
                    <span className="text-[11px] font-semibold uppercase tracking-wider px-2.5 py-1 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                      {feature.badge}
                    </span>
                  </div>

                  <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2 group-hover:text-brand-500 transition-colors">
                    {feature.title}
                  </h3>

                  <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed mb-6">
                    {feature.description}
                  </p>
                </div>

                <div className="pt-4 border-t border-slate-100 dark:border-slate-800/80 space-y-2">
                  {feature.points.map((pt, pIdx) => (
                    <div key={pIdx} className="flex items-start gap-2 text-xs text-slate-700 dark:text-slate-300">
                      <span className="w-1.5 h-1.5 rounded-full bg-brand-500 mt-1.5 shrink-0"></span>
                      <span>{pt}</span>
                    </div>
                  ))}
                </div>
              </div>
            )
          })}
        </div>

      </div>
    </section>
  )
}
