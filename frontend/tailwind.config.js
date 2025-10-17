/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      keyframes: {
        'flash-green': {
          '0%, 100%': { backgroundColor: 'transparent' },
          '50%': { backgroundColor: 'rgb(134 239 172)' }, // green-300
        },
        'pulse-green': {
          '0%, 100%': {
            backgroundColor: 'rgb(187 247 208)', // green-200
            boxShadow: '0 0 0 0 rgba(34, 197, 94, 0.7)',
            transform: 'scale(1)',
          },
          '50%': {
            backgroundColor: 'rgb(134 239 172)', // green-300
            boxShadow: '0 0 0 8px rgba(34, 197, 94, 0)',
            transform: 'scale(1.05)',
          },
        },
        'slide-in': {
          '0%': { transform: 'translateX(-100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'scale-in': {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        }
      },
      animation: {
        'flash-green': 'flash-green 1s ease-in-out',
        'pulse-green': 'pulse-green 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'slide-in': 'slide-in 0.3s ease-out',
        'fade-in': 'fade-in 0.5s ease-in',
        'scale-in': 'scale-in 0.3s ease-out',
      },
    },
  },
  plugins: [],
}