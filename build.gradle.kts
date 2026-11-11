// Top-level build file. Shared plugin versions come from gradle/libs.versions.toml.
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

// Every Android module gets detekt + ktlint with the shared config, so quality gates are the same everywhere.
subprojects {
    pluginManager.withPlugin("com.android.base") {
        apply(plugin = "io.gitlab.arturbosch.detekt")
        apply(plugin = "org.jlleitschuh.gradle.ktlint")

        extensions.configure<DetektExtension> {
            config.setFrom(files("${rootProject.projectDir}/detekt.yml"))
            buildUponDefaultConfig = true
        }
        extensions.configure<KtlintExtension> {
            android.set(true)
            ignoreFailures.set(true)
        }
    }
}

tasks.register("checkCode") {
    group = "verification"
    description = "Runs detekt on all Android modules"
}

tasks.register("checkKtlint") {
    group = "verification"
    description = "Runs ktlintCheck on all Android modules"
}

gradle.projectsEvaluated {
    val detektTasks = subprojects.mapNotNull { it.tasks.findByName("detekt")?.path }
    val ktlintTasks = subprojects.mapNotNull { it.tasks.findByName("ktlintCheck")?.path }

    tasks.named("checkCode") { dependsOn(detektTasks) }
    tasks.named("checkKtlint") { dependsOn(ktlintTasks) }
}
