import React, { useState } from 'react'
import { Download, Moon, Sun, Menu, X, ExternalLink, ShieldCheck } from 'lucide-react'
import { APP_CONFIG } from '../config/downloadConfig'

interface NavbarProps {
  darkMode: boolean
  toggleDarkMode: () => void
}

export const Navbar: React.FC<NavbarProps> = ({ darkMode, toggleDarkMode }) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const navLinks = [
    { name: 'Features', href: '#features' },
    { name: 'App Preview', href: '#preview' },
    { name: 'Screenshots', href: '#screenshots' },
    { name: 'Specifications', href: '#tech-specs' },
    { name: 'What\'s New', href: '#whats-new' },
    { name: 'FAQ', href: '#faq' },
  ]

  return (
    <header className="sticky top-0 z-50 glass-panel border-b border-slate-200/80 dark:border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <a href="#" className="flex items-center space-x-3 group">
          <img 
            src="./logo.png" 
            alt="HisabKhata Pro Logo" 
            className="w-9 h-9 rounded-xl shadow-sm ring-1 ring-brand-500/20 group-hover:scale-105 transition-transform" 
          />
          <div className="flex flex-col">
            <span className="font-bold text-lg leading-tight tracking-tight text-slate-900 dark:text-white flex items-center gap-1.5">
              HisabKhata <span className="text-brand-500">Pro</span>
              <span className="text-[10px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded-full bg-brand-50 text-brand-600 dark:bg-brand-950/60 dark:text-brand-400 border border-brand-200 dark:border-brand-800/50">
                Android
              </span>
            </span>
            <span className="text-[11px] text-slate-500 dark:text-slate-400 font-medium -mt-0.5">
              Smart Business Ledger
            </span>
          </div>
        </a>

        <nav className="hidden md:flex items-center space-x-7">
          {navLinks.map((link) => (
            <a
              key={link.name}
              href={link.href}
              className="text-sm font-medium text-slate-600 hover:text-brand-600 dark:text-slate-300 dark:hover:text-brand-400 transition-colors"
            >
              {link.name}
            </a>
          ))}
          <a
            href={APP_CONFIG.webAppUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-sm font-medium text-slate-600 hover:text-brand-600 dark:text-slate-300 dark:hover:text-brand-400 transition-colors flex items-center gap-1"
          >
            <span>Web App</span>
            <ExternalLink className="w-3.5 h-3.5 opacity-70" />
          </a>
        </nav>

        <div className="hidden md:flex items-center space-x-3">
          <button
            onClick={toggleDarkMode}
            aria-label="Toggle color theme"
            className="p-2 rounded-xl text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            {darkMode ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-600" />}
          </button>

          <a
            href="#download"
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold text-white bg-brand-500 hover:bg-brand-600 rounded-xl shadow-sm hover:shadow transition-all active:scale-95"
          >
            <Download className="w-4 h-4" />
            <span>Download APK</span>
          </a>
        </div>

        <div className="flex items-center gap-2 md:hidden">
          <button
            onClick={toggleDarkMode}
            aria-label="Toggle color theme"
            className="p-2 rounded-xl text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            {darkMode ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-600" />}
          </button>

          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle navigation menu"
            className="p-2 rounded-xl text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-200 dark:border-slate-800 bg-white/95 dark:bg-slate-900/95 backdrop-blur-lg px-4 pt-3 pb-6 space-y-3">
          <div className="flex flex-col space-y-2">
            {navLinks.map((link) => (
              <a
                key={link.name}
                href={link.href}
                onClick={() => setMobileMenuOpen(false)}
                className="px-3 py-2 text-base font-medium rounded-lg text-slate-700 hover:text-brand-600 hover:bg-slate-50 dark:text-slate-200 dark:hover:bg-slate-800/60 transition-colors"
              >
                {link.name}
              </a>
            ))}
            <a
              href={APP_CONFIG.webAppUrl}
              target="_blank"
              rel="noopener noreferrer"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 text-base font-medium rounded-lg text-slate-700 hover:text-brand-600 hover:bg-slate-50 dark:text-slate-200 dark:hover:bg-slate-800/60 transition-colors flex items-center justify-between"
            >
              <span>HisabKhata Web Platform</span>
              <ExternalLink className="w-4 h-4" />
            </a>
          </div>

          <div className="pt-2">
            <a
              href="#download"
              onClick={() => setMobileMenuOpen(false)}
              className="w-full flex items-center justify-center gap-2 px-4 py-2.5 text-base font-semibold text-white bg-brand-500 hover:bg-brand-600 rounded-xl shadow transition-colors"
            >
              <Download className="w-5 h-5" />
              <span>Download APK (v{APP_CONFIG.version})</span>
            </a>
            <p className="text-center text-xs text-slate-500 dark:text-slate-400 mt-2 flex items-center justify-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5 text-credit" />
              <span>Verified Clean APK • {APP_CONFIG.apkSize}</span>
            </p>
          </div>
        </div>
      )}
    </header>
  )
}
