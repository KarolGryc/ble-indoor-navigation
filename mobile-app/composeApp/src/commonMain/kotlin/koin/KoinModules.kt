package koin

import data.repository.LocalMapRepositoryImpl
import domain.repository.BuildingMapRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.module
import presentation.maplist.MapListViewModel
import presentation.navigationScreen.MapNavigationViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

expect val filesystemModule: Module

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

fun createKoinConfiguration(): KoinConfiguration {
    return KoinConfiguration {
        // platform specific modules
        modules(filesystemModule)

        // common modules
        modules(modules = listOf(mapListModule, navigationModule))
    }
}