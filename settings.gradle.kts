pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HogwartsAndroid"

// Main app module
include(":app")

// Core modules
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:network")
include(":core:designsystem")
include(":core:security")

// Feature modules
include(":feature:auth")
include(":feature:dashboard")
include(":feature:students")
include(":feature:attendance")
include(":feature:grades")
include(":feature:fees")
include(":feature:timetable")
include(":feature:messaging")
include(":feature:settings")
include(":feature:exams")
include(":feature:profile")
include(":feature:notifications")
include(":feature:guardian")
include(":feature:teacher")
include(":feature:announcements")
include(":feature:events")
include(":feature:admission")
include(":feature:library")
include(":feature:subjects")
include(":feature:report-cards")
include(":feature:stream")
include(":feature:lessons")
include(":feature:admin")
include(":feature:idcard")
include(":feature:quizgame")

// New core modules
include(":core:sync")
include(":core:push")
include(":core:connectivity")
