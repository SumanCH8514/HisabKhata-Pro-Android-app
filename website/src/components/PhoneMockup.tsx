import React, { useState } from 'react'
import { 
  Wifi, 
  BatteryMedium, 
  Signal, 
  ArrowUpRight, 
  ArrowDownLeft, 
  Search, 
  Plus, 
  QrCode, 
  FileText, 
  MessageSquare,
  Clock,
  IndianRupee,
  Share2
} from 'lucide-react'

export const PhoneMockup: React.FC = () => {
  const [activeScreen, setActiveScreen] = useState<'parties' | 'ledger' | 'upi'>('parties')

  return (
    <div className="flex flex-col items-center">
      <div className="inline-flex p-1 bg-slate-200/80 dark:bg-slate-800/80 backdrop-blur rounded-2xl mb-4 border border-slate-300/60 dark:border-slate-700/60 shadow-inner">
        <button
          onClick={() => setActiveScreen('parties')}
          className={`px-3.5 py-1.5 text-xs font-semibold rounded-xl transition-all ${
            activeScreen === 'parties'
              ? 'bg-white dark:bg-slate-700 text-brand-600 dark:text-brand-300 shadow-sm'
              : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          Customer Ledger
        </button>
        <button
          onClick={() => setActiveScreen('ledger')}
          className={`px-3.5 py-1.5 text-xs font-semibold rounded-xl transition-all ${
            activeScreen === 'ledger'
              ? 'bg-white dark:bg-slate-700 text-brand-600 dark:text-brand-300 shadow-sm'
              : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          Statement History
        </button>
        <button
          onClick={() => setActiveScreen('upi')}
          className={`px-3.5 py-1.5 text-xs font-semibold rounded-xl transition-all ${
            activeScreen === 'upi'
              ? 'bg-white dark:bg-slate-700 text-brand-600 dark:text-brand-300 shadow-sm'
              : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          Dynamic UPI QR
        </button>
      </div>

      <div className="relative mx-auto border-gray-800 dark:border-gray-800 bg-gray-900 border-[10px] rounded-[2.8rem] h-[640px] w-[310px] sm:w-[330px] shadow-2xl ring-1 ring-slate-900/20">
        <div className="w-[110px] h-[16px] bg-gray-900 top-0 left-1/2 -translate-x-1/2 absolute rounded-b-xl z-20 flex items-center justify-center">
          <div className="w-3.5 h-3.5 bg-black rounded-full border border-gray-800 flex items-center justify-center">
            <div className="w-1.5 h-1.5 bg-blue-950 rounded-full"></div>
          </div>
        </div>

        <div className="h-[32px] w-[3px] bg-gray-800 absolute -start-[13px] top-[100px] rounded-s-lg"></div>
        <div className="h-[50px] w-[3px] bg-gray-800 absolute -start-[13px] top-[146px] rounded-s-lg"></div>
        <div className="h-[50px] w-[3px] bg-gray-800 absolute -start-[13px] top-[204px] rounded-s-lg"></div>
        <div className="h-[60px] w-[3px] bg-gray-800 absolute -end-[13px] top-[135px] rounded-e-lg"></div>

        <div className="rounded-[2.2rem] overflow-hidden w-full h-full bg-slate-50 dark:bg-slate-950 flex flex-col select-none relative font-sans text-slate-800 dark:text-slate-100">
          
          <div className="h-7 px-5 pt-1.5 flex items-center justify-between text-[11px] font-semibold text-slate-700 dark:text-slate-300 bg-white dark:bg-slate-900 shrink-0">
            <span>09:41</span>
            <div className="flex items-center gap-1.5">
              <Signal className="w-3 h-3" />
              <Wifi className="w-3 h-3" />
              <BatteryMedium className="w-3.5 h-3.5 text-credit" />
            </div>
          </div>

          {activeScreen === 'parties' && (
            <div className="flex-1 flex flex-col overflow-hidden bg-slate-100 dark:bg-slate-950">
              <div className="bg-brand-500 text-white px-4 pt-2.5 pb-4 shadow">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <img src="./logo.png" alt="App" className="w-6 h-6 rounded-md shadow-sm" />
                    <span className="font-bold text-base tracking-tight">HisabKhata Pro</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="inline-flex items-center gap-1 bg-brand-600/70 text-[10px] font-medium px-2 py-0.5 rounded-full border border-brand-400/30">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                      Synced
                    </span>
                    <button className="p-1 rounded-md bg-brand-600/60 hover:bg-brand-600 text-white text-xs">
                      <Search className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-2 mt-1">
                  <div className="bg-white/10 backdrop-blur rounded-xl p-2.5 border border-white/15">
                    <div className="text-[10px] text-blue-100 uppercase tracking-wider font-semibold flex items-center gap-1">
                      <ArrowDownLeft className="w-3 h-3 text-emerald-300" />
                      <span>You'll Get</span>
                    </div>
                    <div className="text-sm font-bold text-emerald-300 mt-0.5">
                      ₹ 48,900
                    </div>
                  </div>
                  <div className="bg-white/10 backdrop-blur rounded-xl p-2.5 border border-white/15">
                    <div className="text-[10px] text-blue-100 uppercase tracking-wider font-semibold flex items-center gap-1">
                      <ArrowUpRight className="w-3 h-3 text-rose-300" />
                      <span>You'll Give</span>
                    </div>
                    <div className="text-sm font-bold text-rose-300 mt-0.5">
                      ₹ 14,250
                    </div>
                  </div>
                </div>
              </div>

              <div className="px-3 pt-2.5 pb-1 flex items-center justify-between text-[11px] font-semibold text-slate-500 dark:text-slate-400">
                <span>RECENT PARTIES (14)</span>
                <span className="text-brand-500 dark:text-brand-400">Sort by Balance</span>
              </div>

              <div className="flex-1 overflow-y-auto px-3 space-y-1.5 pb-12">
                {[
                  { name: "Sharma Electronics", time: "10 mins ago", amount: "₹ 12,450", type: "get", initials: "SE", color: "bg-blue-600" },
                  { name: "Patel General Store", time: "Today, 02:15 PM", amount: "₹ 8,300", type: "give", initials: "PG", color: "bg-emerald-600" },
                  { name: "Kolkata Wholesale Traders", time: "Yesterday", amount: "₹ 24,000", type: "get", initials: "KW", color: "bg-purple-600" },
                  { name: "Das Hardware & Paints", time: "09 Nov 2026", amount: "₹ 5,950", type: "give", initials: "DH", color: "bg-amber-600" },
                  { name: "Roy Stationery Supply", time: "07 Nov 2026", amount: "₹ 12,450", type: "get", initials: "RS", color: "bg-teal-600" },
                ].map((item, idx) => (
                  <div 
                    key={idx}
                    className="bg-white dark:bg-slate-900 rounded-xl p-2.5 border border-slate-200/80 dark:border-slate-800 shadow-xs flex items-center justify-between hover:border-brand-500 transition-colors"
                  >
                    <div className="flex items-center gap-2.5">
                      <div className={`w-8 h-8 rounded-lg ${item.color} text-white font-bold text-xs flex items-center justify-center shrink-0 shadow-xs`}>
                        {item.initials}
                      </div>
                      <div>
                        <div className="text-xs font-semibold leading-tight text-slate-800 dark:text-slate-100">
                          {item.name}
                        </div>
                        <div className="text-[10px] text-slate-400 dark:text-slate-500 flex items-center gap-1 mt-0.5">
                          <Clock className="w-2.5 h-2.5" />
                          <span>{item.time}</span>
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className={`text-xs font-bold ${
                        item.type === 'get' ? 'text-credit dark:text-emerald-400' : 'text-debit dark:text-rose-400'
                      }`}>
                        {item.amount}
                      </div>
                      <div className="text-[9px] uppercase tracking-wider font-semibold text-slate-400">
                        {item.type === 'get' ? "You'll Get" : "You'll Give"}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="absolute bottom-4 right-4 z-10">
                <button className="bg-brand-500 hover:bg-brand-600 text-white rounded-2xl p-3 shadow-lg flex items-center gap-1.5 text-xs font-semibold">
                  <Plus className="w-4 h-4" />
                  <span>Add Party</span>
                </button>
              </div>
            </div>
          )}

          {activeScreen === 'ledger' && (
            <div className="flex-1 flex flex-col bg-slate-100 dark:bg-slate-950 overflow-hidden">
              <div className="bg-white dark:bg-slate-900 px-3.5 py-2.5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-lg bg-blue-600 text-white text-xs font-bold flex items-center justify-center">
                    SE
                  </div>
                  <div>
                    <div className="text-xs font-bold text-slate-900 dark:text-white leading-tight">
                      Sharma Electronics
                    </div>
                    <div className="text-[10px] text-slate-500">+91 98765 43210</div>
                  </div>
                </div>
                <div className="flex items-center gap-1">
                  <button className="p-1.5 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                    <FileText className="w-3.5 h-3.5" />
                  </button>
                  <button className="p-1.5 rounded-lg bg-emerald-50 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400">
                    <MessageSquare className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <div className="p-3 bg-brand-50 dark:bg-brand-950/40 border-b border-brand-100 dark:border-brand-900/40 flex items-center justify-between">
                <div>
                  <span className="text-[10px] text-brand-700 dark:text-brand-300 font-medium">Net Pending Balance</span>
                  <div className="text-base font-bold text-brand-600 dark:text-brand-400">
                    ₹ 12,450 <span className="text-[10px] font-normal text-slate-500">(You'll Get)</span>
                  </div>
                </div>
                <button className="px-2.5 py-1 bg-brand-500 text-white rounded-lg text-[10px] font-semibold flex items-center gap-1">
                  <QrCode className="w-3 h-3" />
                  <span>Collect UPI</span>
                </button>
              </div>

              <div className="flex-1 overflow-y-auto p-2.5 space-y-2">
                {[
                  { desc: "Inventory Delivery Batch #48", date: "10 Nov 2026", amount: "₹ 15,000", type: "given", balance: "₹ 12,450" },
                  { desc: "UPI Transfer Received", date: "05 Nov 2026", amount: "₹ 5,000", type: "received", balance: "₹ 2,550" },
                  { desc: "AC Compressor Unit x2", date: "28 Oct 2026", amount: "₹ 7,550", type: "given", balance: "₹ 7,550" },
                ].map((entry, idx) => (
                  <div key={idx} className="bg-white dark:bg-slate-900 p-2.5 rounded-xl border border-slate-200/80 dark:border-slate-800 text-[11px]">
                    <div className="flex justify-between items-start mb-1">
                      <span className="font-semibold text-slate-800 dark:text-slate-200">{entry.desc}</span>
                      <span className={`font-bold ${entry.type === 'received' ? 'text-credit' : 'text-debit'}`}>
                        {entry.type === 'received' ? `+ ${entry.amount}` : `- ${entry.amount}`}
                      </span>
                    </div>
                    <div className="flex justify-between items-center text-[10px] text-slate-400">
                      <span>{entry.date}</span>
                      <span>Balance: {entry.balance}</span>
                    </div>
                  </div>
                ))}
              </div>

              <div className="p-2.5 bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 grid grid-cols-2 gap-2">
                <button className="py-2 bg-rose-600 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1 shadow-xs">
                  <ArrowUpRight className="w-3.5 h-3.5" />
                  <span>You Gave ₹</span>
                </button>
                <button className="py-2 bg-emerald-600 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1 shadow-xs">
                  <ArrowDownLeft className="w-3.5 h-3.5" />
                  <span>You Got ₹</span>
                </button>
              </div>
            </div>
          )}

          {activeScreen === 'upi' && (
            <div className="flex-1 flex flex-col items-center justify-center p-4 bg-white dark:bg-slate-900 text-center">
              <div className="w-10 h-10 rounded-2xl bg-brand-50 dark:bg-brand-950 flex items-center justify-center text-brand-500 mb-2">
                <IndianRupee className="w-5 h-5" />
              </div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-white">Sharma Electronics</h4>
              <p className="text-[10px] text-slate-500 mb-2">Scan & Pay using any UPI Application</p>

              <div className="p-3 bg-white rounded-2xl border-2 border-slate-200 shadow-md my-1">
                <div className="w-36 h-36 bg-slate-950 rounded-lg p-2 flex flex-col items-center justify-center relative overflow-hidden">
                  <div className="grid grid-cols-6 gap-1 w-full h-full opacity-90">
                    {Array.from({ length: 36 }).map((_, i) => (
                      <div 
                        key={i} 
                        className={`rounded-xs ${
                          (i % 2 === 0 || i % 5 === 0) ? 'bg-white' : 'bg-transparent'
                        }`} 
                      />
                    ))}
                  </div>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="bg-white p-1 rounded-md shadow">
                      <img src="./logo.png" alt="App" className="w-5 h-5" />
                    </div>
                  </div>
                </div>
              </div>

              <div className="text-sm font-extrabold text-slate-900 dark:text-white mt-1">
                ₹ 12,450.00
              </div>
              <span className="text-[10px] text-slate-500">UPI ID: business@sumanonline</span>

              <div className="flex items-center gap-2 mt-4 w-full">
                <button className="flex-1 py-1.5 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-xl text-[10px] font-semibold flex items-center justify-center gap-1">
                  <Share2 className="w-3 h-3" />
                  <span>Share QR</span>
                </button>
                <button className="flex-1 py-1.5 bg-emerald-600 text-white rounded-xl text-[10px] font-semibold flex items-center justify-center gap-1 shadow-xs">
                  <MessageSquare className="w-3 h-3" />
                  <span>WhatsApp</span>
                </button>
              </div>
            </div>
          )}

          <div className="h-5 bg-white dark:bg-slate-900 shrink-0 flex items-center justify-center pb-1">
            <div className="w-24 h-1 bg-slate-400 dark:bg-slate-600 rounded-full"></div>
          </div>
        </div>
      </div>
    </div>
  )
}
