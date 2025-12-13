package presentation.permissions

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException

sealed interface PermissionCheckResult {
    object Granted : PermissionCheckResult
    object Denied : PermissionCheckResult
    object PermanentlyDenied : PermissionCheckResult
    object Canceled : PermissionCheckResult
}

suspend fun checkPermission(
    permission: Permission,
    controller: PermissionsController
) : PermissionCheckResult {
    if (!controller.isPermissionGranted(permission)) {
        try {
            controller.providePermission(permission)
        } catch (e: Exception) {
            when (e) {
                is DeniedAlwaysException -> return PermissionCheckResult.PermanentlyDenied
                is DeniedException -> return PermissionCheckResult.Denied
                is RequestCanceledException -> return PermissionCheckResult.Canceled
            }
        }
    }

    return PermissionCheckResult.Granted
}

suspend fun checkPermissions(
    permissions: List<Permission>,
    controller: PermissionsController
) : PermissionCheckResult {
    for (permission in permissions) {
        if (!controller.isPermissionGranted(permission)) {
            try {
                controller.providePermission(permission)
            } catch (e: Exception) {
                when (e) {
                    is DeniedAlwaysException -> return PermissionCheckResult.PermanentlyDenied
                    is DeniedException -> return PermissionCheckResult.Denied
                    is RequestCanceledException -> return PermissionCheckResult.Canceled
                }
            }
        }
    }

    return PermissionCheckResult.Granted
}