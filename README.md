# Holoverse 🌌

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-green.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-orange.svg?style=flat&logo=firebase)](https://firebase.google.com)
[![ARCore](https://img.shields.io/badge/AR-ARCore-ff69b4.svg?style=flat)](https://developers.google.com/ar)
[![LiveKit](https://img.shields.io/badge/RealTime-LiveKit-brightgreen.svg?style=flat)](https://livekit.io)

**Holoverse** is a cutting-edge educational and collaborative platform that bridges the gap between digital and physical learning through Augmented Reality (AR) and high-performance real-time communication. Designed for mentors and students alike, it offers a rich ecosystem for immersive learning experiences.

---

## 🚀 Key Features

- **👓 Immersive AR Experience**: View and interact with 3D models in real-world space using ARCore and SceneView.
- **🎙️ Real-time Collaboration**: High-quality video and voice calling powered by **LiveKit (WebRTC)**.
- **💬 Intelligent Messaging**: Real-time chat with push notifications and multimedia support.
- **📚 Course Ecosystem**: Comprehensive tools for mentors to create and manage courses, and for students to explore popular and recommended content.
- **🛡️ Admin Control Panel**: Robust user management, broadcast capabilities, and system oversight.
- **🌐 Multi-language Support**: Built-in translation services using **ML Kit**.
- **💳 Secure Transactions**: Integrated payment and transaction history tracking.
- **📊 Performance Optimized**: Baseline profiles and efficient state management for a buttery-smooth UI.

---

## 🛠 Tech Stack

Holoverse is built with the modern Android developer in mind, utilizing the latest libraries and best practices:

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
- **Navigation**: [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) for type-safe, multi-backstack navigation.
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).
- **Backend**: [Firebase](https://firebase.google.com/) (Authentication, Firestore, Cloud Functions, Cloud Messaging, Crashlytics, Performance Monitoring).
- **AR/3D**: [ARCore](https://developers.google.com/ar) & [SceneView](https://github.com/SceneView/sceneview-android).
- **Real-time Comms**: [LiveKit Android SDK](https://github.com/livekit/client-sdk-android) & WebRTC.
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) & [DataStore](https://developer.android.com/topic/libraries/architecture/datastore).
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/).
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/).
- **Concurrency**: Kotlin Coroutines & Flow.
- **Media Management**: [Cloudinary](https://cloudinary.com/documentation/android_integration).

---

## 📁 Project Structure

The project follows a clean, feature-based architecture:

```text
app/src/main/java/com/example/holoverse/
├── auth/           # Authentication flows (Login, SignUp, Profiles)
├── ar/             # AR implementation and 3D viewing
├── chat/           # Real-time messaging
├── course/         # Course discovery and management
├── webrtc/         # LiveKit & Video/Voice call logic
├── admin/          # Admin Control Panel
├── core/           # Shared UI, data models, and utils
├── navigation/     # AppNavigation and destination definitions
└── di/             # Hilt modules
```

---

## 🏁 Getting Started

### Prerequisites

- **Android Studio Ladybug** (or newer).
- **Minimum SDK**: 30.
- **Target SDK**: 37.
- **Firebase Project**: You'll need to set up a project at the [Firebase Console](https://console.firebase.google.com/).

### Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/holoverse.git
   ```
2. **Firebase configuration**:
   - Download `google-services.json` from your Firebase project and place it in the `app/` directory.
3. **Local Properties**:
   Add your API keys and server URLs to `local.properties`:
   ```properties
   LIVEKIT_URL=your_livekit_url
   TOKEN_SERVER_URL=your_token_server_url
   ```
4. **Build & Run**:
   Sync your Gradle files and run the project on a physical device (recommended for AR features).

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request or open an issue for any bugs or feature requests.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
