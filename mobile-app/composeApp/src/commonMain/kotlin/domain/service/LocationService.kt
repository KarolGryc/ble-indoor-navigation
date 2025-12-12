package domain.service

import domain.model.Building
import domain.model.Fingerprint
import domain.model.Zone

interface LocationService {
    fun determineLocation(currentSignals: Fingerprint, building: Building): Zone?
}