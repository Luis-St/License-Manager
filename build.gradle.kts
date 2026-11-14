import net.luis.lm.LineEnding
import java.time.Year
import java.util.Properties

plugins {
	kotlin("jvm") version "2.4.21"
	id("java-gradle-plugin")
	id("maven-publish")
	id("net.luis.lm") version "1.2.0"
}

group = "net.luis"
version = System.getenv("VERSION")?.removePrefix("v") ?: "1.2.0"

// The publish workflow passes the token as environment variable, local builds read it from credentials.properties
var artifactoryToken: String? = System.getenv("ARTIFACTORY_TOKEN")
val file: File = rootProject.file("./../credentials.properties")
if (artifactoryToken == null && file.exists()) {
	val properties = Properties()
	file.inputStream().use { properties.load(it) }
	
	artifactoryToken = properties.getProperty("token")
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	implementation(gradleApi())
	implementation(localGroovy())
}

kotlin {
	jvmToolchain(25)
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
		if (artifactoryToken != null) {
			maven {
				url = uri("https://artifact.luis-st.net/maven/plugins/")
				credentials {
					username = "__token__"
					password = artifactoryToken
				}
			}
		} else {
			System.err.println("No credentials provided. Publishing to artifact.luis-st.net not possible.")
		}
	}
}
