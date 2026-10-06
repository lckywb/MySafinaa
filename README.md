# SAFINA Native — Full Feature Build

SAFINA is an Android-native prototype built around the principle **Regulate Exposure, Preserve Agency**.

## Included functional modules

- Epistemic Goal Anchor with local persistence
- Activity Session start/end
- SAFINA Cek: text claim triage
- SAFINA Cek: screenshot/image OCR with Google ML Kit
- Save for Later / Check Queue counter
- SAFINA Pause: 3-minute intervention and post-pause decision
- Epistemic Report with observed activity counters, pattern status, reflection and Adapt
- SAFINA Shield: URL technical-risk indicators
- Exposure Control: Usage Access + overlay permission setup
- Exposure monitor for common Instagram, TikTok and YouTube package IDs
- Edge bubble and expanded check-in prompt after monitored exposure threshold
- Six local educational micro-videos with post-video actions
- Android back navigation and native dialogs
- Local-first persistence using SharedPreferences

## Important design boundary

SAFINA does not automatically decide whether information is true, does not read private messages/passwords, does not automatically block or close other apps, and does not automatically report users/content. Usage time is treated as a proxy/context rather than proof of epistemic exposure.

## Build target

- Android API 37
- minSdk 23
- targetSdk 37
- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- JDK 17
- Google ML Kit Text Recognition 16.0.1

## First run

1. Set an epistemic goal from Home.
2. Test Cek with text.
3. Test Cek with a screenshot/image and OCR.
4. Test Pause.
5. Open Report and save a reflection/adaptation.
6. Test Shield with an HTTPS URL and a suspicious-looking URL.
7. Open Exposure Control and grant Usage Access + Overlay permission before starting the monitor.
8. Open Edukasi and play the six local videos.

The cloud workflow should be added separately in `.github/workflows/build.yml` after this project is uploaded to a new GitHub repository.
