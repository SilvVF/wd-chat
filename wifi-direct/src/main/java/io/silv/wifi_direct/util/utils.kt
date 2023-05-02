package io.silv.wifi_direct.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.callbackFlow

private const val granted = PackageManager.PERMISSION_GRANTED

fun locationPerms(ctx: Context): Boolean = ActivityCompat
    .checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == granted

fun nearbyDevicePerms(ctx: Context): Boolean = if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
    ActivityCompat.checkSelfPermission(ctx, Manifest.permission.NEARBY_WIFI_DEVICES) == granted
} else {
    true
}

fun logd(item: String) {
    Log.d("wifi-receiver", item)
}
