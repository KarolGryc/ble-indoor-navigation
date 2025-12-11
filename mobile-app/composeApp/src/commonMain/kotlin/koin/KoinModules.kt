package koin

import data.repository.LocalMapRepositoryImpl
import domain.repository.BuildingMapRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.module
import presentation.bleScanner.BleScanViewModel
import presentation.mapClassification.MapClassificationViewModel
import presentation.mapList.MapListViewModel
import presentation.navigationScreen.MapNavigationViewModel
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

expect val filesystemModule: Module
expect val bleScanModule: Module

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
    viewModel { (buildingId: Uuid) ->
        MapNavigationViewModel(
            buildingId = buildingId,
            mapRepository = get()
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
            scanner = get()
        )
    }
}

fun createKoinConfiguration(): KoinConfiguration {
    return KoinConfiguration {
        // platform specific modules
        modules(modules = listOf(filesystemModule, bleScanModule))

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