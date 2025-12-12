package koin

import data.filesystemProviders.AndroidStoragePathProvider
import data.filesystemProviders.IoFileService
import data.filesystemProviders.IoFileServiceImpl
import data.filesystemProviders.StoragePathProvider
import data.repository.AndroidBleScanner
import data.service.AndroidCompassService
import domain.repository.BleScanner
import domain.service.CompassService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val filesystemModule = module {
    single<StoragePathProvider> {
        AndroidStoragePathProvider(context = androidContext())
    }

    single<IoFileService> {
        IoFileServiceImpl(pathProvider = get())
    }
}
actual val bleScanModule = module {
    single<BleScanner> {
        AndroidBleScanner(context = androidContext())
    }
}
actual val compassModule = module {
    single<CompassService> {
        AndroidCompassService(context = androidContext())
    }
}