package domain.usecase

import domain.model.Building
import domain.model.Zone

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FindPathBetweenUseCase {
    actual operator fun invoke(
        building: Building,
        startZone: Zone,
        endZone: Zone
    ): FoundPath? {
        TODO("Not yet implemented")
    }
}