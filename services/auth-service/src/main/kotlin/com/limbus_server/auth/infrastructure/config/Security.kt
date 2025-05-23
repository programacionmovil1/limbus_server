package com.limbus_server.auth.infrastructure.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.limbus_server.auth.application.dto.response.ErrorResponse
import com.limbus_server.auth.infrastructure.security.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.config.ApplicationConfig

// Esta función configura los plugins relacionados con la seguridad de la aplicación.
fun Application.configureSecurity(tokenService: TokenService) { // Recibe TokenService como parámetro

    // Captura la configuración de la aplicación FUERA del bloque de autenticación.
    // Esto resuelve el error de "implicit receiver".
    val appConfig: ApplicationConfig = environment.config

    // Configuración del plugin de autenticación de Ktor.
    // Aquí definimos cómo Ktor debe validar los tokens JWT entrantes.
    install(Authentication) {
        // Configuración del esquema de autenticación JWT.
        jwt {
            // Lee las propiedades JWT desde el archivo de configuración (application.yaml).
            // Asegúrate de que estas propiedades estén definidas en tu application.yaml bajo la sección 'jwt'.
            val jwtAudience = appConfig.property("jwt.audience").getString() // Usa appConfig
            val jwtIssuer = appConfig.property("jwt.issuer").getString()     // Usa appConfig
            val jwtSecret = appConfig.property("jwt.secret").getString()     // Usa appConfig
            val jwtRealm = appConfig.property("jwt.realm").getString()       // Usa appConfig

            realm = jwtRealm // El "realm" es un identificador para este esquema de autenticación.

            // Define el verificador de JWT.
            // Este verificador se encarga de validar la firma del token y sus claims básicos (audiencia, emisor, expiración).
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret)) // Usa el algoritmo HMAC256 con la clave secreta.
                    .withAudience(jwtAudience) // Verifica que el token esté destinado a esta audiencia.
                    .withIssuer(jwtIssuer) // Verifica que el token fue emitido por este emisor.
                    .build()
            )

            // Bloque 'validate': Se ejecuta DESPUÉS de que el verifier ha validado la firma y claims básicos.
            // Aquí puedes realizar validaciones adicionales sobre el payload del token.
            validate { credential ->
                // El 'credential.payload' contiene los claims decodificados del JWT.
                // Verificamos si la audiencia del token incluye la audiencia esperada.
                if (credential.payload.audience.contains(jwtAudience)) {
                    // Si la audiencia es correcta, creamos un JWTPrincipal.
                    // El JWTPrincipal hará que la información del usuario esté disponible en el 'call.principal<JWTPrincipal>()'.
                    // Puedes extraer el userId del payload aquí y usarlo para crear un Principal personalizado
                    // si necesitas más que solo el payload crudo del JWT.
                    // val userId = credential.payload.getClaim("userId").asInt()
                    // return UserPrincipal(userId) // Si tienes un UserPrincipal personalizado

                    JWTPrincipal(credential.payload) // Devuelve el principal si es válido
                } else {
                    null // Si la validación falla, devuelve null.
                }
            }

            // Bloque 'challenge': Se ejecuta cuando la autenticación falla (ej: token ausente, inválido o expirado).
            // Aquí puedes personalizar la respuesta al cliente.
            challenge { defaultScheme, realm ->
                // Cuando la autenticación JWT falla, el JWT verifier de Auth0/Ktor
                // puede lanzar una excepción interna. Aquí la interceptamos para dar una respuesta amigable.
                // Es común que el plugin JWT maneje la expiración internamente y active este challenge.

                // Puedes intentar ser más granular aquí si el error es por expiración vs. invalidez.
                // Sin embargo, para simplificar, a menudo se devuelve un 401 genérico.
                // Si quieres distinguir, podrías necesitar una lógica más compleja o un interceptor.

                // Por ahora, devolvemos un 401 Unauthorized con un mensaje de error genérico.
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(message = "Authentication failed: Invalid or missing token", errorCode = 401)
                )
                // Opcional: Si quieres lanzar tus excepciones personalizadas para que StatusPages las maneje:
                // throw InvalidTokenException("Authentication failed: Invalid or missing token")
                // throw TokenExpiredException("Authentication failed: Token has expired")
            }
        }
    }
}