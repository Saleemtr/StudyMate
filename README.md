# StudyMate

StudyMate is a native Android application that helps students find compatible study partners, publish study requests and communicate in real time.

The project is written in Kotlin and uses Firebase for authentication and cloud data synchronization.

## Main features

- Email and password registration and sign-in
- Persistent user sessions and secure logout
- Student profiles with department, courses, availability and biography
- Detailed engineering department options
- Create, edit, close and delete study requests
- Real-time synchronization of study requests with Cloud Firestore
- Search and filter students by name, course, department, availability and meeting mode
- Study-partner match score
- Real-time conversations between authenticated users
- Conversation list with the latest message and timestamp
- Location sharing through a clickable Google Maps link
- Loading, validation and Firebase error states
- Custom StudyMate branding, launcher icon and accessible navigation

## Technology

- Kotlin
- Android SDK
- XML layouts
- Gradle Kotlin DSL
- Firebase Authentication
- Cloud Firestore
- Firebase Cloud Messaging
- Firebase Storage

## Requirements

- Android Studio
- Android SDK 26 or newer
- A Firebase project
- An emulator with Google Play services or a physical Android device

## Firebase setup

1. Create a Firebase project.
2. Register an Android application with the package name `com.studymate.app`.
3. Download `google-services.json` and place it in the `app/` directory.
4. Enable the Email/Password provider in Firebase Authentication.
5. Create a Cloud Firestore database.
6. Enable Firebase Storage if profile-image uploads are required.
7. Publish the rules from `firestore.rules` in the Firebase Console.
8. Sync Gradle and run the application.

`google-services.json` is intentionally excluded from Git and must not be committed.

## Firestore structure

The application uses the following main collections:

- `users` — private account information and messaging installation identifiers
- `publicProfiles` — profile information visible to authenticated students
- `studyRequests` — study requests owned by individual users
- `chats` — conversation summaries and participant identifiers
- `chats/{chatId}/messages` — real-time messages for each conversation

Chat document identifiers are generated consistently from the two participant UIDs so both users open the same conversation.

## Running the project

1. Clone the repository:

   ```bash
   git clone https://github.com/Saleemtr/StudyMate.git
   ```

2. Open the cloned project in Android Studio.
3. Add your Firebase `google-services.json` file to `app/`.
4. Wait for Gradle synchronization to finish.
5. Select a physical device or a Google Play-enabled emulator.
6. Press **Run**.

## Verification

The completed user flow has been tested on a physical Android phone and an Android 14 Google Play emulator:

- Registration, validation, login, saved session and logout
- Profile loading and synchronization between accounts
- Study-request creation, editing, closing, deletion and persistence
- Partner search, filters and profile details
- Two-way real-time chat and conversation previews
- Duplicate-send prevention and Firebase failure handling
- Location sharing and opening the shared Google Maps link
- Back navigation, application branding and launcher icon

The project also passes:

```bash
./gradlew lintDebug assembleDebug
```

## Project status

The main application flow is complete and connected to Firebase. Future improvements may include dark mode, full push-notification delivery through a trusted backend and additional automated UI tests.
