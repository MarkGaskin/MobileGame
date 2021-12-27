import com.soywiz.korge.gradle.*

plugins {
	id("com.soywiz.korge")
	kotlin("kapt") version "1.5.21"
}

buildscript {
	val korgePluginVersion: String by project
	dependencies {
		classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:$korgePluginVersion")
		classpath("com.google.gms:google-services:4.3.10")
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
	admob("ca-app-pub-2040551467408424~4460713550")
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
				//implementation("com.google.android.gms:play-services-ads:20.5.0")

				// Import the Firebase BoM
				//implementation("com.google.firebase:firebase-bom:29.0.3")

				// When using the BoM, you don't specify versions in Firebase library dependencies

				// Declare the dependency for the Firebase SDK for Google Analytics
				//implementation("com.google.firebase:firebase-analytics-ktx")
			}
		}
	}
}