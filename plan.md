# Messaging System & Notification Fix Plan

## 1. Fix Notification Visibility & Importance [DONE]
- Updated `FcmService.kt` to use `IMPORTANCE_HIGH` for the notification channel.
- Added `setPriority(NotificationCompat.PRIORITY_HIGH)` and `setDefaults(NotificationCompat.DEFAULT_ALL)` to the notification builder.

## 2. Enhance FCM V1 Request Payload [IN PROGRESS]
- Update `FcmApi.kt` to use correct FCM V1 fields:
    - `AndroidConfig.priority` = `"high"`
    - `AndroidNotification.notification_priority` = `"PRIORITY_HIGH"`
    - `AndroidNotification.channel_id` = `"chat_notifications"`
- This ensures the system treats the message as high priority even if the app is killed or in the background.

## 3. Robust Error Handling & Authentication [DONE]
- Added logging and error handling for `service-account.json`.
- *Note: User has now added `service-account.json` to `app/src/main/assets/`.*

## 4. Enable Floating (Heads-up) Notifications
- Ensure the notification channel is created with `IMPORTANCE_HIGH`.
- The notification MUST have a sound or vibration to trigger heads-up display on most Android versions.
- The `channel_id` in the FCM payload must match the ID used to create the channel in `FcmService.kt`.

## 5. UI/UX Improvements for Notifications
- Use sender name as title and message text as body.
- Support different message types (Image, Video, etc.) in the notification text.

## 6. Verification
- Test sending messages while the app is in background/minimized.
- Verify that a floating banner appears at the top of the screen.
