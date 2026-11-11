import React from 'react'
import { Terminal, Cpu, Database, Network, ShieldCheck, Layers, GitBranch, Smartphone } from 'lucide-react'
import { APP_CONFIG } from '../config/downloadConfig'

export const TechSpecsSection: React.FC = () => {
  const specs = [
    { label: "Architecture", value: "Clean Architecture + MVVM + MVI Unidirectional Flow", icon: Layers },
    { label: "Core Language", value: "Kotlin 2.0.21 with Coroutines & StateFlow", icon: Cpu },
    { label: "UI Toolkit", value: "Jetpack Compose + Material Design 3 Tokens", icon: Smartphone },
    { label: "Local Database", value: "Room SQLite 2.6.1 with Reactive Data Observers", icon: Database },
    { label: "Background Sync", value: "Android WorkManager with Exponential Backoff", icon: GitBranch },
    { label: "Networking Engine", value: "Retrofit 2.11 + OkHttp 4.12 HTTP/2 Client", icon: Network },
    { label: "Dependency Injection", value: "Google Hilt 2.52 (Dagger Architecture)", icon: Terminal },
    { label: "Security & Crypto", value: "Android Keystore + BiometricPrompt + AES-256", icon: ShieldCheck },
    { label: "Operating Platform", value: `Android 8.0 (API ${APP_CONFIG.minSdk}) to Android 15 (API ${APP_CONFIG.targetSdk})`, icon: Smartphone },
    { label: "Package Binary", value: `${APP_CONFIG.apkSize} Universal APK (${APP_CONFIG.packageName})`, icon: Terminal },
  ]

  return (
    <section id="tech-specs" className="py-20 bg-slate-50 dark:bg-slate-900/50 border-t border-slate-200/80 dark:border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-brand-100 text-brand-700 dark:bg-brand-950 dark:text-brand-300 mb-3">
            <Cpu className="w-3.5 h-3.5" />
            <span>Under The Hood</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Production-grade architecture from the ground up.
          </h2>
          <p className="mt-4 text-base text-slate-600 dark:text-slate-400">
            Engineered according to modern Google Android development standards for exceptional performance and zero battery drain.
          </p>
        </div>

        <div className="max-w-4xl mx-auto bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
          <div className="divide-y divide-slate-100 dark:divide-slate-800">
            {specs.map((item, idx) => {
              const Icon = item.icon
              return (
                <div 
                  key={idx}
                  className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-2 hover:bg-slate-50/60 dark:hover:bg-slate-800/40 transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 shrink-0">
                      <Icon className="w-4 h-4" />
                    </div>
                    <span className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                      {item.label}
                    </span>
                  </div>
                  <span className="text-xs sm:text-sm font-mono text-slate-600 dark:text-slate-400 text-left sm:text-right pl-9 sm:pl-0">
                    {item.value}
                  </span>
                </div>
              )
            })}
          </div>
        </div>

      </div>
    </section>
  )
}
