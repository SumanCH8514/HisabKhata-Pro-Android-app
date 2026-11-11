import React, { useState } from 'react'
import { 
  Download, 
  ShieldCheck, 
  Copy, 
  Check, 
  ExternalLink, 
  FileCheck2 
} from 'lucide-react'
import { APP_CONFIG } from '../config/downloadConfig'

export const DownloadSection: React.FC = () => {
  const [copiedHash, setCopiedHash] = useState(false)

  const copyToClipboard = () => {
    navigator.clipboard.writeText(APP_CONFIG.sha256)
    setCopiedHash(true)
    setTimeout(() => setCopiedHash(false), 2500)
  }

  return (
    <section id="download" className="py-20 bg-slate-50 dark:bg-slate-900/50 border-t border-slate-200/80 dark:border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 mb-3">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Official Android Release</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Download HisabKhata Pro for Android
          </h2>
          <p className="mt-4 text-base text-slate-600 dark:text-slate-400">
            Get the full production build directly on your phone. No tracking, zero bloat, and fully functional without an internet connection.
          </p>
        </div>

        <div className="max-w-4xl mx-auto">
          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-xl overflow-hidden">
            
            <div className="bg-gradient-to-r from-brand-600 to-blue-700 p-6 sm:p-8 text-white flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
              <div className="flex items-center gap-4">
                <img 
                  src="./logo.png" 
                  alt="HisabKhata Pro" 
                  className="w-16 h-16 rounded-2xl shadow-md border-2 border-white/20 bg-white" 
                />
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-2xl font-black">HisabKhata Pro</h3>
                    <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-white/20 text-white">
                      v{APP_CONFIG.version}
                    </span>
                  </div>
                  <p className="text-sm text-blue-100 mt-1">
                    Native Android Accounting & Customer Ledger
                  </p>
                  <p className="text-xs text-blue-200 mt-0.5 font-mono">
                    {APP_CONFIG.packageName}
                  </p>
                </div>
              </div>

              <div className="w-full sm:w-auto">
                <a
                  href={APP_CONFIG.downloadUrl}
                  download="HisabKhata-Pro.apk"
                  className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 px-6 py-3.5 text-base font-bold text-brand-600 bg-white hover:bg-slate-50 active:scale-95 rounded-2xl shadow-lg transition-all"
                >
                  <Download className="w-5 h-5" />
                  <span>Download APK ({APP_CONFIG.apkSize})</span>
                </a>
              </div>
            </div>

            <div className="p-6 sm:p-8 space-y-6">
              
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <div className="text-[11px] text-slate-500 font-medium">Version</div>
                  <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                    {APP_CONFIG.version} (Build {APP_CONFIG.buildNumber})
                  </div>
                </div>

                <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <div className="text-[11px] text-slate-500 font-medium">Compatibility</div>
                  <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                    {APP_CONFIG.minAndroid}
                  </div>
                </div>

                <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <div className="text-[11px] text-slate-500 font-medium">Architecture</div>
                  <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                    Universal (arm64, x86)
                  </div>
                </div>

                <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <div className="text-[11px] text-slate-500 font-medium">Release Date</div>
                  <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                    {APP_CONFIG.releaseDate}
                  </div>
                </div>
              </div>

              <div className="p-4 rounded-2xl bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700/80">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                    <FileCheck2 className="w-4 h-4 text-emerald-500" />
                    <span>SHA-256 Checksum Verification</span>
                  </span>
                  <button
                    onClick={copyToClipboard}
                    className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-lg bg-white dark:bg-slate-700 hover:bg-slate-50 dark:hover:bg-slate-600 text-slate-700 dark:text-slate-200 border border-slate-300 dark:border-slate-600 transition-colors"
                  >
                    {copiedHash ? (
                      <>
                        <Check className="w-3.5 h-3.5 text-emerald-500" />
                        <span className="text-emerald-600 dark:text-emerald-400">Copied</span>
                      </>
                    ) : (
                      <>
                        <Copy className="w-3.5 h-3.5" />
                        <span>Copy Hash</span>
                      </>
                    )}
                  </button>
                </div>
                <div className="font-mono text-xs text-slate-600 dark:text-slate-400 break-all select-all bg-white/70 dark:bg-slate-900/70 p-2.5 rounded-xl border border-slate-200/70 dark:border-slate-800">
                  {APP_CONFIG.sha256}
                </div>
              </div>

              <div>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white mb-3">
                  How to Install on Android
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
                  <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200/70 dark:border-slate-800">
                    <div className="font-bold text-brand-600 dark:text-brand-400 mb-1">
                      1. Download APK
                    </div>
                    <p className="text-slate-600 dark:text-slate-400">
                      Tap the Download APK button above to save the package to your device.
                    </p>
                  </div>
                  <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200/70 dark:border-slate-800">
                    <div className="font-bold text-brand-600 dark:text-brand-400 mb-1">
                      2. Allow Installation
                    </div>
                    <p className="text-slate-600 dark:text-slate-400">
                      If prompted, tap Settings and enable "Install from this source" in Chrome or Files.
                    </p>
                  </div>
                  <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200/70 dark:border-slate-800">
                    <div className="font-bold text-brand-600 dark:text-brand-400 mb-1">
                      3. Open & Manage
                    </div>
                    <p className="text-slate-600 dark:text-slate-400">
                      Launch HisabKhata Pro, sign in or work offline immediately with full features.
                    </p>
                  </div>
                </div>
              </div>

              <div className="pt-2 flex flex-wrap items-center justify-between gap-4 border-t border-slate-200 dark:border-slate-800 text-xs">
                <a
                  href={APP_CONFIG.releasesUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 text-brand-600 dark:text-brand-400 hover:underline font-semibold"
                >
                  <span>View All GitHub Releases & Tags</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>

                <div className="flex items-center gap-2 text-slate-500">
                  <span className="w-2 h-2 rounded-full bg-amber-400"></span>
                  <span>Google Play Store listing coming soon</span>
                </div>
              </div>

            </div>

          </div>
        </div>

      </div>
    </section>
  )
}
