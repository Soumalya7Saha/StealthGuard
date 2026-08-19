# 🥷 StealthGuard

**StealthGuard** is a mobile-first, Mobile application disguised as a fully functional, dark-mode native calculator. Beneath the surface, it features a covert emergency SOS trigger that silently fetches high-accuracy GPS coordinates and redirects the user to WhatsApp or SMS to dispatch a pre-written distress message.

---

## ✨ Features

### 🧮 The Decoy (Calculator UI)
*   **Fully Functional:** Standard arithmetic operations (+, −, ×, ÷, %, ±).
*   **Native Feel:** Designed to mimic native iOS/Android dark-mode calculators.
*   **Mobile-Optimized UX:** Prevents text selection, double-tap zoom, and page overscroll. 
*   **Responsive:** Scales perfectly across all mobile screen sizes.

### 🚨 Covert SOS Trigger
*   **Hidden Activation:** Typing `911` (default) and pressing `=` activates the sequence instead of calculating.
*   **Silent GPS Fetch:** Utilizes the browser's Geolocation API to get a high-accuracy location fix.
*   **Seamless Dispatch:** Automatically redirects to WhatsApp or SMS with a pre-filled SOS message and a live Google Maps link.
*   **Graceful Fallback:** If GPS fails or permissions are denied, it dispatches an alternate message stating location is unavailable and requests an immediate call.

### ⚙️ Discreet Configuration Modal
*   **Hidden Access:** Typing `0000` and pressing `=` opens a hidden settings dashboard.
*   **Customizable:** Users can change the emergency contact number, customize the SOS trigger code, and choose the default dispatch channel (WhatsApp vs. SMS).
*   **Privacy-First:** All settings are saved locally on the device using `localStorage`. No databases, no backend tracking.

---

## 🛠️ Tech Stack

*   **Framework:** [TanStack Start](https://tanstack.com/start) / React
*   **Language:** TypeScript
*   **Styling:** Tailwind CSS v4 (utilizing OKLCH-based semantic color tokens)
*   **Icons:** [Lucide React](https://lucide.dev/) (or similar minimal icon library)
*   **Deployment:** Client-side only (can be hosted on Vercel, Netlify, or GitHub Pages)

---

## 🚀 Getting Started

### Prerequisites
Make sure you have Node.js (v18+ recommended) and `npm`, `yarn`, or `pnpm` installed.

### Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Soumalya7Saha/stealthguard.git](https://github.com/Soumalya7Saha/stealthguard.git)
   cd stealthguard
