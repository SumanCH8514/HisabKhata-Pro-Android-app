import React, { useState, useEffect } from 'react'
import { Navbar } from './components/Navbar'
import { HeroSection } from './components/HeroSection'
import { FeaturesSection } from './components/FeaturesSection'
import { ScreenshotsGallery } from './components/ScreenshotsGallery'
import { DownloadSection } from './components/DownloadSection'
import { WhatsNewSection } from './components/WhatsNewSection'
import { TechSpecsSection } from './components/TechSpecsSection'
import { FaqSection } from './components/FaqSection'
import { Footer } from './components/Footer'

export const App: React.FC = () => {
  const [darkMode, setDarkMode] = useState<boolean>(() => {
    const saved = localStorage.getItem('hk_theme')
    if (saved) return saved === 'dark'
    return window.matchMedia('(prefers-color-scheme: dark)').matches
  })

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('hk_theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('hk_theme', 'light')
    }
  }, [darkMode])

  const toggleDarkMode = () => {
    setDarkMode(prev => !prev)
  }

  return (
    <div className={`min-h-screen flex flex-col font-sans ${darkMode ? 'grid-bg-dark' : 'grid-bg-light'}`}>
      <Navbar darkMode={darkMode} toggleDarkMode={toggleDarkMode} />
      <main className="flex-1">
        <HeroSection />
        <FeaturesSection />
        <ScreenshotsGallery />
        <DownloadSection />
        <WhatsNewSection />
        <TechSpecsSection />
        <FaqSection />
      </main>
      <Footer />
    </div>
  )
}

export default App
