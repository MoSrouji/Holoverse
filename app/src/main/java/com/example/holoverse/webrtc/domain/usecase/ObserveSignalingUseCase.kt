package com.example.holoverse.webrtc.domain.usecase

import com.example.holoverse.webrtc.domain.repository.WebRtcRepository
import javax.inject.Inject

class ObserveSignalingUseCase @Inject constructor(private val repository: WebRtcRepository) {
    operator fun invoke(callId: String) = repository.startSignalingObservation(callId)
}
