package com.limbus_server.auth.infrastructure.config

import com.limbus_server.auth.application.service.AuthService
import com.limbus_server.auth.domain.repository.*
import com.limbus_server.auth.domain.service.PasswordHasher
import com.limbus_server.auth.domain.service.GoogleAuthClient
import com.limbus_server.auth.infrastructure.repository.*
import com.limbus_server.auth.infrastructure.security.PasswordHasherImpl
import com.limbus_server.auth.infrastructure.security.TokenService
import com.limbus_server.auth.infrastructure.security.GoogleAuthClientImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.application.ApplicationEnvironment
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.core.parameter.parametersOf


// Define el módulo de Koin para la inyección de dependencias de tu servicio de autenticación.
val authModule = module {
    // --- Repositorios (Implementaciones de la capa de infraestructura) ---
    // Singletons para las implementaciones de los repositorios.
    // get() resuelve automáticamente las dependencias declaradas en el constructor de la clase.
    // Ahora, los repositorios reciben la instancia de Database a través de Koin.
    single<UserRepository> { UserRepositoryImpl(get()) } // Inyecta Database
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl(get()) } // Inyecta Database
    single<VerificationTokenRepository> { VerificationTokenRepositoryImpl(get()) } // Inyecta Database
    single<PasswordResetTokenRepository> { PasswordResetTokenRepositoryImpl(get()) } // Inyecta Database

    // --- Servicios de Dominio (Implementaciones de la capa de infraestructura) ---
    single<PasswordHasher> { PasswordHasherImpl() }

    // --- Ktor HttpClient (para comunicaciones externas, ej. con Google) ---
    // Proporciona una instancia de HttpClient configurada para usar JSON.
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Ignora campos desconocidos en las respuestas JSON
                    isLenient = true // Permite JSONs menos estrictos
                })
            }
            // Puedes añadir otros plugins aquí, como logging para el cliente HTTP
            // install(HttpLogging) {
            //     level = io.ktor.client.plugins.logging.LogLevel.ALL
            // }
        }
    }

    // --- Cliente de Autenticación de Google ---
    single<GoogleAuthClient> { (environment: ApplicationEnvironment) -> // 'environment' es el parámetro que Koin proporciona a esta lambda
        GoogleAuthClientImpl(
            httpClient = get(), // Koin inyecta la instancia de HttpClient
            googleClientId = environment.config.property("google.client_id").getString(), // Usar 'googleClientId' como nombre de parámetro
            environment = environment // Pasar la instancia de ApplicationEnvironment directamente
        )
    }

    // --- Servicios de Seguridad (Implementaciones de la capa de infraestructura) ---
    single {
        // Obtenemos la configuración de la aplicación para pasarla al TokenService
        val appConfig = get<ApplicationConfig>() // Koin puede inyectar ApplicationConfig automáticamente

        // Leemos las propiedades JWT y de expiración de tokens desde la configuración
        val jwtAudience = appConfig.property("jwt.audience").getString()
        val jwtIssuer = appConfig.property("jwt.issuer").getString()
        val jwtSecret = appConfig.property("jwt.secret").getString() // ¡CUIDADO con el secreto en producción!

        val accessTokenExpirationMillis = appConfig.property("token.access-token-expiration-millis").getString().toLong()
        val refreshTokenExpirationMillis = appConfig.property("token.refresh-token-expiration-millis").getString().toLong()
        val verificationTokenExpirationMillis = appConfig.property("token.verification-token-expiration-millis").getString().toLong()
        val passwordResetTokenExpirationMillis = appConfig.property("token.password-reset-token-expiration-millis").getString().toLong()

        // Creamos la instancia de TokenService, inyectando sus dependencias
        TokenService(
            jwtAudience = jwtAudience,
            jwtIssuer = jwtIssuer,
            jwtSecret = jwtSecret,
            accessTokenExpirationMillis = accessTokenExpirationMillis,
            refreshTokenExpirationMillis = refreshTokenExpirationMillis,
            verificationTokenExpirationMillis = verificationTokenExpirationMillis,
            passwordResetTokenExpirationMillis = passwordResetTokenExpirationMillis,
            refreshTokenRepository = get(), // Koin inyecta la implementación de RefreshTokenRepository
            verificationTokenRepository = get(), // Koin inyecta la implementación de VerificationTokenRepository
            passwordResetTokenRepository = get() // Koin inyecta la implementación de PasswordResetTokenRepository
        )
    }

    // --- Servicios de Aplicación ---
    single {
        // Creamos la instancia de AuthService, inyectando sus dependencias
        AuthService(
            userRepository = get(), // Koin inyecta la implementación de UserRepository
            passwordHasher = get(), // Koin inyecta la implementación de PasswordHasher
            tokenService = get(), // Koin inyecta la instancia de TokenService
            googleAuthClient = get { parametersOf(get<ApplicationEnvironment>()) }
            // Si el AuthService necesitase EmailService, lo inyectaríamos aquí también: emailService = get()
        )
    }

    // Puedes añadir aquí otros singletons o factories si los necesitas
}
