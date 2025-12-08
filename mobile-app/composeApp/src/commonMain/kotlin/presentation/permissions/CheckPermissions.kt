package presentation.permissions

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException

suspend fun checkPermission(
    permission: Permission,
    controller: PermissionsController,
    onGranted: (Permission) -> Unit = {},
    onDenied: (Permission) -> Unit = {},
    onPermanentlyDenied: (Permission) -> Unit = {},
    onCanceled: () -> Unit = {}
) {
    if(!controller.isPermissionGranted(permission)) {
        try {
            controller.providePermission(permission)
        } catch (e: Exception) {
            when (e) {
                is DeniedAlwaysException -> onPermanentlyDenied(permission)
                is DeniedException -> onDenied(permission)
                is RequestCanceledException -> onCanceled()
            }
        }
    } else {
        onGranted(permission)
    }
}

suspend fun checkPermissions(
    permissions: List<Permission>,
    controller: PermissionsController,
    onGranted: (List<Permission>) -> Unit = {},
    onDenied: (List<Permission>) -> Unit = {},
    onPermanentlyDenied: (List<Permission>) -> Unit = {},
    onCanceled: () -> Unit = {}
) {
    val granted = mutableListOf<Permission>()
    val denied = mutableListOf<Permission>()
    val permanentlyDenied = mutableListOf<Permission>()

    for (permission in permissions) {
        if(!controller.isPermissionGranted(permission)) {
            try {
                controller.providePermission(permission)
                granted.add(permission)
            } catch (e: Exception) {
                when (e) {
                    is DeniedAlwaysException -> permanentlyDenied.add(permission)
                    is DeniedException -> denied.add(permission)
                    is RequestCanceledException -> {
                        onCanceled()
                        return
                    }
                }
            }
        } else {
            granted.add(permission)
        }
    }

    if (granted.isNotEmpty()) {
        onGranted(granted)
    }
    if (denied.isNotEmpty()) {
        onDenied(denied)
    }
    if (permanentlyDenied.isNotEmpty()) {
        onPermanentlyDenied(permanentlyDenied)
    }
}