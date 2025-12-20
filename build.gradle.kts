// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("com.android.library") version "8.9.1" apply false

    //  id("dev.iurysouza.modulegraph") version "0.10.0"
}

/*
moduleGraphConfig {
    readmePath.set("./README.md")
    heading = "### Module Graph"
    setStyleByModuleType.set(true)
    theme.set(Theme.DARK)
}
*/
