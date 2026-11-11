import React from 'react'
import { Github, ExternalLink, ArrowUp } from 'lucide-react'
import { APP_CONFIG } from '../config/downloadConfig'

export const Footer: React.FC = () => {
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <footer className="bg-slate-100 dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 lg:py-16">
        
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 lg:gap-12">
          
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center space-x-3">
              <img src="./logo.png" alt="HisabKhata Pro" className="w-9 h-9 rounded-xl shadow-xs" />
              <div className="flex flex-col">
                <span className="font-bold text-lg text-slate-900 dark:text-white leading-tight">
                  HisabKhata <span className="text-brand-500">Pro</span>
                </span>
                <span className="text-xs text-slate-500">Native Android Smart Ledger</span>
              </div>
            </div>

            <p className="text-sm text-slate-600 dark:text-slate-400 max-w-md leading-relaxed">
              HisabKhata Pro is a modern, 100% offline-capable business khata and customer credit management application for Android devices. Engineered with Jetpack Compose, Room SQLite, and bank-grade Keystore encryption.
            </p>

            <div className="flex items-center gap-3 pt-2">
              <a
                href={APP_CONFIG.repoUrl}
                target="_blank"
                rel="noopener noreferrer"
                aria-label="GitHub Repository"
                className="p-2.5 rounded-xl bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:text-brand-600 dark:hover:text-brand-400 border border-slate-200 dark:border-slate-700 transition-colors shadow-xs"
              >
                <Github className="w-4 h-4" />
              </a>
              <a
                href={APP_CONFIG.webAppUrl}
                target="_blank"
                rel="noopener noreferrer"
                aria-label="Web Platform"
                className="p-2.5 rounded-xl bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:text-brand-600 dark:hover:text-brand-400 border border-slate-200 dark:border-slate-700 transition-colors shadow-xs"
              >
                <ExternalLink className="w-4 h-4" />
              </a>
            </div>
          </div>

          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white mb-4">
              Navigation
            </h4>
            <ul className="space-y-2.5 text-sm">
              <li>
                <a href="#features" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">Features</a>
              </li>
              <li>
                <a href="#preview" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">App Preview</a>
              </li>
              <li>
                <a href="#screenshots" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">Screenshots</a>
              </li>
              <li>
                <a href="#tech-specs" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">Specifications</a>
              </li>
              <li>
                <a href="#whats-new" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">What's New (v{APP_CONFIG.version})</a>
              </li>
              <li>
                <a href="#faq" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">FAQ</a>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white mb-4">
              Platform & Downloads
            </h4>
            <ul className="space-y-2.5 text-sm">
              <li>
                <a href={APP_CONFIG.downloadUrl} className="font-semibold text-brand-600 dark:text-brand-400 hover:underline">
                  Download APK ({APP_CONFIG.apkSize})
                </a>
              </li>
              <li>
                <a href={APP_CONFIG.releasesUrl} target="_blank" rel="noopener noreferrer" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">
                  GitHub Releases
                </a>
              </li>
              <li>
                <a href={APP_CONFIG.webAppUrl} target="_blank" rel="noopener noreferrer" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">
                  HisabKhata Web Application
                </a>
              </li>
              <li>
                <a href={APP_CONFIG.repoUrl} target="_blank" rel="noopener noreferrer" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">
                  Source Code & Issues
                </a>
              </li>
            </ul>
          </div>

        </div>

        <div className="mt-12 pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500 dark:text-slate-400">
          <div className="flex items-center gap-1">
            <span>Built by</span>
            <a 
              href="https://github.com/SumanCH8514" 
              target="_blank" 
              rel="noopener noreferrer"
              className="font-semibold text-slate-700 dark:text-slate-300 hover:text-brand-500 underline decoration-slate-300 dark:decoration-slate-700 underline-offset-2"
            >
              Suman Chakrabortty (@SumanCH8514)
            </a>
          </div>

          <div className="flex items-center gap-4">
            <span>© {new Date().getFullYear()} HisabKhata Pro. All rights reserved.</span>
            <button
              onClick={scrollToTop}
              aria-label="Back to top"
              className="p-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 hover:text-brand-500 transition-colors"
            >
              <ArrowUp className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

      </div>
    </footer>
  )
}
