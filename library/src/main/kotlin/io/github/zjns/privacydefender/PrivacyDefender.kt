package io.github.zjns.privacydefender

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PrivacyDefender : Plugin<Project> {
    companion object {
        var DEBUG = false
            private set
        const val CONFIG_NAME = "privacyDefender"
    }

    override fun apply(target: Project) {
        target.extensions.add(CONFIG_NAME, PrivacyDefenderConfig::class.java)

        target.gradle.afterProject {
            if (it != target) return@afterProject
            var config = target.extensions.findByName(CONFIG_NAME) as? PrivacyDefenderConfig
            if (config == null) {
                println("[PrivacyDefender] privacy defender plugin config not found, use default")
                config = PrivacyDefenderConfig()
            }
            if (!config.enable || (!config.hookWeibo && !config.hookFontSdk)) {
                println("[PrivacyDefender] privacy defender plugin disabled or no hook enabled")
                return@afterProject
            }
            DEBUG = config.debug
            val android = target.extensions.getByName("android") as? AppExtension
            android?.registerTransform(PrivacyDefenderTransform(config))
        }
    }
}

open class PrivacyDefenderConfig {
    var enable: Boolean = true
    var debug: Boolean = false
    var hookWeibo: Boolean = true
    var hookFontSdk: Boolean = true

    fun enable(enable: Boolean) {
        this.enable = enable
    }

    fun debug(debug: Boolean) {
        this.debug = debug
    }

    fun hookWeibo(hookWeibo: Boolean) {
        this.hookWeibo = hookWeibo
    }

    fun hookFontSdk(hookFontSdk: Boolean) {
        this.hookFontSdk = hookFontSdk
    }
}
