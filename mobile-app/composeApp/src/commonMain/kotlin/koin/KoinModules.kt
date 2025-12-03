package koin

import data.repository.LocalMapRepositoryImpl
import domain.repository.BuildingMapRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.module
import presentation.maplist.MapListViewModel

expect val filesystemModule: Module

val mapListModule: Module = module {
    single<BuildingMapRepository> {
        LocalMapRepositoryImpl(fileIO = get())
    }

    viewModel {
        MapListViewModel(
            mapRepository = get()
        )
    }
}

fun createKoinConfiguration(): KoinConfiguration {
    return KoinConfiguration {
        // platform specific modules
        modules(filesystemModule)

        // common modules
        modules(mapListModule)
    }
}