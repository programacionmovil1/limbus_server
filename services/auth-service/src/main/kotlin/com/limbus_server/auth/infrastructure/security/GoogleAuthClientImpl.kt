package com.limbus_server.auth.infrastructure.security

import com.limbus_server.auth.domain.model.GoogleUser
import com.limbus_server.auth.domain.service.GoogleAuthClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.ApplicationEnvironment

// DTO interno para la respuesta de verificación del token de Google
// Google envía una respuesta JSON con varias claims JWT
@Serializable
data class GoogleTokenInfoResponse(
    val sub: String, // ID de usuario de Google
    val email: String,
    val name: String? = null,
    val picture: String? = null,
    val aud: String, // Audiencia: el Client ID de tu aplicación
    val iss: String, // Emisor: accounts.google.com
    val exp: Long // Tiempo de expiración del token (timestamp de Unix)
    // Otras claims como "iat", "hd", etc., pueden ser ignoradas o añadidas según necesidad.
)

// Implementación concreta de la interfaz GoogleAuthClient
class GoogleAuthClientImpl(
    private val httpClient: HttpClient, // Ktor HttpClient para hacer solicitudes HTTP
    private val googleClientId: String, // El Client ID de tu aplicación web/Android de Google
    private val environment: ApplicationEnvironment // Para acceder a las propiedades de configuración
) : GoogleAuthClient {

    // URL del endpoint de verificación de tokens de Google
    private val GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo"

    // Constructor secundario para facilitar la inyección de Koin con HttpClient preconfigurado
    // Asume que el httpClient ya tiene configurado ContentNegotiation
    constructor(googleClientId: String, environment: ApplicationEnvironment) : this(
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Importante para no fallar si Google añade nuevas propiedades
                    isLenient = true
                })
            }
        },
        googleClientId = googleClientId,
        environment = environment
    )

    override suspend fun verifyIdToken(idToken: String): GoogleUser? {
        return try {
            val response = httpClient.get(GOOGLE_TOKEN_INFO_URL) {
                parameter("id_token", idToken)
            }

            if (response.status == HttpStatusCode.OK) {
                val tokenInfo = response.body<GoogleTokenInfoResponse>()

                // Validar la audiencia (aud) y el emisor (iss) del token
                // El 'aud' debe ser tu Google Client ID
                // El 'iss' debe ser 'https://accounts.google.com' o 'accounts.google.com'
                if (tokenInfo.aud == googleClientId &&
                    (tokenInfo.iss == "https://accounts.google.com" || tokenInfo.iss == "accounts.google.com") &&
                    tokenInfo.email.isNotEmpty() // Asegurar que el email no esté vacío
                ) {
                    GoogleUser(
                        id = tokenInfo.sub,
                        email = tokenInfo.email,
                        name = tokenInfo.name,
                        picture = tokenInfo.picture
                    )
                } else {
                    // Token inválido (audiencia o emisor no coinciden)
                    environment.log.warn("Google ID Token validation failed: Audience mismatch or invalid issuer for token $idToken")
                    null
                }
            } else {
                // La solicitud a Google no fue exitosa
                environment.log.warn("Failed to verify Google ID Token. Status: ${response.status}. Body: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            // Error durante la comunicación o deserialización
            environment.log.error("Error verifying Google ID Token: ${e.message}", e)
            null
        }
    }
}

// Notas importantes:
// - Esta implementación asume que ya tienes configurado un Http Client en tu aplicación.
//   Si no lo tienes, el constructor secundario crea uno básico. Idealmente,
//   el HttpClient se inyectaría como una dependencia para mayor control.
// - Necesitarás obtener el 'googleClientId' de alguna manera, por ejemplo,
//   de las configuraciones de tu aplicación (application.yaml o variables de entorno).
// - La verificación del token de Google es crucial. Asegúrate de que las validaciones de 'aud' e 'iss'
//   sean correctas para tu caso de uso.
// - La gestión de errores y logging es básica. Deberías expandirla para producción.
// - La URL GOOGLE_TOKEN_INFO_URL es para verificar el ID Token. Si usas el flujo de código de autorización,
//   necesitarías el endpoint /oauth2/v4/token para intercambiar el código por tokens.
