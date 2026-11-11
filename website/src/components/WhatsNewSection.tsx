import React from 'react'
import { Sparkles, CheckCircle2 } from 'lucide-react'
import { APP_CONFIG } from '../config/downloadConfig'

export const WhatsNewSection: React.FC = () => {
  const changelog = [
    {
      title: "Native Jetpack Compose Architecture",
      desc: "Complete native Android rebuild using Jetpack Compose and Material 3 design tokens, providing instantaneous 60fps animations and fluid interactions."
    },
    {
      title: "100% Offline-First Outbox Sync",
      desc: "Integrated local Room SQLite database with automated WorkManager background queue. Create customers and record transactions with zero internet connection."
    },
    {
      title: "Dynamic UPI Payment QR Codes",
      desc: "Generate dynamic UPI QR codes containing the customer's exact pending amount and your business UPI VPA. Supports GPay, PhonePe, Paytm, and BHIM."
    },
    {
      title: "Automated WhatsApp & SMS Reminders",
      desc: "Pre-composed, professional payment reminder messages prefilled with net balance and payment links, ready to send with one tap."
    },
    {
      title: "Hardware-Backed Android Keystore Security",
      desc: "Integrated BiometricPrompt for fingerprint and facial authentication, safeguarding business ledger records from unauthorized local access."
    }
  ]

  return (
    <section id="whats-new" className="py-20 bg-white dark:bg-slate-950">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="max-w-3xl mx-auto">
          <div className="text-center mb-12">
            <div className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold bg-brand-100 text-brand-700 dark:bg-slate-800 dark:text-brand-300 dark:border dark:border-slate-700 mb-3 shadow-xs">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Release Notes</span>
            </div>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
              What's New in Version {APP_CONFIG.version}
            </h2>
            <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
              Initial production release of the native HisabKhata Pro Android mobile client.
            </p>
          </div>

          <div className="bg-slate-50 dark:bg-slate-900/60 rounded-3xl p-6 sm:p-8 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
            <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-800 pb-4">
              <div>
                <span className="text-base font-bold text-slate-900 dark:text-white">
                  v{APP_CONFIG.version} (Build {APP_CONFIG.buildNumber})
                </span>
                <span className="text-xs text-slate-500 ml-2">Stable Milestone</span>
              </div>
              <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-brand-50 text-brand-600 dark:bg-slate-800 dark:text-brand-300 border border-brand-200 dark:border-slate-700">
                {APP_CONFIG.releaseDate}
              </span>
            </div>

            <div className="space-y-4">
              {changelog.map((item, idx) => (
                <div key={idx} className="flex items-start gap-3.5">
                  <div className="w-5 h-5 rounded-full bg-emerald-100 dark:bg-emerald-950/80 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                      {item.title}
                    </h4>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mt-1 leading-relaxed">
                      {item.desc}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            <div className="pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center text-xs text-slate-500">
              <span>Verified commit: Initial Production Release</span>
              <a
                href={APP_CONFIG.releasesUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-brand-600 dark:text-brand-400 font-semibold hover:underline"
              >
                View on GitHub Releases
              </a>
            </div>
          </div>
        </div>

      </div>
    </section>
  )
}
