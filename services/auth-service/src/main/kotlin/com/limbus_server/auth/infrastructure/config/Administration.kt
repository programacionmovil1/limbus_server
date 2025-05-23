package com.limbus_server.auth.infrastructure.config

//import io.github.flaxoos.ktor.server.plugins.ratelimiter.*
//import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.seconds // Import para usar 10.seconds

fun Application.configureAdministration() {
    // Configuración de Rate Limiting (Limitación de tasa de peticiones)
    // Esto ayuda a proteger contra ataques de fuerza bruta o abuso de la API.
    // La configuración actual aplica Rate Limiting a todas las rutas bajo la raíz ("/").
    // Considera aplicar esto de forma más granular a endpoints específicos como /login.
    routing {
        // Puedes aplicar Rate Limiting a rutas específicas así:
        // route("/login") {
        //     install(RateLimiting) {
        //         rateLimiter {
        //             type = TokenBucket::class
        //             capacity = 10 // Ejemplo: 10 intentos de login por período
        //             rate = 60.seconds // Ejemplo: por minuto
        //         }
        //     }
        //     // ... tus handlers de ruta para /login ...
        // }
    }

    // Si tu servicio de autenticación tuviera otras configuraciones de administración
    // (que no sean HTTP, seguridad, etc.), irían aquí.
}
