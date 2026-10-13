pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (providers.gradleProperty("composeSmokePublished").orNull == "true") {
            maven { url = uri(file("build/p0-maven-repository")) }
        }
    }
}

rootProject.name = "ComposeDesignSystem"
include(":design-system")
include(":gallery")
