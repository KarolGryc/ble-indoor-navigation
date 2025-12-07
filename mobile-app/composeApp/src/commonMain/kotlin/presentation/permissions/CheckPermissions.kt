package presentation.permissions

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException

suspend fun checkPermission(
    permission: Permission,
    controller: PermissionsController,
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onPermanentlyDenied: () -> Unit = {},
    onCanceled: () -> Unit = {}
) {
    if(!controller.isPermissionGranted(permission)) {
        try {
            controller.providePermission(permission)
        } catch (e: Exception) {
            when (e) {
                is DeniedAlwaysException -> onPermanentlyDenied()
                is DeniedException -> onDenied()
                is RequestCanceledException -> onCanceled()
            }
        }
    } else {
        onGranted()
    }
}