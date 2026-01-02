import net.luis.lm.LineEnding
import java.time.Year
import java.util.Properties

plugins {
	kotlin("jvm") version "2.2.10"
	id("java-gradle-plugin")
	id("maven-publish")
	id("net.luis.lm") version "1.2.0"
}

group = "net.luis"
version = "1.2.0"

var mavenUsername: String? = null
var mavenPassword: String? = null
val file: File = rootProject.file("./../credentials.properties")
if (file.exists()) {
	val properties = Properties()
	file.inputStream().use { properties.load(it) }
	
	mavenUsername = properties.getProperty("username")
	mavenPassword = properties.getProperty("password")
} else {
	throw GradleException("No credentials.properties file found.")
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	implementation(gradleApi())
	implementation(localGroovy())
}

licenseManager {
	header = "header.txt"
	lineEnding = LineEnding.LF
	spacingAfterHeader = 1
	
	variable("year", Year.now())
	variable("author", "Luis Staudt")
	variable("project", rootProject.name)
	
	sourceSets = listOf("main", "test")
	
	include("**/*.kt")
}

gradlePlugin {
	plugins {
		register("licenseManager") {
			id = "net.luis.lm"
			implementationClass = "net.luis.lm.LicenseManager"
			displayName = "License Manager Plugin"
			description = "A plugin to manage licenses in source files"
		}
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			
			artifactId = "lm"
		}
	}
	repositories {
		if (mavenUsername != null && mavenPassword != null) {
			maven {
				url = uri("https://maven.luis-st.net/plugins/")
				credentials {
					username = mavenUsername
					password = mavenPassword
				}
			}
		} else {
			System.err.println("No credentials provided. Publishing to maven.luis-st.net not possible.")
		}
	}
}
