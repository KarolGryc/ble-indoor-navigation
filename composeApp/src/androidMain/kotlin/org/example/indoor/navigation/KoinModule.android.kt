package org.example.indoor.navigation

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual val targetModule = module {
    single {
        BluetoothScanner(
            context = androidContext()
        )
    }
    viewModel {
        BleSimpleViewModel(
            bluetoothScanner = get()
        )
    }
}