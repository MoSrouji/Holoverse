# Task Management

- [x] Research existing chat implementation
- [x] Create implementation plan
- [x] Implement real-time messaging
	- [x] Update `ChatRepositoryImpl` with snapshot listeners
	- [x] Manage listener lifecycle in `ChatViewModel`
- [x] Fix message ordering
	- [x] Use `FieldValue.serverTimestamp()` in `sendMessage`
	- [x] Handle null timestamps in `toEntity`
- [x] Improve offline support
	- [x] Verify Firestore offline persistence settings
- [x] Implement push notification deep linking
	- [x] Update `FcmService` to pass `chatId`
	- [x] Update `MainActivity` to handle navigation from intent
- [x] Verification
	- [x] Run build verification
	- [x] Document changes in walkthrough
