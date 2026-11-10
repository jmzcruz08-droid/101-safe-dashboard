# 101 Safe Dashboard

A clean Android dashboard for Globe The 101 (hardware V1.0, firmware RA_M1_v5.2.00g).

## Safety scope
- No firmware image, updater, flashing, router login, shell execution, command injection, upload, or modem write operation.
- Shows the user-provided modem profile and the Android phone's current network transport.
- Includes an optional HTTPS download speed test (about 5 MB; uses internet data).
- Modem temperature, modem signal, and modem-side 4G/5G remain unavailable until a documented read-only router API is verified. The app does not guess these values.

## Build
GitHub Actions automatically builds a debug APK on pushes to main or when manually run from Actions. Download the artifact named `101-safe-dashboard-debug-apk`; extract it and install `app-debug.apk`.

## Device target
Globe The 101 • hardware V1.0 • firmware RA_M1_v5.2.00g

## Important
This is a new safe dashboard, not a patched copy of `BUGOK101_2.9.15_sign.apk`. Do not run the original updater APK against the modem. Application ID: `com.globe101.safedashboard`.
