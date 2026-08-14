package com.example.holoverse.webrtc.domain.model

import org.webrtc.VideoTrack

data class Participant(
    val id: String,
    val identity: String,
    val videoTrack: livekit.org.webrtc.VideoTrack? = null,
    val livekitVideoTrack: io.livekit.android.room.track.VideoTrack? = null,
    val isAudioEnabled: Boolean = true,
    val isVideoEnabled: Boolean = true
)
