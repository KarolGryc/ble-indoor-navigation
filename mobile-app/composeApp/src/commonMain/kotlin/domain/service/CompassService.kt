package domain.service

import kotlinx.coroutines.flow.Flow

interface CompassService {
    val azimuth: Flow<Float>
}