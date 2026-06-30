# Fix WebRTC "Failed to create answer" and Connection Stability Issues

The user is experiencing a "Failed to create answer" error in their WebRTC implementation. This usually happens when `createAnswer` is called on a PeerConnection that is not in the `have-remote-offer` state. In this project, this is caused by redundant Firestore snapshot emissions triggering the signaling logic multiple times. Additionally, there is a bug in the caller's logic where it waits for ICE gathering before setting the local description, which prevents ICE gathering from ever starting correctly.

## Proposed Changes

### [WebRTC Signaling Layer]

Fix redundant emissions in `SignalingManager` and ensure offer/answer are only processed once in `WebRtcManager`.

#### [SignalingManager.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/webrtc/SignalingManager.kt)

- Add `distinctUntilChanged()` to `observeOffer` and `observeAnswer` to prevent redundant triggers when other document fields (like ICE candidates) change.

#### [WebRtcManager.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/webrtc/WebRtcManager.kt)

- **Fix Offer/Answer Processing**: In `observeSignaling`, use `first { it != null }` (or similar logic) to ensure the offer and answer are handled exactly once. This prevents the state machine from being reset or confused by redundant signaling.
- **Fix ICE Gathering Start**: In `startCall`, call `setLocalDescription` immediately after `createOffer` so that ICE gathering actually starts.
- **Improve ICE Candidate Handling**:
    - Avoid re-adding the same ICE candidates to the `PeerConnection`.
    - Fix `observeIceCandidates` to handle the list of candidates more efficiently.
- **Fix `waitForIceGatheringAndSendOffer`**: Update it to call `setLocalDescription` first and then optionally wait for gathering if a full (non-trickle) SDP is desired, though trickle ICE is already partially implemented.

## Verification Plan

### Manual Verification
- **Scenario 1: Caller to Callee**
    1. Start the app on Device A and Device B.
    2. Device A clicks "Start Call" with a unique ID.
    3. Device B clicks "Join Call" with the same ID.
    4. Verify that the "Failed to create answer" error no longer appears.
    5. Verify that video/audio connection is established.
- **Scenario 2: Swap Roles**
    1. Repeat the above with Device B as Caller and Device A as Callee.
- **Log Inspection**:
    - Check Logcat for "Signaling State: have-remote-offer" followed by "Answer created successfully".
    - Ensure "Remote description set successfully" only appears once per call.
    - Verify that ICE candidates are being gathered and added without errors.
