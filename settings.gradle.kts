pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
    }

    val korgePluginVersion: String by settings

    plugins {
        id("com.soywiz.korge") version korgePluginVersion
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.soywiz") {
                useModule("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:$korgePluginVersion")
            }
        }
    }

}
