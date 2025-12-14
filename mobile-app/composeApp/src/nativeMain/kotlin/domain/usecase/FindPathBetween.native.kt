package domain.usecase

import domain.model.Building
import domain.model.Zone

actual class FindPathBetweenUseCase {
    actual fun invoke(
        building: Building,
        startZone: Zone,
        endZone: Zone
    ): FoundPath? {
        TODO("Not yet implemented")
    }
}