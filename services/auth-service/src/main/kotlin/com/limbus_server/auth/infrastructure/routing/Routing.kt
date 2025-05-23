package com.limbus_server.auth.infrastructure.routing

import com.limbus_server.auth.application.dto.request.*
import com.limbus_server.auth.application.dto.response.*
import com.limbus_server.auth.application.exception.*
import com.limbus_server.auth.application.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

// Esta función configura el routing principal y los plugins relacionados con rutas
fun Application.configureRouting() {

    // Inyecta el AuthService usando Koin
    val authService by inject<AuthService>()

    // Configuración de StatusPages para manejar excepciones y errores HTTP
    // Esto proporciona respuestas consistentes para diferentes códigos de estado o excepciones.
    install(StatusPages) {
        // Manejo de excepciones personalizadas de negocio
        exception<UserAlreadyExistsException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, ErrorResponse(message = cause.message ?: "User already exists"))
            call.application.log.warn("UserAlreadyExistsException: ${cause.message}")
        }
        exception<InvalidCredentialsException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = cause.message ?: "Invalid credentials"))
            call.application.log.warn("InvalidCredentialsException: ${cause.message}")
        }
        exception<UserNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(message = cause.message ?: "User not found"))
            call.application.log.warn("UserNotFoundException: ${cause.message}")
        }
        exception<InvalidTokenException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = cause.message ?: "Invalid token"))
            call.application.log.warn("InvalidTokenException: ${cause.message}")
        }
        exception<TokenExpiredException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = cause.message ?: "Token expired"))
            call.application.log.warn("TokenExpiredException: ${cause.message}")
        }
        exception<EmailNotVerifiedException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = cause.message ?: "Email not verified"))
            call.application.log.warn("EmailNotVerifiedException: ${cause.message}")
        }
        exception<AccountLockedException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = cause.message ?: "Account locked"))
            call.application.log.warn("AccountLockedException: ${cause.message}")
        }

        // Maneja cualquier otra excepción no capturada y responde con un error 500 Internal Server Error
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: Internal Server Error - ${cause.message}" , status = HttpStatusCode.InternalServerError)
            call.application.log.error("Unhandled exception", cause) // Loguea la excepción para debugging
        }

        // Puedes añadir manejo para otros códigos de estado si es necesario, ej:
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(text = "404: Page Not Found", status = status)
        }
    }

    // Define el bloque principal de routing
    routing {
        // Ruta raíz que responde con un mensaje de bienvenida
        get("/") {
            call.respondText("Bienvenido a Auth Service!", status = HttpStatusCode.OK)
        }

        // Define las rutas de autenticación dentro de un bloque route
        route("/auth") {
            // Ruta para registro de usuario
            post("/register") {
                val request = call.receive<RegisterRequest>()
                val response = authService.registerUser(request.email, request.password, request.name)
                call.respond(HttpStatusCode.Created, response)
            }

            // Ruta para inicio de sesión con credenciales tradicionales
            post("/login") {
                val request = call.receive<LoginRequest>()
                val response = authService.loginUser(request.email, request.password)
                call.respond(HttpStatusCode.OK, response)
            }

            // Ruta para iniciar sesión con Google (OAuth/OIDC)
            post("/google-login") {
                val request = call.receive<GoogleLoginRequest>() // Recibe el ID Token de Google
                val response = authService.loginWithGoogle(request.idToken) // Llama al servicio para procesar el login con Google
                call.respond(HttpStatusCode.OK, response) // Responde con los tokens de tu sistema
            }

            // Ruta para refrescar tokens
            post("/refresh-token") {
                val request = call.receive<RefreshTokenRequest>()
                val response = authService.refreshAccessToken(request.refreshToken)
                call.respond(HttpStatusCode.OK, response)
            }

            // Ruta para iniciar el proceso de "Olvidé mi contraseña"
            post("/forgot-password") {
                val request = call.receive<ForgotPasswordRequest>()
                val response = authService.initiatePasswordReset(request.email)
                call.respond(HttpStatusCode.OK, response)
            }

            // Ruta para restablecer la contraseña
            post("/reset-password") {
                val request = call.receive<ResetPasswordRequest>()
                authService.resetPassword(request.resetToken, request.newPassword)
                call.respond(HttpStatusCode.OK, SuccessResponse("Password has been reset successfully."))
            }

            // Ruta para verificar el correo electrónico
            post("/verify-email") {
                val request = call.receive<VerifyEmailRequest>()
                authService.verifyEmail(request.verificationToken)
                call.respond(HttpStatusCode.OK, SuccessResponse("Email verified successfully."))
            }

            // Puedes añadir otras rutas relacionadas con autenticación aquí.
        }

        // Health check simple para el servicio
        get("/health") {
            call.respondText("Auth Service is healthy!", status = HttpStatusCode.OK)
        }
    }
}
