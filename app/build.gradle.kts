import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val supabase = Properties().apply {
    val file = rootProject.file("supabase.properties")
    if (file.exists()) file.inputStream().use(::load)
}
val local = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}
fun quoted(value: String) = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "ru.chipmarket.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.chipmarket.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "1.7"
        buildConfigField("String", "SUPABASE_URL", quoted(supabase.getProperty("SUPABASE_URL", "https://sqmgejzbbysxblgzcqyg.supabase.co")))
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", quoted(supabase.getProperty("SUPABASE_PUBLISHABLE_KEY", "")))
        buildConfigField("String", "DGIS_MAPGL_KEY", quoted(local.getProperty("DGIS_MAPGL_KEY", "")))
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.7.0")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
