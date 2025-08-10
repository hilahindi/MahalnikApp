#  Mahalnik – All a Mahal Soldier Needs, in One App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)

**Mahalnik** is an Android application built to support **Mahal soldiers** (international volunteers serving in the IDF) by centralizing essential information, documents, and service tools in one secure, easy-to-use app.

---
## Table of Contents
- [✨ Features](#features)
- [📱 Screens](#screens)
- [🏗️ Architecture](#architecture)
- [📦 Dependencies & Libraries](#dependencies--libraries)
- [📂 Project Structure](#project-structure)
- [🚀 Installation](#installation)
- [⚙️ Configuration](#configuration)
- [👩‍💻 Author](#author)


---

## Features

### 🔐 Authentication & Profile
- Email/Password authentication with Firebase Authentication.
- Editable personal profile with many fields (name, phone, email, birth date, country, personal number, ID, address, enlistment/release dates, role, base, service stage, and emergency contacts).

### 📂 Files Center
- List of official forms and guides managed in Firestore (`files`).
- One-tap download from Firebase Storage via `DownloadManager`.
- RecyclerView with `FilesAdapter` for a clean list UI.

### 📅 Events
- Monthly/weekly view of upcoming events stored in Firestore (`events`).
- Add events via a bottom sheet (`AddEventBottomSheet`).
- Marked dates and a RecyclerView list (`EventsAdapter`).

### 📖 Information Hub
- Category → content flow using the Android Navigation Component.
- Dynamic pages loaded from Firestore by category in `InfoContentFragment` (e.g., housing, health).

### 📞 Contact & Support
- Contact page to reach staff: open mail/phone intents and upload an attachment if needed.
- Welcomes the signed-in user by name (pulled from the `users` collection).

---

## Screens
- `LoginActivity` / `SignupActivity` – authentication flow.
- `MainActivity` with bottom navigation.
- `ProfileActivity` – view/edit a large set of personal fields.
- `FilesActivity` – browse and download managed files.
- `EventsActivity` – view upcoming events and add new ones.
- `ContactActivity` – reach support (email/phone), optional file upload.
- `InfoCategoriesFragment` → `InfoContentFragment` – browse categories and view Firestore-driven content.

---

## Architecture
- **Activities + Fragments** with a shared `BaseActivity` for common behavior.
- **Navigation Component** (Safe Args) for type-safe navigation between fragments.
- **Adapters** for RecyclerViews (`EventsAdapter`, `FilesAdapter`).
- **Models**: simple Kotlin data classes (`Event`, `MyFile`).
- **Firestore** for dynamic content, events, users, and documents.
- **Storage** for file hosting.
- **DownloadManager** API for robust background downloads.

---

## Dependencies & Libraries
- 📷 **Image Loading:** Glide
- 📆 **Calendar UI:** Material CalendarView
- 🌐 **Networking:** OkHttp (Contact form handling)
- 📧 **Email Service:** SendGrid (sending contact form emails)
- 📥 **File Downloads:** Android DownloadManager API
- 🎨 **UI Components:** Material Design Components
- 🧪 **Testing:** JUnit, Espresso

---

## Project Structure
```
app/src/main/java/com/example/mahalapp/
├─ activities/
│  ├─ BaseActivity.kt
│  ├─ LoginActivity.kt
│  ├─ SignupActivity.kt
│  ├─ MainActivity.kt
│  ├─ ProfileActivity.kt
│  ├─ FilesActivity.kt
│  ├─ EventsActivity.kt
│  └─ ContactActivity.kt
├─ fragments/
│  ├─ InfoCategoriesFragment.kt
│  └─ InfoContentFragment.kt
├─ adapters/
│  ├─ EventsAdapter.kt
│  └─ FilesAdapter.kt
├─ models/
│  ├─ Event.kt
│  └─ MyFile.kt
└─ ui/
   ├─ AddEventBottomSheet.kt
   └─ EventDecorator.kt
```

---

## Installation
**Prerequisites**
- Android Studio (latest) and Android SDK 26+
- A Firebase project

**Steps**
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/mahalnik.git
   cd mahalnik
   ```
2. Open the project in Android Studio
3. Add `google-services.json` in `/app`
4. Build & Run

---

## ⚙️ Configuration
**Firebase Setup**

**Firebase Authentication**
- Enable Email/Password authentication in Firebase Console
- Configure sign-in methods as needed

**Firestore Database**
- Create a Firestore database
- Set up collections: `users`, `documents`, `events`, `info`, `requests`

**Storage**
- Enable Firebase Storage for file uploads and downloads
- Organize files according to `storagePath` values in Firestore

---

## Author
**Hila Hindi**  
Computer Science Student  
