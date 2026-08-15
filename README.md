# StudyMate

Android application for finding study partners, based on the supplied product presentation.

## Daily delivery plan

1. Project foundation, Kotlin app shell, theme and feature map.
2. Registration, login and student profile UI.
3. Create, view and manage study requests.
4. Partner search, filters and matching flow.
5. Chat, contact flow and location sharing permissions/UI.
6. Firebase Authentication, Firestore, Storage and Cloud Messaging integration.
7. Validation, accessibility, tests and release polish.

Each day is kept as a focused Git commit so it can be pushed to GitHub independently.

## Firebase setup

Firebase Authentication, Cloud Firestore, Storage and Cloud Messaging SDKs are integrated. The app keeps a local fallback until a Firebase project is connected.

1. Create an Android app in Firebase Console with package name `com.studymate.app`.
2. Download `google-services.json` and place it in the `app/` directory.
3. Enable Email/Password Authentication, create a Firestore database and a Storage bucket.
4. Rebuild the project. The Google Services plugin is applied automatically when the configuration file exists.

The configuration file is intentionally ignored by Git.
