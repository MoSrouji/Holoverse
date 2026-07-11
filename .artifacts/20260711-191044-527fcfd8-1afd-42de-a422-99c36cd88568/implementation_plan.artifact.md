# Implementation Plan - Help Center with FAQ and Admin Chat

Implement a Help Center in the Profile screen containing an FAQ section and a support chat with the admin. The support chat will be filtered out from the regular conversation list for students and mentors but will be visible to admins.

## User Review Required

> [!IMPORTANT]
> - **Admin Identification**: I will implement a mechanism to fetch the first available admin to act as the "Support Admin". If no admins exist, "Contact Support" might fail.
> - **Chat Visibility**: Support chats will be identified by a new `isSupportChat` flag in the `Chat` model.
> - **FAQ Content**: I will provide a set of common FAQs. These will be static for now.

## Proposed Changes

### Domain Models

#### [Chat.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat/domain/model/Chat.kt)
- Add `isSupportChat: Boolean = false` to the `Chat` data class.

---

### Data Layer

#### [ChatEntity.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat/data/local/entities/ChatEntity.kt)
- Add `isSupportChat: Boolean = false` to the `ChatEntity`.

#### [ChatDatabase.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat/data/local/ChatDatabase.kt)
- Increment database version to 6.

#### [AuthRepository.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/auth/domain/repository/AuthRepository.kt)
- Add `getSupportAdmin(): Flow<Response<User.Admin>>`.

#### [AuthRepositoryImpl.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/auth/data/repository/AuthRepositoryImpl.kt)
- Implement `getSupportAdmin()` by fetching the first document from the `admins` collection.

#### [ChatRepository.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat/domain/repository/ChatRepository.kt)
- Add `createOrGetSupportChat(userId: String, userName: String, userImageUrl: String?, admin: User.Admin): Flow<Response<String>>`.

#### [ChatRepositoryImpl.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat/data/repository/ChatRepositoryImpl.kt)
- Implement `createOrGetSupportChat()` which sets `isSupportChat = true`.
- Update `getChats()` to support filtering (or let the ViewModel handle filtering).

---

### Presentation Layer (New Screen)

#### [NEW] [HelpCenterScreen.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/user/presentation/help_center/HelpCenterScreen.kt)
- UI for FAQ list (expandable items).
- "Contact Support" button that navigates to the chat with the admin.

#### [NEW] [HelpCenterViewModel.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/user/presentation/help_center/HelpCenterViewModel.kt)
- Handles fetching FAQ and initiating support chat.

---

### UI & Navigation Updates

#### [ProfileScreen.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/user/presentation/profile/ProfileScreen.kt)
- Add `onHelpCenterClick` parameter.
- Link the "Help Center" item to `onHelpCenterClick`.

#### [AppScreen.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/navigation/AppScreen.kt)
- Add `data object HelpCenter : AppDestination` and `ChatSupport(val chatId: String) : AppDestination`.

#### [AppNavigation.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/navigation/AppNavigation.kt)
- Add navigation entries for `HelpCenter` and `ChatSupport`.
- `ChatSupport` will reuse `ChatScreen` but might need a way to identify itself as a support chat for UI purposes (e.g., specific title).

#### [ChatViewModel.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat/presentation/ChatViewModel.kt)
- Filter `uiState.chats` to exclude `isSupportChat == true` if the `currentUser` is not an Admin.

## Verification Plan

### Automated Tests
- N/A (UI focused)

### Manual Verification
1. **Student/Mentor View**:
    - Navigate to Profile -> Help Center.
    - Check FAQ list.
    - Click "Chat with Admin".
    - Verify chat opens and messages can be sent.
    - Go back to main "Messages" tab (ChatList).
    - **Verify that the Support Chat is NOT visible in the main chat list.**
2. **Admin View**:
    - Login as Admin.
    - Go to "Messages" tab.
    - **Verify that the Support Chat from the student IS visible in the list.**
    - Reply to the student.
3. **Student View (again)**:
    - Go to Help Center -> Support Chat.
    - Verify admin's reply is visible.
