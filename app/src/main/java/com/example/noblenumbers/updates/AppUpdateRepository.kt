package com.example.noblenumbers.updates

import android.content.Context
import com.example.noblenumbers.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AppUpdateRepository(
    private val context: Context,
) {
    suspend fun checkLatestRelease(): UpdateCheckResult = withContext(Dispatchers.IO) {
        val release = fetchLatestRelease()
        val versionCode = release.versionCode
            ?: return@withContext UpdateCheckResult.Error(UpdateError.ReleaseVersionMissing)
        if (versionCode <= BuildConfig.VERSION_CODE) {
            return@withContext UpdateCheckResult.NoUpdate
        }

        val asset = release.assets.firstOrNull { it.name == BuildConfig.UPDATE_APK_ASSET }
            ?: return@withContext UpdateCheckResult.Error(UpdateError.ApkAssetMissing)

        UpdateCheckResult.UpdateAvailable(
            update = AvailableUpdate(
                versionCode = versionCode,
                label = release.tagName,
                downloadUrl = asset.downloadUrl,
                assetName = asset.name,
            ),
        )
    }

    suspend fun download(update: AvailableUpdate): File = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, UPDATE_CACHE_DIR).apply { mkdirs() }
        val apkFile = File(updatesDir, update.assetName)
        val connection = openConnection(update.downloadUrl)
        try {
            connection.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            connection.disconnect()
        }
        apkFile
    }

    private fun fetchLatestRelease(): GitHubRelease {
        val url = "https://api.github.com/repos/${BuildConfig.UPDATE_GITHUB_OWNER}/" +
            "${BuildConfig.UPDATE_GITHUB_REPO}/releases/latest"
        val connection = openConnection(url)
        val json = try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
        val root = JSONObject(json)
        val tagName = root.optString("tag_name").ifBlank {
            root.optString("name").ifBlank { "latest" }
        }
        val assetsJson = root.optJSONArray("assets")
        val assets = buildList {
            if (assetsJson != null) {
                for (index in 0 until assetsJson.length()) {
                    val asset = assetsJson.getJSONObject(index)
                    add(
                        GitHubReleaseAsset(
                            name = asset.getString("name"),
                            downloadUrl = asset.getString("browser_download_url"),
                        ),
                    )
                }
            }
        }
        return GitHubRelease(
            tagName = tagName,
            versionCode = tagName.versionCodeFromTag(),
            assets = assets,
        )
    }

    private fun openConnection(url: String): HttpURLConnection {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = NETWORK_TIMEOUT_MILLIS
            readTimeout = NETWORK_TIMEOUT_MILLIS
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "NobleNumbers/${BuildConfig.VERSION_NAME}")
        }
        val code = connection.responseCode
        if (code !in HTTP_SUCCESS_RANGE) {
            connection.disconnect()
            throw UpdateNetworkException(code)
        }
        return connection
    }

    private fun String.versionCodeFromTag(): Int? {
        val plusVersion = substringAfterLast(delimiter = "+", missingDelimiterValue = "")
            .toIntOrNull()
        if (plusVersion != null) return plusVersion
        return Regex("""(?:versionCode|code|vc)[-_: ]?(\d+)""", RegexOption.IGNORE_CASE)
            .find(this)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
    }

    private companion object {
        const val UPDATE_CACHE_DIR = "updates"
        const val NETWORK_TIMEOUT_MILLIS = 15_000
        val HTTP_SUCCESS_RANGE = 200..299
    }
}

data class AvailableUpdate(
    val versionCode: Int,
    val label: String,
    val downloadUrl: String,
    val assetName: String,
)

sealed interface UpdateCheckResult {
    data object NoUpdate : UpdateCheckResult
    data class UpdateAvailable(val update: AvailableUpdate) : UpdateCheckResult
    data class Error(val error: UpdateError) : UpdateCheckResult
}

enum class UpdateError {
    Network,
    ReleaseVersionMissing,
    ApkAssetMissing,
    DownloadFailed,
    InstallPermissionRequired,
    InstallerLaunchFailed,
}

class UpdateNetworkException(
    val code: Int,
) : Exception("GitHub release request failed with HTTP $code")

private data class GitHubRelease(
    val tagName: String,
    val versionCode: Int?,
    val assets: List<GitHubReleaseAsset>,
)

private data class GitHubReleaseAsset(
    val name: String,
    val downloadUrl: String,
)
