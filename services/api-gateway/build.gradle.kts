// api-gateway/build.gradle.kts

plugins {
    // Asumo que tienes estos alias definidos en tu settings.gradle.kts o en el catálogo de versiones (libs.versions.toml)
    // Si no, usa la forma directa: id("org.jetbrains.kotlin.jvm") version "..."
    alias(libs.plugins.kotlin.jvm)
    // El plugin "ktor" del catálogo a menudo es para ayudar con la configuración en IntelliJ o tareas específicas.
    // No es estrictamente necesario para compilar y ejecutar si configuras 'application' y las dependencias manualmente.
    // alias(libs.plugins.ktor) // Puedes omitirlo si no lo usas directamente para configuración especial del gateway.
    alias(libs.plugins.kotlin.plugin.serialization) // Necesario para kotlinx.serialization
    application // Para poder ejecutar y empaquetar la aplicación
}

group = "com.limbus_server.gateway" // Específico para este módulo
version = "0.0.1" // Específico para este módulo

application {
    mainClass.set("com.limbus_server.gateway.ApplicationKt") // Ajusta a tu paquete y nombre de archivo principal
}

repositories {
    mavenCentral()
    // maven { url = uri("https://packages.confluent.io/maven/") } // Solo si el API Gateway necesita algo de Confluent
}

dependencies {
    // --- Ktor Server Core ---
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty) // O el motor que prefieras (cio, tomcat, jetty)
    implementation(libs.ktor.server.host.common) // Útil para configuraciones de host

    // --- Para enrutar y manejar peticiones HTTP ---
    // Routing ya viene con ktor-server-core, pero es bueno tenerlo en mente.

    // --- Serialización y Negociación de Contenido (para errores o payloads que el gateway maneje) ---
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)

    // --- Ktor Client (CRUCIAL para llamar a otros microservicios) ---
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.apache) // O cio, jetty, etc. para el motor del cliente
    // Si los otros servicios también usan kotlinx.serialization, querrás el plugin de cliente para ello:
    implementation(libs.ktor.client.content.negotiation) // Para que el cliente pueda manejar JSON

    // --- Logging ---
    implementation(libs.ktor.server.call.logging) // Para loguear las peticiones entrantes/salientes
    implementation(libs.logback.classic) // Implementación de SLF4J

    // --- Manejo de Errores y Status ---
    implementation(libs.ktor.server.status.pages)

    // --- Seguridad ---
    implementation(libs.ktor.server.cors) // Muy importante para un API Gateway
    // Si el API Gateway va a validar tokens JWT (recomendado)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    // NO necesitaría `ktor-server-sessions` o `ktor-server-sse` a menos que el gateway tenga esa lógica específica.

    // --- Métricas (bueno para observar el rendimiento del gateway) ---
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus) // O tu registrador de métricas preferido

    // --- Documentación de la API (el gateway expone la API consolidada o su propia API) ---
    implementation(libs.ktor.server.openapi) // Si quieres exponer una definición OpenAPI
    implementation(libs.ktor.server.swagger) // Para servir la UI de Swagger

    // --- Inyección de Dependencias (si la usas en el gateway, ej: para inyectar clientes HTTP) ---
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j) // Opcional

    // --- Configuración (si usas YAML en lugar de HOCON por defecto) ---
     implementation(libs.ktor.server.config.yaml) // Solo si usas application.yaml

    // --- Testing ---
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)


    // === DEPENDENCIAS QUE PROBABLEMENTE NO NECESITAS EN EL API GATEWAY ===
    // (Comentadas o eliminadas del original)
    // implementation(libs.ktor.server.rate.limiting) // Podría ser, pero a menudo se maneja en soluciones de API Gateway más robustas o con plugins específicos. Si lo necesitas, descomenta.
    // implementation(libs.ktor.server.websockets) // Solo si el gateway proxy pasarelas WebSocket
    // implementation(libs.postgresql) // El gateway no debería acceder directamente a BDs de otros servicios
    // implementation(libs.h2) // Para testing de BD, no aplica aquí
    // implementation(libs.exposed.core) // No maneja persistencia de datos de negocio
    // implementation(libs.exposed.jdbc) // Idem
    // implementation(libs.ktor.server.metrics) // `ktor-server-metrics-micrometer` es más específico y preferido
    // implementation(libs.ktor.server.sse) // A menos que el gateway maneje SSE directamente
    // implementation(libs.ktor.server.sessions) // Las sesiones suelen manejarse en el servicio de autenticación o frontend
    // implementation(project(":")) // Esto es una dependencia a sí mismo, probablemente un error en el original o configuración específica. No lo necesitas en un módulo de microservicio.
}