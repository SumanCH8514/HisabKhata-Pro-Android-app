export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#0057BB',
          600: '#004aa0',
          700: '#003e85',
          800: '#00336e',
          900: '#002652',
          950: '#031735',
        },
        credit: {
          light: '#dcfce7',
          DEFAULT: '#16A34A',
          dark: '#15803d',
        },
        debit: {
          light: '#fee2e2',
          DEFAULT: '#DC2626',
          dark: '#b91c1c',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
