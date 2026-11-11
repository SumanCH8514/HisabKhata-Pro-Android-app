import React, { useState } from 'react'
import { 
  Smartphone, 
  Check, 
  IndianRupee, 
  QrCode 
} from 'lucide-react'

export const ScreenshotsGallery: React.FC = () => {
  const [selectedScreen, setSelectedScreen] = useState(0)

  const screens = [
    {
      title: "Parties & Ledger Overview",
      category: "Dashboard",
      description: "Quick aggregate summaries of total pending receivables and payables with customer search and status filters.",
      highlights: ["Instant search across all parties", "Real-time receivables & payables calculation", "Categorization by customer or vendor"],
      preview: {
        header: "HisabKhata Pro • Dashboard",
        content: (
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-2">
              <div className="bg-emerald-50 dark:bg-emerald-950/60 p-3 rounded-xl border border-emerald-200 dark:border-emerald-800">
                <span className="text-[10px] font-bold text-emerald-800 dark:text-emerald-300 uppercase">You'll Get</span>
                <div className="text-lg font-extrabold text-emerald-600 dark:text-emerald-400">₹ 48,900</div>
              </div>
              <div className="bg-rose-50 dark:bg-rose-950/60 p-3 rounded-xl border border-rose-200 dark:border-rose-800">
                <span className="text-[10px] font-bold text-rose-800 dark:text-rose-300 uppercase">You'll Give</span>
                <div className="text-lg font-extrabold text-rose-600 dark:text-rose-400">₹ 14,250</div>
              </div>
            </div>
            <div className="bg-white dark:bg-slate-900 rounded-xl p-3 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="flex justify-between items-center text-xs">
                <span className="font-bold text-slate-800 dark:text-slate-100">Sharma Electronics</span>
                <span className="font-bold text-emerald-600">₹ 12,450 (Get)</span>
              </div>
              <div className="flex justify-between items-center text-xs">
                <span className="font-bold text-slate-800 dark:text-slate-100">Patel General Store</span>
                <span className="font-bold text-rose-600">₹ 8,300 (Give)</span>
              </div>
              <div className="flex justify-between items-center text-xs">
                <span className="font-bold text-slate-800 dark:text-slate-100">Kolkata Wholesale</span>
                <span className="font-bold text-emerald-600">₹ 24,000 (Get)</span>
              </div>
            </div>
          </div>
        )
      }
    },
    {
      title: "Customer Transaction Detail",
      category: "Accounting",
      description: "Full transaction chronology for any customer with running balance, date timestamps, and direct action shortcuts.",
      highlights: ["Running balance computed on each entry", "One-tap WhatsApp payment reminder", "Instant PDF export for date range"],
      preview: {
        header: "Sharma Electronics • Statement",
        content: (
          <div className="space-y-2.5">
            <div className="bg-brand-500 text-white p-3 rounded-xl">
              <div className="text-[10px] text-blue-100 font-medium">Net Outstanding Due</div>
              <div className="text-xl font-black">₹ 12,450</div>
            </div>
            <div className="bg-white dark:bg-slate-900 rounded-xl p-2.5 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="flex justify-between text-xs">
                <div>
                  <div className="font-semibold text-slate-800 dark:text-slate-200">Goods Batch #48</div>
                  <div className="text-[10px] text-slate-400">10 Nov 2026 • 04:30 PM</div>
                </div>
                <div className="text-right">
                  <div className="font-bold text-rose-600">- ₹ 15,000</div>
                  <div className="text-[9px] text-slate-400">Bal: ₹ 12,450</div>
                </div>
              </div>
              <div className="border-t border-slate-100 dark:border-slate-800 pt-1.5 flex justify-between text-xs">
                <div>
                  <div className="font-semibold text-slate-800 dark:text-slate-200">UPI Payment Recd</div>
                  <div className="text-[10px] text-slate-400">05 Nov 2026 • 11:20 AM</div>
                </div>
                <div className="text-right">
                  <div className="font-bold text-emerald-600">+ ₹ 5,000</div>
                  <div className="text-[9px] text-slate-400">Bal: ₹ 2,550</div>
                </div>
              </div>
            </div>
          </div>
        )
      }
    },
    {
      title: "Dynamic UPI Collection QR",
      category: "Payments",
      description: "Customized on-the-fly QR generation with pre-populated amount and customer name for seamless digital collection.",
      highlights: ["Supports GPay, PhonePe, Paytm, BHIM", "Zero payment gateway processing fees", "Instant shareable QR code card"],
      preview: {
        header: "Collect Payment • UPI QR",
        content: (
          <div className="text-center bg-white dark:bg-slate-900 p-4 rounded-xl border border-slate-200 dark:border-slate-800 space-y-3">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-brand-50 dark:bg-slate-800 text-brand-600 dark:text-brand-300 rounded-full text-xs font-bold border border-brand-200 dark:border-slate-700">
              <IndianRupee className="w-3.5 h-3.5" />
              <span>Amount: ₹ 12,450.00</span>
            </div>
            <div className="w-28 h-28 mx-auto bg-slate-900 rounded-xl p-2 flex items-center justify-center text-white border border-slate-800">
              <QrCode className="w-20 h-20 text-white" />
            </div>
            <div className="text-[11px] text-slate-500 font-medium">
              Scan with any UPI App to Pay
            </div>
          </div>
        )
      }
    },
    {
      title: "Offline Outbox & Background Sync",
      category: "Synchronization",
      description: "Dedicated outbox monitor displaying pending background sync items with automatic retry when online.",
      highlights: ["Non-blocking local Room database writes", "WorkManager with exponential backoff", "Zero transaction loss guarantee"],
      preview: {
        header: "Sync Status • 100% Up to Date",
        content: (
          <div className="bg-white dark:bg-slate-900 p-3 rounded-xl border border-slate-200 dark:border-slate-800 space-y-2">
            <div className="flex items-center gap-2 p-2 bg-emerald-50 dark:bg-emerald-950/60 rounded-lg text-emerald-700 dark:text-emerald-300 text-xs font-semibold">
              <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
              <span>All 48 local records synchronized</span>
            </div>
            <div className="text-[11px] text-slate-500 space-y-1">
              <div className="flex justify-between">
                <span>Last Cloud Reconcile:</span>
                <span className="font-semibold text-slate-700 dark:text-slate-300">Just now</span>
              </div>
              <div className="flex justify-between">
                <span>Pending Outbox Queue:</span>
                <span className="font-semibold text-slate-700 dark:text-slate-300">0 items</span>
              </div>
            </div>
          </div>
        )
      }
    }
  ]

  return (
    <section id="screenshots" className="py-20 bg-white dark:bg-slate-950">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="text-center max-w-3xl mx-auto mb-14">
          <div className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold bg-brand-100 text-brand-700 dark:bg-slate-800 dark:text-brand-300 dark:border dark:border-slate-700 mb-3 shadow-xs">
            <Smartphone className="w-3.5 h-3.5" />
            <span>Interactive Gallery</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Designed for speed, clarity, and daily shop operations.
          </h2>
          <p className="mt-4 text-base text-slate-600 dark:text-slate-400">
            Engineered with Material Design 3 guidelines for an uncluttered, responsive native Android experience.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          
          <div className="lg:col-span-5 space-y-3">
            {screens.map((screen, idx) => (
              <button
                key={idx}
                onClick={() => setSelectedScreen(idx)}
                className={`w-full text-left p-4 rounded-2xl transition-all duration-200 border flex items-start gap-3.5 ${
                  selectedScreen === idx
                    ? 'bg-brand-50/70 dark:bg-slate-800/90 border-brand-500 dark:border-brand-500/60 shadow-xs'
                    : 'bg-slate-50 dark:bg-slate-900/60 border-slate-200/80 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                <div className={`p-2 rounded-xl shrink-0 ${
                  selectedScreen === idx
                    ? 'bg-brand-500 text-white'
                    : 'bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                }`}>
                  <Smartphone className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-brand-600 dark:text-brand-400">
                      {screen.category}
                    </span>
                  </div>
                  <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                    {screen.title}
                  </h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 line-clamp-2">
                    {screen.description}
                  </p>
                </div>
              </button>
            ))}
          </div>

          <div className="lg:col-span-7 flex justify-center">
            <div className="w-full max-w-md bg-slate-900 rounded-[2.5rem] p-4 shadow-2xl border-4 border-slate-800">
              <div className="bg-slate-100 dark:bg-slate-950 rounded-[2rem] p-5 overflow-hidden">
                <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-800 pb-3 mb-4">
                  <div className="flex items-center gap-2">
                    <img src="./logo.png" alt="HisabKhata" className="w-5 h-5 rounded" />
                    <span className="text-xs font-bold text-slate-800 dark:text-slate-100">
                      {screens[selectedScreen].preview.header}
                    </span>
                  </div>
                  <span className="text-[10px] bg-brand-50 text-brand-600 dark:bg-slate-800 dark:text-brand-300 px-2.5 py-0.5 rounded-full font-semibold border border-brand-200 dark:border-slate-700">
                    Live View
                  </span>
                </div>

                {screens[selectedScreen].preview.content}

                <div className="mt-5 pt-4 border-t border-slate-200 dark:border-slate-800 space-y-2">
                  <span className="text-[11px] font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider">
                    Key Highlights
                  </span>
                  {screens[selectedScreen].highlights.map((h, hIdx) => (
                    <div key={hIdx} className="flex items-center gap-2 text-xs text-slate-600 dark:text-slate-400">
                      <Check className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                      <span>{h}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

        </div>

      </div>
    </section>
  )
}
