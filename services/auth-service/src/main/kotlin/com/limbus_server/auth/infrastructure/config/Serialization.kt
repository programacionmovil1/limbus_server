package com.limbus_server.auth.infrastructure.config // Paquete corregido

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

// Esta función configura el plugin ContentNegotiation para manejar la serialización/deserialización
fun Application.configureSerialization() {
    // Instala el plugin ContentNegotiation
    install(ContentNegotiation) {
        // Configura el serializador para usar JSON con kotlinx.serialization
        // Esto permite que Ktor convierta automáticamente tus data classes
        // a JSON en las respuestas y de JSON a tus data classes en las peticiones.
        json()
    }

    // Las rutas que manejan JSON (como tus endpoints de login/registro)
    // se definirán en la capa de routing (infrastructure/routing).
    // Este archivo solo se encarga de la configuración del serializador.
}
