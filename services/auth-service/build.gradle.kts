// auth-service/build.gradle.kts

plugins {
    // Plugins definidos en tu libs.versions.toml
    alias(libs.plugins.kotlin.jvm)
    // Puedes omitir el plugin 'ktor' si no lo usas directamente para configuración especial
    // alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization) // Necesario para kotlinx.serialization
    application // Para poder ejecutar y empaquetar la aplicación
}

group = "com.limbus_server.auth" // Específico para este módulo
version = "0.0.1" // Específico para este módulo

application {
    // Ajusta a tu paquete y nombre de archivo principal del servicio de autenticación
//    mainClass.set("com.limbus_server.auth.ApplicationKt")
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    // Agrega repositorios adicionales si tus dependencias lo requieren (ej: para Exposed snapshot si usas una versión no estable)
}

dependencies {
    // --- Ktor Server Core (usando el catálogo) ---
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.host.common) // Útil para configuraciones de host

    // --- Serialización y Negociación de Contenido (usando el catálogo) ---
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)

    // --- Logging (usando el catálogo) ---
    implementation(libs.ktor.server.call.logging) // Para loguear las peticiones
    implementation(libs.logback.classic) // Implementación de SLF4J

    // --- Manejo de Errores y Status (usando el catálogo) ---
    implementation(libs.ktor.server.status.pages)

    // --- Seguridad (CRUCIAL para auth-service) (usando el catálogo) ---
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt) // Para manejar la autenticación JWT
    implementation(libs.jbcrypt)

    // --- Base de Datos (si el servicio de auth gestiona usuarios y credenciales) (usando el catálogo) ---
    implementation(libs.postgresql) // Driver de PostgreSQL
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.kotlinx.datetime)

    implementation(libs.hikari.cp)

    // --- Koin (Inyección de Dependencias, si la usas en este servicio) (usando el catálogo) ---
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // --- Configuración (si usas YAML) (usando el catálogo) ---
    implementation(libs.ktor.server.config.yaml)

    // --- CORS (necesario si tu frontend está en un origen diferente) (usando el catálogo) ---
    implementation(libs.ktor.server.cors)

    // --- OpenAPI y Swagger UI (para documentación de la API de este servicio) (usando el catálogo) ---
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)

    // --- Monitoring (Micrometer y Prometheus) (usando el catálogo) ---
    implementation(libs.ktor.server.metrics.micrometer) // Plugin de Ktor para integrar Micrometer
    implementation(libs.micrometer.registry.prometheus) // Registro de Prometheus para Micrometer

    // --- Testing (usando el catálogo) ---
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    // Si usas Koin para testing
    testImplementation(libs.koin.test)
    // testImplementation(libs.koin.test.junit4) // O junit5 si usas JUnit 5 -> Asegúrate de que esté en libs.versions.toml
}