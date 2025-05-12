package com.limbus_server.auth.infrastructure.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    install(StatusPages) {
        // Maneja cualquier excepción no capturada y responde con un error 500 Internal Server Error
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
            // Considera loguear la excepción aquí para debugging
            // call.application.log.error("Unhandled exception", cause)
        }
        // Puedes añadir manejo para otros códigos de estado si es necesario, ej:
        // status(HttpStatusCode.NotFound) { call, status ->
        //     call.respondText(text = "404: Page Not Found", status = status)
        // }
    }

    // Define el bloque principal de routing
    routing {
        // Aquí se definirán las rutas específicas de autenticación.
        // Es buena práctica agrupar rutas relacionadas.
        // Por ejemplo, puedes usar una ruta base como /auth

        // Define las rutas de autenticación dentro de un bloque route
        route("/auth") {
            // Ejemplo de ruta para registro de usuario
            // Debería recibir un DTO de registro y llamar a un servicio de aplicación
            post("/register") {
                // TODO: Implementar lógica de registro
                // 1. Recibir y validar el DTO de registro (ej: RegisterRequest)
                // val registerRequest = call.receive<RegisterRequest>()
                // 2. Llamar al servicio de aplicación de autenticación para registrar al usuario
                // val userId = authService.registerUser(registerRequest.toDomainModel())
                // 3. Responder con el resultado (ej: ID del usuario creado, o un DTO de éxito)
                // call.respond(HttpStatusCode.Created, mapOf("userId" to userId))
                call.respondText("Endpoint de registro de usuario", status = HttpStatusCode.OK) // Placeholder
            }

            // Ejemplo de ruta para inicio de sesión
            // Debería recibir un DTO de login y devolver tokens (JWT)
            post("/login") {
                // TODO: Implementar lógica de login
                // 1. Recibir y validar el DTO de login (ej: LoginRequest)
                // val loginRequest = call.receive<LoginRequest>()
                // 2. Llamar al servicio de aplicación para autenticar al usuario y generar tokens
                // val tokens = authService.loginUser(loginRequest.email, loginRequest.password)
                // 3. Responder con los tokens (ej: AuthResponse con accessToken y refreshToken)
                // call.respond(HttpStatusCode.OK, tokens)
                call.respondText("Endpoint de inicio de sesión", status = HttpStatusCode.OK) // Placeholder
            }

            // Ejemplo de ruta para refrescar tokens
            // Debería recibir un refresh token y devolver un nuevo access token (y quizás refresh token)
            post("/refresh-token") {
                // TODO: Implementar lógica de refresh token
                // 1. Recibir el refresh token (probablemente en el cuerpo de la petición o una cookie)
                // val refreshToken = call.receive<RefreshTokenRequest>().token
                // 2. Llamar al servicio de aplicación para validar el refresh token y generar nuevos tokens
                // val newTokens = authService.refreshAccessToken(refreshToken)
                // 3. Responder con los nuevos tokens
                // call.respond(HttpStatusCode.OK, newTokens)
                call.respondText("Endpoint para refrescar tokens", status = HttpStatusCode.OK) // Placeholder
            }

            // Puedes añadir otras rutas relacionadas con autenticación aquí, ej:
            // post("/forgot-password") { ... }
            // post("/reset-password") { ... }
            // get("/verify-email") { ... }
        }

        // Si necesitas alguna otra ruta general para el servicio de auth que no sea /auth, defínela aquí.
        // Por ejemplo, un health check simple:
        get("/health") {
            call.respondText("Auth Service is healthy!", status = HttpStatusCode.OK)
        }
    }
}