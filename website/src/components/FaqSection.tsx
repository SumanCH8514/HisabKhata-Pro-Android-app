import React, { useState } from 'react'
import { HelpCircle, ChevronDown } from 'lucide-react'

export const FaqSection: React.FC = () => {
  const [openIdx, setOpenIdx] = useState<number | null>(0)

  const faqs = [
    {
      q: "Does HisabKhata Pro work without an active internet connection?",
      a: "Yes, 100%. The Android application is built with an offline-first architecture. All customer records, credit/debit transactions, and balance calculations execute instantly inside your local Room SQLite database. When internet connectivity is restored, our background sync engine automatically reconciles changes with the cloud."
    },
    {
      q: "How do I install the application from the downloaded APK file?",
      a: "Download the APK using the Download button above. When downloaded, open the file from your Notifications or Files app. If your Android system shows 'Install unknown apps', tap Settings and enable 'Allow from this source'. Then tap Install. The app is completely safe, verified clean, and signed."
    },
    {
      q: "Is my business ledger and customer information secure?",
      a: "Yes. Sensitive session credentials are encrypted with the hardware-backed Android Keystore using AES-256. You can also configure biometric authentication (Fingerprint / Face Unlock) to require authentication whenever the application is opened."
    },
    {
      q: "How does the Android app sync with the HisabKhata Web Platform?",
      a: "The Android app connects directly to the HisabKhata Pro backend. Every party, transaction, and balance you manage on your Android phone seamlessly syncs with your desktop web portal at hisabkhata.sumanonline.com."
    },
    {
      q: "Can customers pay me directly via UPI using the generated QR code?",
      a: "Yes. You can open any customer ledger entry and tap 'Collect UPI'. HisabKhata Pro generates a dynamic UPI QR code prefilled with the customer's exact pending rupee balance and your business UPI VPA. Customers can scan it with Google Pay, PhonePe, Paytm, or BHIM."
    },
    {
      q: "How will I receive updates when newer versions are released?",
      a: "Updates are published directly to our official GitHub Releases page. HisabKhata Pro includes an in-app version check that alerts you whenever a new release is available with a one-tap update link."
    }
  ]

  const toggleAccordion = (idx: number) => {
    setOpenIdx(openIdx === idx ? null : idx)
  }

  return (
    <section id="faq" className="py-20 bg-white dark:bg-slate-950">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="text-center max-w-3xl mx-auto mb-14">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-brand-100 text-brand-700 dark:bg-brand-950 dark:text-brand-300 mb-3">
            <HelpCircle className="w-3.5 h-3.5" />
            <span>Got Questions?</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Frequently Asked Questions
          </h2>
          <p className="mt-4 text-base text-slate-600 dark:text-slate-400">
            Everything you need to know about HisabKhata Pro for Android.
          </p>
        </div>

        <div className="max-w-3xl mx-auto space-y-3">
          {faqs.map((faq, idx) => {
            const isOpen = openIdx === idx
            return (
              <div
                key={idx}
                className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/60 overflow-hidden transition-colors"
              >
                <button
                  onClick={() => toggleAccordion(idx)}
                  className="w-full p-5 text-left flex items-center justify-between gap-4 font-bold text-slate-900 dark:text-white text-sm sm:text-base hover:text-brand-600 dark:hover:text-brand-400 transition-colors"
                >
                  <span>{faq.q}</span>
                  <ChevronDown
                    className={`w-5 h-5 text-slate-500 shrink-0 transition-transform duration-200 ${
                      isOpen ? 'rotate-180 text-brand-500' : ''
                    }`}
                  />
                </button>
                {isOpen && (
                  <div className="px-5 pb-5 text-sm text-slate-600 dark:text-slate-400 leading-relaxed border-t border-slate-200/60 dark:border-slate-800/60 pt-3">
                    {faq.a}
                  </div>
                )}
              </div>
            )
          })}
        </div>

      </div>
    </section>
  )
}
