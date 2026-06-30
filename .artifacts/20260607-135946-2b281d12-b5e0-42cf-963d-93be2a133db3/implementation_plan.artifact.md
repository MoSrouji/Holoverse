# Chat Feature Fixes Implementation Plan

This plan outlines the changes required to fix the chat feature, specifically addressing real-time updates, message ordering, offline support, and push notifications.

## Proposed Changes

### Data Layer (Firebase & Room)

#### [ChatRepositoryImpl.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat_system/data/repository/ChatRepositoryImpl.kt)

- **Real-time listeners**: Replace one-time fetches with `addSnapshotListener`.
- **Message Ordering**: Use `FieldValue.serverTimestamp()` for all timestamps sent to Firestore.
- **Timestamp Handling**: Gracefully handle `null` server timestamps in local Room entities by using a temporary local timestamp until the server-side value is available.
- **Notification Cleanup**: Remove client-side FCM triggering to follow the recommended server-side (Cloud Functions) approach.

```kotlin
// Example of real-time listener using callbackFlow
override fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
    val subscription = firestore.collection("chats").document(chatId)
        .collection("messages")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            snapshot?.let {
                val messages = it.documents.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                }
                repositoryScope.launch {
                    messageDao.insertMessages(messages.map { m -> m.toEntity(chatId) })
                }
            }
        }
    awaitClose { subscription.remove() }
}
```

#### [FirebaseModule.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/di/FirebaseModule.kt)

- Ensure `FirebaseFirestoreSettings` are configured for persistent cache (enabled by default, but can be made explicit).

---

### Push Notifications

#### [FcmService.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/chat_system/FcmService.kt)

- Update `onMessageReceived` to extract `chatId` from the notification's data payload.
- Include `chatId` in the `PendingIntent` so it can be handled by `MainActivity`.

#### [MainActivity.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/MainActivity.kt)

- Add logic in `onCreate` and `onNewIntent` to check for `chatId` in the intent extras.
- If a `chatId` is found, navigate the user directly to the corresponding chat screen.

---

### UI & ViewModel

#### [ChatViewModel.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/ui/chat/ChatViewModel.kt)

- Ensure that observing `getMessages` and `getChats` is handled correctly when the screen is focused or cleared. The repository's `Flow` lifecycle will naturally handle the listener attachment/detachment.

## Verification Plan

### Automated Tests
- I will verify if there are existing unit tests for `ChatRepository` and add new ones if feasible.
- Command: `./gradlew :app:testDebugUnitTest --tests "com.example.holoverse.chat_system.*"`

### Manual Verification
1.  **Real-time check**: Open two instances of the app (or use Firebase Console) and send a message. Verify it appears instantly without refreshing.
2.  **Ordering check**: Send messages from devices with different clock settings. Verify they appear in the correct order based on server time.
3.  **Offline check**: Disable internet, send a message, verify it shows as "SENDING" or similar. Re-enable internet and verify it syncs and updates timestamp.
4.  **Notification check**: Send a message from one user to another while the app is in the background. Tap the notification and verify it opens the correct chat.
