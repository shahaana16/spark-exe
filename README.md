# Spark.exe — The Comeback Lab ✦

A complete responsive, installable personal study app for **desktop and mobile**. Built with plain HTML, CSS and JavaScript (no dependencies and no account required).

## Features
- Pomodoro focus studio with 25/50/custom study timers, a short break, pause/resume/reset, and local timer persistence
- Optional subject/task selection and distraction parking lot
- Task planner, priority, deadlines, completion, and exam countdowns
- Daily focus goals, seven-day chart, streaks, study history, habit garden, and reflection notes
- Personalized, rotating achievement-based motivation honoring past academic successes without pressuring the user
- Gentle in-app nudges while open and optional end-of-timer browser notifications
- Browser-local data, JSON backup/export and import, and offline PWA support

## Run on a PC now
1. Extract the ZIP.
2. Open a terminal **inside** the `spark-exe-app` folder.
3. Run `python -m http.server 8080` (or `py -m http.server 8080` on Windows).
4. Open `http://localhost:8080` in Chrome, Edge, Safari, or Firefox.

A **standalone HTML file** is also supplied separately for quick opening without a local server. App installation/offline caching uses the full project.

## Install as an app
- **Windows/macOS/Linux Chrome or Edge:** Open the hosted app and use the browser's **Install app** action in the address bar/menu.
- **Android Chrome:** Open the hosted app and choose **Add to Home screen** / **Install app**.
- **iPhone Safari:** Open the hosted app and choose **Share → Add to Home Screen**.

A PWA must be served from **HTTPS** (e.g., GitHub Pages/Netlify) or from `localhost` for installability/service workers. For a phone, publish the static folder to an HTTPS host. No backend/server code is required.

## Important limitations
- Phone and PC **do not automatically sync**. Use **Settings → Export backup / Import backup** to transfer progress, or set up a separate sync service in the future.
- Gentle nudges and completion notifications work while the page/app is running. This version does **not** implement background push notifications or device-level app blockers.
- If you clear browser storage, local progress is lost unless exported first.
- Timer measures wall-clock time. If the browser is closed during a session, the current timer is restored on reopening and a completed full timer is recorded.

## Privacy
All personal entries remain in browser localStorage unless you explicitly export a backup. No analytics or network API calls.
