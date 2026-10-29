package id.co.mondo.livestudio.core.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import id.co.mondo.livestudio.domain.model.TargetApp

/**
 * Helper untuk mengambil daftar aplikasi berpeluncur (Launcher Apps),
 * meluncurkan aplikasi target, dan mendeteksi aplikasi yang sedang aktif di layar depan.
 */
object AppHelper {

    /**
     * Mengambil daftar seluruh aplikasi yang memiliki ikon peluncur (Launcher) di perangkat,
     * kecuali aplikasi Live Studio sendiri, diurutkan berdasarkan nama aplikasi A-Z.
     */
    fun getInstalledLauncherApps(context: Context): List<TargetApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        return resolveInfos
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == context.packageName) return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
                if (label.isBlank()) return@mapNotNull null
                TargetApp(packageName = pkg, appName = label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    /**
     * Memuat ikon aplikasi berdasarkan nama package untuk ditampilkan di UI.
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? =
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }

    /**
     * Meluncurkan aplikasi target ke layar depan.
     */
    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                } ?: return false
            context.startActivity(launchIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Mendeteksi package aplikasi yang sedang tampil di layar depan (foreground)
     * menggunakan [UsageStatsManager]. Mengembalikan [fallbackPackage] jika tidak ada
     * event transisi baru dalam jendela waktu pemeriksaan.
     */
    fun getForegroundPackageName(
        context: Context,
        lookbackMs: Long = 10_000L,
        fallbackPackage: String? = null
    ): String? {
        if (!PermissionHelper.hasUsageAccess(context)) return fallbackPackage
        return try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(now - lookbackMs, now)
            val event = UsageEvents.Event()
            var latestForegroundPkg: String? = null
            var latestTimestamp = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                    event.timeStamp >= latestTimestamp
                ) {
                    val pkg = event.packageName
                    if (!pkg.isNullOrBlank() && !isIgnoredSystemOverlay(pkg)) {
                        latestForegroundPkg = pkg
                        latestTimestamp = event.timeStamp
                    }
                }
            }

            if (latestForegroundPkg != null) {
                latestForegroundPkg
            } else if (fallbackPackage != null) {
                fallbackPackage
            } else {
                // Jika baru pertama kali dicek dan belum ada event dalam 10 detik terakhir,
                // perluas pencarian ke 5 menit terakhir.
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    now - 300_000L,
                    now
                )
                stats
                    ?.filter { !it.packageName.isNullOrBlank() && !isIgnoredSystemOverlay(it.packageName) }
                    ?.maxByOrNull { it.lastTimeUsed }
                    ?.packageName
            }
        } catch (_: Exception) {
            fallbackPackage
        }
    }

    /**
     * Mengabaikan package sistem transien seperti controller izin atau systemui
     * agar tidak memicu false-pause sesaat ketika dialog volume/izin sistem muncul.
     */
    private fun isIgnoredSystemOverlay(packageName: String): Boolean {
        return packageName == "com.android.systemui" ||
            packageName.contains("permissioncontroller")
    }
}
