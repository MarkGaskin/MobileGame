import com.soywiz.korge.gradle.*

plugins {
	id("com.soywiz.korge")
	kotlin("kapt") version "1.5.21"
}

buildscript {
	val korgePluginVersion: String by project
	dependencies {
		classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:$korgePluginVersion")
	}
}

repositories {
	mavenLocal()
	mavenCentral()
	google()
	maven("https://jitpack.io")
}

apply<KorgeGradlePlugin>()

korge {
	id = "com.sample.demo"
	name = "tr.io"

	orientation=Orientation.PORTRAIT
// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets
	
	targetJvm()
	targetJs()
	targetDesktop()
	targetIos()
	targetAndroidIndirect() // targetAndroidDirect()
	//targetAndroidDirect()
}

kotlin {
	val napierVersion: String by project
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation("io.github.aakira:napier:$napierVersion")
			}
		}
	}
}