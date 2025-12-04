package koin

import data.filesystemProviders.AndroidStoragePathProvider
import data.filesystemProviders.IoFileService
import data.filesystemProviders.IoFileServiceImpl
import data.filesystemProviders.StoragePathProvider
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