package com.example.noblenumbers.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

class AppUpdateInstaller(
    private val context: Context,
) {
    fun launchInstall(apkFile: File): InstallLaunchResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return runCatching {
                context.startActivity(settingsIntent)
                InstallLaunchResult.PermissionRequired
            }.getOrElse {
                InstallLaunchResult.Failed
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return runCatching {
            context.startActivity(installIntent)
            InstallLaunchResult.Started
        }.getOrElse {
            InstallLaunchResult.Failed
        }
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}

enum class InstallLaunchResult {
    Started,
    PermissionRequired,
    Failed,
}
