package com.limbus_server.auth.infrastructure.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        swaggerUI(path = "openapi")
    }

    // Configuración de CORS (Cross-Origin Resource Sharing)
    // Permite que tu frontend (u otros orígenes) hagan peticiones a este servicio.
    // !!! ADVERTENCIA: anyHost() permite peticiones desde cualquier origen.
    // En producción, DEBERÍAS restringir esto a los dominios específicos de tu frontend.
    install(CORS) {
        allowMethod(HttpMethod.Options) // Permitir método OPTIONS (necesario para preflight requests de CORS)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization) // Permitir el header Authorization (necesario para JWT)
        allowHeader("MyCustomHeader") // Permitir cualquier otro header personalizado que uses
        anyHost() // @TODO: No usar anyHost() en producción. Limitar a orígenes específicos.
    }

    // Configuración de OpenAPI para generar la especificación de la API
    // La especificación estará disponible en /openapi.yaml (por defecto)
    routing {
        openAPI(path = "openapi") // Genera la especificación OpenAPI en /openapi.yaml
    }
}
