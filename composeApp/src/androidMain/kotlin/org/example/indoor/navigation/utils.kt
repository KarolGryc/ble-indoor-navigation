package org.example.indoor.navigation

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun androidApiLevel(): Int {
    return android.os.Build.VERSION.SDK_INT
}

fun isAtLeastAndroidVersion(version: Int): Boolean {
    return androidApiLevel() >= version
}

fun hasPermission(context: Context, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}