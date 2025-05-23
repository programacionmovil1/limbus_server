package com.limbus_server.auth

import com.limbus_server.auth.infrastructure.config.*
import com.limbus_server.auth.infrastructure.database.configureDatabases
import com.limbus_server.auth.infrastructure.routing.configureRouting
import com.limbus_server.auth.infrastructure.security.TokenService
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger
import org.koin.ktor.ext.inject
import org.koin.dsl.module // Importación necesaria para 'module'

fun main(args: Array<String>) {
    EngineMain.main(args) // Punto de entrada principal para el servidor Ktor
}

// Función de extensión para Application, donde se configuran todos los plugins y rutas.
fun Application.module() {
    // 1. Configurar la base de datos.
    // Esto debe hacerse ANTES de configurar Koin si la instancia de Database
    // se va a inyectar a través de Koin.
    val database = configureDatabases() // Obtiene la instancia de Database devuelta por la función

    // 2. Configurar Koin para la inyección de dependencias.
    // Esto debe hacerse al principio para que otras configuraciones puedan usar 'inject()'.
    install(Koin) {
        SLF4JLogger() // Configura el logger de Koin (opcional, pero recomendado)
        // Carga tu módulo de Koin y AÑADE la instancia de Database y ApplicationConfig como singletons
        modules(authModule + module {
            single { database } // Registra la instancia de Database
            single { environment.config } // Registra la ApplicationConfig de Ktor
            single { environment } // Registra ApplicationEnvironment
        })
    }

    // 3. Inyectar servicios necesarios para las configuraciones.
    // El TokenService se inyecta aquí porque configureSecurity lo necesita.
    val tokenService by inject<TokenService>()

    // 4. Configurar otros plugins.
    // El orden puede importar si hay dependencias entre plugins.
    configureSerialization() // Para JSON (ContentNegotiation)
    configureMonitoring() // Para CallLogging y Micrometer
    configureAdministration() // Para Rate Limiting
    configureHTTP() // Para CORS, OpenAPI, Swagger UI

    // 5. Configurar la seguridad (Autenticación JWT).
    // Se le pasa la instancia de TokenService inyectada.
    configureSecurity(tokenService)

    // 6. Configurar las rutas.
    // Las rutas suelen ir al final después de que todos los plugins necesarios estén configurados.
    configureRouting()
}
