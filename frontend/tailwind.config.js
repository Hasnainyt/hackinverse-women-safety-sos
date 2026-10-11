/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        pink: {
          primary: '#C2185B',
          pressed: '#A0144A',
          soft: '#FCE4EC',
          border: '#F2C9D8',
          lightened: '#F06292',
        },
        bg: {
          app: '#FFF7FA',
          card: '#FFFFFF',
          plum: '#1E1218',
          plumSurface: '#2A1A22',
        },
        ink: {
          primary: '#2B1B22',
          secondary: '#6B5560',
          border: '#F2C9D8',
        },
        status: {
          danger: '#D32F2F', // ONLY for active SOS or danger
          safe: '#2E7D32',   // ACK received, confirmations, healthy mesh
          guardian: '#1565C0', // Info, responder / relay role
          amber: '#B26A00',   // Warning, cooldown, low budget
        }
      },
      borderRadius: {
        'card': '24px',
        'btn': '20px',
        'chip': '9999px',
      },
      boxShadow: {
        'flat-card': '0 2px 8px rgba(43, 27, 34, 0.06)',
        'flat-elevated': '0 4px 16px rgba(43, 27, 34, 0.08)',
        'pink-action': '0 4px 14px rgba(194, 24, 91, 0.25)',
      }
    },
  },
  plugins: [],
}
