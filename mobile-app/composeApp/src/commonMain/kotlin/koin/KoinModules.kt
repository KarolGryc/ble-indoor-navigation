package koin

import data.repository.LocalMapRepositoryImpl
import domain.repository.BuildingMapRepository
import domain.service.KnnLocationService
import domain.service.LocationService
import domain.usecase.RecordFingerprintUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.module
import presentation.bleScanner.BleScanViewModel
import presentation.buildingNavigation.MapNavigationViewModel
import presentation.mapClassification.MapClassificationViewModel
import presentation.mapList.MapListViewModel
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

expect val filesystemModule: Module
expect val bleScanModule: Module
expect val compassModule: Module

val mapListModule = module {
    single<BuildingMapRepository> {
        LocalMapRepositoryImpl(
            fileIO = get()
        )
    }

    viewModel {
        MapListViewModel(
            mapRepository = get()
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
val navigationModule = module {
    factory<LocationService> {
        KnnLocationService()
    }

    factory<RecordFingerprintUseCase> {
        RecordFingerprintUseCase(scanner = get())
    }

    viewModel { (buildingId: Uuid) ->
        MapNavigationViewModel(
            buildingId = buildingId,
            mapRepository = get(),
            compassSensor = get(),
            locationService = get(),
            recordFingerprintUseCase = get(),
            scanner = get()
        )
    }
}

@OptIn(ExperimentalTime::class)
val devicesListModule = module {
    viewModel {
        BleScanViewModel(scanner = get())
    }
}

@OptIn(ExperimentalUuidApi::class)
val mapClassificationModule = module {
    viewModel { (uuid: Uuid) ->
        MapClassificationViewModel(
            buildingId = uuid,
            mapRepository = get(),
            scanner = get(),
            recordFingerprintUseCase = get()
        )
    }
}

fun createKoinConfiguration(): KoinConfiguration {
    return KoinConfiguration {
        // platform specific modules
        modules(
            modules = listOf(
                filesystemModule,
                bleScanModule,
                compassModule
            )
        )

        // common modules
        modules(
            modules = listOf(
                mapListModule,
                navigationModule,
                devicesListModule,
                mapClassificationModule
            )
        )
    }
}