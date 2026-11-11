import React from 'react'
import { Download, Shield, WifiOff, FileDown, ArrowRight, Github, Cpu } from 'lucide-react'
import { APP_CONFIG } from '../config/downloadConfig'
import { PhoneMockup } from './PhoneMockup'

export const HeroSection: React.FC = () => {
  return (
    <section className="relative overflow-hidden pt-8 pb-16 md:pt-14 md:pb-24">
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-brand-500/10 dark:bg-brand-500/15 blur-[120px] rounded-full pointer-events-none -z-10" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">
          
          <div className="lg:col-span-7 flex flex-col items-center lg:items-start text-center lg:text-left space-y-6">
            
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-semibold bg-brand-50 text-brand-700 dark:bg-brand-950/60 dark:text-brand-300 border border-brand-200/80 dark:border-brand-800/60 shadow-xs">
              <span className="flex h-2 w-2 rounded-full bg-emerald-500 animate-ping"></span>
              <span>v{APP_CONFIG.version} Release Live</span>
              <span className="text-slate-300 dark:text-slate-700">•</span>
              <span className="text-slate-600 dark:text-slate-400">Universal Android APK</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 dark:text-white leading-[1.12]">
              Smart Business Ledger & Khata. <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-500 via-blue-600 to-indigo-600 dark:from-brand-400 dark:via-blue-400 dark:to-indigo-300">
                100% Offline-First.
              </span>
            </h1>

            <p className="text-base sm:text-lg text-slate-600 dark:text-slate-300 max-w-2xl leading-relaxed">
              Track customer credit and debit, generate instantaneous UPI dynamic QR codes, export PDF financial statements, and synchronize automatically when connected. Native Android performance built with Jetpack Compose.
            </p>

            <div className="flex flex-col sm:flex-row items-center gap-3 w-full sm:w-auto pt-2">
              <a
                href={APP_CONFIG.downloadUrl}
                download="HisabKhata-Pro.apk"
                className="w-full sm:w-auto inline-flex items-center justify-center gap-3 px-6 py-3.5 text-base font-semibold text-white bg-brand-500 hover:bg-brand-600 active:scale-95 rounded-2xl shadow-md hover:shadow-lg transition-all"
              >
                <Download className="w-5 h-5" />
                <div className="text-left">
                  <div className="leading-tight">Download for Android</div>
                  <div className="text-[11px] font-normal text-blue-100 opacity-90">v{APP_CONFIG.version} • {APP_CONFIG.apkSize}</div>
                </div>
              </a>

              <a
                href="#features"
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-3.5 text-sm font-semibold text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-800/80 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-xs transition-colors"
              >
                <span>Explore Features</span>
                <ArrowRight className="w-4 h-4" />
              </a>

              <a
                href={APP_CONFIG.repoUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-4 py-3.5 text-sm font-semibold text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white transition-colors"
              >
                <Github className="w-4 h-4" />
                <span>GitHub Source</span>
              </a>
            </div>

            <div className="pt-4 border-t border-slate-200 dark:border-slate-800/80 w-full grid grid-cols-2 sm:grid-cols-4 gap-3 text-left">
              <div className="flex items-center gap-2">
                <WifiOff className="w-4 h-4 text-brand-500 shrink-0" />
                <div>
                  <div className="text-xs font-bold text-slate-900 dark:text-white">Offline Outbox</div>
                  <div className="text-[11px] text-slate-500">Room SQLite Engine</div>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Cpu className="w-4 h-4 text-emerald-500 shrink-0" />
                <div>
                  <div className="text-xs font-bold text-slate-900 dark:text-white">Jetpack Compose</div>
                  <div className="text-[11px] text-slate-500">Pure 60fps Native</div>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Shield className="w-4 h-4 text-indigo-500 shrink-0" />
                <div>
                  <div className="text-xs font-bold text-slate-900 dark:text-white">Android Keystore</div>
                  <div className="text-[11px] text-slate-500">Biometric & Pin Lock</div>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <FileDown className="w-4 h-4 text-amber-500 shrink-0" />
                <div>
                  <div className="text-xs font-bold text-slate-900 dark:text-white">PDF Ledger</div>
                  <div className="text-[11px] text-slate-500">Print & Share Direct</div>
                </div>
              </div>
            </div>

          </div>

          <div className="lg:col-span-5 flex justify-center lg:justify-end">
            <div id="preview" className="relative">
              <div className="absolute -inset-4 bg-gradient-to-tr from-brand-500/20 via-blue-500/10 to-indigo-500/20 rounded-[3rem] blur-xl opacity-75"></div>
              <PhoneMockup />
            </div>
          </div>

        </div>
      </div>
    </section>
  )
}
