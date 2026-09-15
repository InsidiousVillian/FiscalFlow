// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // KSP is used by Room to generate DAO/database implementations at build time
    alias(libs.plugins.ksp) apply false
}
