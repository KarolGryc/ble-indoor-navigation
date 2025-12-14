package domain.usecase

import domain.model.Building
import domain.model.Zone

data class FoundPath (
    val path: List<Zone> = listOf()
)

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class FindPathBetweenUseCase() {
    operator fun invoke(building: Building, startZone: Zone, endZone: Zone): FoundPath?
}