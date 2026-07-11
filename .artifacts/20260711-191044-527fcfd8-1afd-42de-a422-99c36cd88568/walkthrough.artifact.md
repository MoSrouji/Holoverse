# Walkthrough - Help Center and Support Chat

I have implemented a comprehensive Help Center and a dedicated Support Chat system to improve user assistance in HoloVerse.

## Changes Made

### 1. Data Layer Enhancements
- **Chat Model**: Added `isSupportChat` flag to `Chat` (domain) and `ChatEntity` (data) to distinguish support threads.
- **Database**: Incremented `ChatDatabase` version to 6 to support the new field.
- **Repositories**:
    - `AuthRepository`: Added `getSupportAdmin()` to fetch the first available admin for support.
    - `ChatRepository`: Added `createOrGetSupportChat()` which initializes chats with the `isSupportChat` flag set to `true`.

### 2. Help Center UI
- **New Screen**: Created [HelpCenterScreen](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/user/presentation/help_center/HelpCenterScreen.kt) with an expandable FAQ list.
- **ViewModel**: Created [HelpCenterViewModel](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/user/presentation/help_center/HelpCenterViewModel.kt) to manage FAQ state and support chat initiation.
- **Profile Integration**: The "Help Center" item in the Profile screen now navigates to this new section.

### 3. Support Chat Filtering
- **Logic**: Updated [ChatViewModel](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat/presentation/ChatViewModel.kt) to filter out support chats from the main "Messages" list for non-admin users.
- **Admin View**: Admins can see all chats, including support ones, in their main message list, allowing them to manage support requests alongside regular messages.

### 4. Navigation
- Added `HelpCenter` and `ChatSupport` destinations to the navigation graph in [AppScreen](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/navigation/AppScreen.kt) and [AppNavigation](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/navigation/AppNavigation.kt).

## Verification Summary

- **Architecture**: Verified that domain models correctly propagate the `isSupportChat` flag.
- **Filtering**: Confirmed that `ChatViewModel` correctly handles conditional filtering based on user role.
- **Navigation**: Ensured all new routes are properly registered in `AppNavHost`.
- **UI**: Created a responsive Help Center with Material 3 components.

> [!NOTE]
> For the "Contact Support" feature to work, at least one user must exist in the `admins` collection in Firestore. I have used a `limit(1)` query to pick the first available admin as the support contact.
