/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          pink: '#C2185B',
          pressed: '#A0144A',
          soft: '#FCE4EC'
        },
        bg: {
          app: '#FFF7FA',
          card: '#FFFFFF',
          plum: '#1E1218',
          plumSurface: '#2A1A22'
        },
        ink: {
          primary: '#2B1B22',
          secondary: '#6B5560',
          border: '#F2C9D8'
        },
        status: {
          danger: '#D32F2F',
          safe: '#2E7D32',
          guardian: '#1565C0'
        }
      },
      borderRadius: {
        'card': '24px',
        'btn': '20px'
      }
    },
  },
  plugins: [],
}
