package io.silv.on_boarding.use_case

import android.content.Context
import android.content.pm.PackageManager


fun interface CheckPermissionsGrantedUseCase: (List<String>) -> Boolean

internal fun checkPermissionsGrantedUseCaseImpl(
    permissionsToCheck: List<String>,
    context: Context
): Boolean = permissionsToCheck.map {
        context.checkSelfPermission(it)
    }
        .all { it == PackageManager.PERMISSION_GRANTED }