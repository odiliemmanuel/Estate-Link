import { createTheme } from '@mui/material/styles'
import type { Theme } from '@mui/material/styles'
import type { ThemeMode } from './context/ThemeModeContext'

const display = '"Playfair Display", Georgia, serif'
const body = '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'

// Luxury refinement on the existing Estate-Link branding (deep teal + ember
// orange), tuned for both light and dark palettes.
const brand = {
  teal: '#0f4c5c',
  tealDeep: '#0a3542',
  tealBright: '#14b8a6',
  orange: '#e36414',
  orangeBright: '#f97316',
  gold: '#c9a86a',
}

export function getTheme(mode: ThemeMode): Theme {
  const isDark = mode === 'dark'

  const theme = createTheme({
    palette: {
      mode,
      primary: {
        main: isDark ? brand.tealBright : brand.teal,
        light: brand.tealBright,
        dark: brand.tealDeep,
        contrastText: '#ffffff',
      },
      secondary: {
        main: isDark ? brand.orangeBright : brand.orange,
        light: brand.orangeBright,
        dark: '#b04d0e',
        contrastText: '#ffffff',
      },
      background: {
        default: isDark ? '#0d1217' : '#f7f7f4',
        paper: isDark ? '#141b22' : '#ffffff',
      },
      divider: isDark ? 'rgba(255,255,255,0.08)' : 'rgba(15,76,92,0.12)',
      text: {
        primary: isDark ? '#e6ecef' : '#1f2937',
        secondary: isDark ? '#94a3ab' : '#5f6b73',
      },
    },
    typography: {
      fontFamily: body,
      h1: { fontFamily: display, fontWeight: 700 },
      h2: { fontFamily: display, fontWeight: 700 },
      h3: { fontFamily: display, fontWeight: 700 },
      h4: { fontFamily: display, fontWeight: 700, letterSpacing: '-0.02em' },
      h5: { fontFamily: display, fontWeight: 700, letterSpacing: '-0.01em' },
      h6: { fontFamily: display, fontWeight: 600 },
      subtitle1: { fontWeight: 600 },
      button: { textTransform: 'none', fontWeight: 600, letterSpacing: '0.01em' },
      overline: { fontFamily: display, letterSpacing: '0.14em' },
    },
    shape: { borderRadius: 12 },
    components: {
      MuiButton: {
        defaultProps: { disableElevation: true },
        styleOverrides: {
          root: ({ ownerState }) => ({
            borderRadius: ownerState.variant === 'outlined' ? 999 : 10,
            paddingLeft: 20,
            paddingRight: 20,
          }),
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            border: `1px solid ${isDark ? 'rgba(255,255,255,0.08)' : 'rgba(15,76,92,0.10)'}`,
            boxShadow: isDark ? 'none' : '0 2px 14px rgba(10,53,66,0.06)',
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          rounded: {
            borderRadius: 14,
          },
        },
      },
      MuiTextField: {
        defaultProps: { size: 'small' },
      },
      MuiChip: {
        styleOverrides: {
          root: { fontWeight: 600 },
        },
      },
      MuiListItemButton: {
        styleOverrides: {
          root: { borderRadius: 10 },
        },
      },
    },
  })

  return theme
}
