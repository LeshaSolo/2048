package com.example.noblenumbers.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

val LocalLanguageTag = compositionLocalOf { "en" }

@Composable
fun ProvideAppLanguage(languageTag: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLanguageTag provides languageTag, content = content)
}

@Composable
fun localizedString(@StringRes id: Int, vararg args: Any): String {
    val context = LocalContext.current
    val configuration = Configuration(LocalConfiguration.current)
    configuration.setLocale(Locale.forLanguageTag(LocalLanguageTag.current))
    val resources = context.createConfigurationContext(configuration).resources
    return if (args.isEmpty()) resources.getString(id) else resources.getString(id, *args)
}
