package id.co.mondo.livestudio.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun hasAudio(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    fun hasNotification(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun hasOverlay(context: Context): Boolean = Settings.canDrawOverlays(context)

    /** Izin wajib sebelum masuk Beranda: mikrofon dan overlay. Notifikasi bersifat opsional. */
    fun hasRequired(context: Context): Boolean = hasAudio(context) && hasOverlay(context)
}