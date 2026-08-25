const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { AccessToken } = require("livekit-server-sdk");

// Secrets to be stored in Google Cloud Secret Manager
const LIVEKIT_API_KEY = defineSecret("LIVEKIT_API_KEY");
const LIVEKIT_API_SECRET = defineSecret("LIVEKIT_API_SECRET");

/**
 * Generates a LiveKit Join Token for the authenticated user.
 */
exports.getLiveKitToken = onCall(
  {
    secrets: ["LIVEKIT_API_KEY", "LIVEKIT_API_SECRET"],
    region: "us-central1" // Change to your preferred region
  },
  async (request) => {
    // 1. Verify Authentication
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "You must be logged in to start a call.");
    }

    const userId = request.auth.uid;
    const roomName = request.data.roomName;
    const participantName = request.data.participantName || userId;

    if (!roomName) {
      throw new HttpsError("invalid-argument", "roomName is required.");
    }

    console.log(`Generating token for room: ${roomName}, user: ${userId}`);

    // 2. Create AccessToken
    const at = new AccessToken(
      LIVEKIT_API_KEY.value(),
      LIVEKIT_API_SECRET.value(),
      {
        identity: participantName,
        ttl: "1h", // Token valid for 1 hour
      }
    );

    // 3. Set Permissions
    at.addGrant({
      roomJoin: true,
      room: roomName,
      canPublish: true,
      canSubscribe: true,
    });

    // 4. Generate JWT
    try {
      const token = await at.toJwt();
      return { token };
    } catch (error) {
      console.error("Token generation failed:", error);
      throw new HttpsError("internal", "Failed to generate security token.");
    }
  }
);
