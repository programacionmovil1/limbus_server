package com.limbus_server.auth.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.limbus_server.auth.domain.model.PasswordResetToken
import com.limbus_server.auth.domain.model.RefreshToken
import com.limbus_server.auth.domain.model.VerificationToken
import com.limbus_server.auth.domain.repository.PasswordResetTokenRepository
import com.limbus_server.auth.domain.repository.RefreshTokenRepository
import com.limbus_server.auth.domain.repository.VerificationTokenRepository
import com.limbus_server.auth.application.exception.InvalidTokenException
import com.limbus_server.auth.application.exception.TokenExpiredException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import java.util.Date
import java.util.UUID

// Servicio de infraestructura para manejar la generación y validación de todos los tipos de tokens.
// Esta clase encapsula los detalles de implementación de los tokens, como el algoritmo JWT
// y la interacción con los repositorios para tokens persistidos.
class TokenService(
    // Propiedades de configuración de JWT (obtenidas de application.yaml a través de Koin)
    private val jwtAudience: String,
    private val jwtIssuer: String,
    private val jwtSecret: String,
    private val accessTokenExpirationMillis: Long, // Tiempo de expiración del Access Token en milisegundos
    private val refreshTokenExpirationMillis: Long, // Tiempo de expiración del Refresh Token en milisegundos
    private val verificationTokenExpirationMillis: Long, // Tiempo de expiración del Verification Token en milisegundos
    private val passwordResetTokenExpirationMillis: Long, // Tiempo de expiración del Password Reset Token en milisegundos

    // Repositorios para tokens persistidos (inyectados a través de Koin)
    private val refreshTokenRepository: RefreshTokenRepository,
    private val verificationTokenRepository: VerificationTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository
) {
    // Algoritmo para firmar y verificar tokens JWT (HMAC256 usando la clave secreta)
    private val algorithm = Algorithm.HMAC256(jwtSecret)

    // Verificador de JWT para tokens de acceso
    private val jwtVerifier: JWTVerifier = JWT.require(algorithm)
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .build()

    /**
     * Genera un token de acceso JWT para un usuario dado su ID.
     * Este token contiene claims básicos y una expiración.
     * @param userId El ID del usuario para el que se genera el token.
     * @return El token de acceso JWT como una cadena de texto.
     */
    fun generateAccessToken(userId: Int): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId) // Añade el ID del usuario como un claim en el token
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpirationMillis)) // Establece la expiración
            .sign(algorithm) // Firma el token con el algoritmo y la clave secreta
    }

    /**
     * Valida un token de acceso JWT.
     * Utiliza el verificador JWT configurado para validar la firma y los claims.
     * @param token El token de acceso JWT a validar.
     * @return El ID del usuario si el token es válido, o null si no lo es.
     * @throws TokenExpiredException Si el token ha expirado.
     * @throws InvalidTokenException Si el token es inválido por otras razones (firma, claims, etc.).
     */
    fun validateAccessToken(token: String): Int? {
        return try {
            val decodedToken = jwtVerifier.verify(token) // Verifica el token
            // Si la verificación pasa, extrae el userId del claim
            decodedToken.getClaim("userId").asInt()
        } catch (e: com.auth0.jwt.exceptions.TokenExpiredException) {
            // Captura la excepción específica de expiración de Auth0 JWT
            throw TokenExpiredException("Access token has expired.")
        } catch (e: Exception) {
            // Captura otras excepciones de validación de JWT (firma inválida, claims incorrectos, etc.)
            throw InvalidTokenException("Invalid access token.")
        }
    }

    /**
     * Genera un nuevo token de refresco y lo persiste en la base de datos.
     * @param userId El ID del usuario para el que se genera el token.
     * @return El modelo de dominio RefreshToken creado.
     */
    suspend fun generateRefreshToken(userId: Int): RefreshToken {
        val tokenValue = UUID.randomUUID().toString() // Genera un valor de token aleatorio
        // Calcula la expiración: suma la duración al instante actual y convierte a LocalDateTime
        val expiresAt = (Clock.System.now() + refreshTokenExpirationMillis.toDuration(DurationUnit.MILLISECONDS)).toLocalDateTime(TimeZone.UTC)

        val newRefreshToken = RefreshToken(
            id = 0, // El ID será autoincremental en la BD
            token = tokenValue,
            userId = userId,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
            expiresAt = expiresAt,
            isUsed = false
        )
        refreshTokenRepository.create(newRefreshToken) // Persiste el token en la BD
        return newRefreshToken
    }

    /**
     * Valida un token de refresco contra la base de datos.
     * @param tokenValue El valor del token de refresco a validar.
     * @return El ID del usuario si el token es válido y activo, o null si no lo es.
     * @throws InvalidTokenException Si el token no se encuentra o ya ha sido usado.
     * @throws TokenExpiredException Si el token ha expirado.
     */
    suspend fun validateRefreshToken(tokenValue: String): Int? {
        val refreshToken = refreshTokenRepository.findByToken(tokenValue)
            ?: throw InvalidTokenException("Refresh token not found.")

        if (refreshToken.isUsed) {
            throw InvalidTokenException("Refresh token has already been used.")
        }

        if (refreshToken.isExpired(Clock.System.now().toLocalDateTime(TimeZone.UTC))) {
            throw TokenExpiredException("Refresh token has expired.")
        }

        return refreshToken.userId
    }

    /**
     * Invalida un token de refresco marcándolo como usado o eliminándolo de la base de datos.
     * @param tokenValue El valor del token de refresco a invalidar.
     */
    suspend fun invalidateRefreshToken(tokenValue: String) {
        val refreshToken = refreshTokenRepository.findByToken(tokenValue)
        if (refreshToken != null) {
            refreshTokenRepository.markAsUsed(refreshToken.id) // O refreshTokenRepository.deleteById(refreshToken.id)
        }
    }

    /**
     * Genera un nuevo token de verificación de email y lo persiste.
     * @param userId El ID del usuario para el que se genera el token.
     * @return El modelo de dominio VerificationToken creado.
     */
    suspend fun generateVerificationToken(userId: Int): VerificationToken {
        val tokenValue = UUID.randomUUID().toString()
        // Calcula la expiración: suma la duración al instante actual y convierte a LocalDateTime
        val expiresAt = (Clock.System.now() + verificationTokenExpirationMillis.toDuration(DurationUnit.MILLISECONDS)).toLocalDateTime(TimeZone.UTC)

        val newVerificationToken = VerificationToken(
            id = 0,
            token = tokenValue,
            userId = userId,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
            expiresAt = expiresAt,
            isUsed = false
        )
        verificationTokenRepository.create(newVerificationToken)
        return newVerificationToken
    }

    /**
     * Valida un token de verificación de email contra la base de datos.
     * @param tokenValue El valor del token de verificación a validar.
     * @return El ID del usuario si el token es válido y activo, o null si no lo es.
     * @throws InvalidTokenException Si el token no se encuentra o ya ha sido usado.
     * @throws TokenExpiredException Si el token ha expirado.
     */
    suspend fun validateVerificationToken(tokenValue: String): Int? {
        val verificationToken = verificationTokenRepository.findByToken(tokenValue)
            ?: throw InvalidTokenException("Email verification token not found.")

        if (verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token has already been used.")
        }

        if (verificationToken.isExpired(Clock.System.now().toLocalDateTime(TimeZone.UTC))) {
            throw TokenExpiredException("Email verification token has expired.")
        }

        return verificationToken.userId
    }

    /**
     * Invalida un token de verificación de email marcándolo como usado o eliminándolo de la base de datos.
     * @param tokenValue El valor del token de verificación a invalidar.
     */
    suspend fun invalidateVerificationToken(tokenValue: String) {
        val verificationToken = verificationTokenRepository.findByToken(tokenValue)
        if (verificationToken != null) {
            verificationTokenRepository.markAsUsed(verificationToken.id)
        }
    }

    /**
     * Genera un nuevo token de restablecimiento de contraseña y lo persiste.
     * @param userId El ID del usuario para el que se genera el token.
     * @return El modelo de dominio PasswordResetToken creado.
     */
    suspend fun generatePasswordResetToken(userId: Int): PasswordResetToken {
        val tokenValue = UUID.randomUUID().toString()
        // Calcula la expiración: suma la duración al instante actual y convierte a LocalDateTime
        val expiresAt = (Clock.System.now() + passwordResetTokenExpirationMillis.toDuration(DurationUnit.MILLISECONDS)).toLocalDateTime(TimeZone.UTC)

        val newPasswordResetToken = PasswordResetToken(
            id = 0,
            token = tokenValue,
            userId = userId,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
            expiresAt = expiresAt,
            isUsed = false
        )
        passwordResetTokenRepository.create(newPasswordResetToken)
        return newPasswordResetToken
    }

    /**
     * Valida un token de restablecimiento de contraseña contra la base de datos.
     * @param tokenValue El valor del token de restablecimiento a validar.
     * @return El ID del usuario si el token es válido y activo, o null si no lo es.
     * @throws InvalidTokenException Si el token no se encuentra o ya ha sido usado.
     * @throws TokenExpiredException Si el token ha expirado.
     */
    suspend fun validatePasswordResetToken(tokenValue: String): Int? {
        val passwordResetToken = passwordResetTokenRepository.findByToken(tokenValue)
            ?: throw InvalidTokenException("Password reset token not found.")

        if (passwordResetToken.isUsed) {
            throw InvalidTokenException("Password reset token has already been used.")
        }

        if (passwordResetToken.isExpired(Clock.System.now().toLocalDateTime(TimeZone.UTC))) {
            throw TokenExpiredException("Password reset token has expired.")
        }

        return passwordResetToken.userId
    }

    /**
     * Invalida un token de restablecimiento de contraseña marcándolo como usado o eliminándolo de la base de datos.
     * @param tokenValue El valor del token de restablecimiento a invalidar.
     */
    suspend fun invalidatePasswordResetToken(tokenValue: String) {
        val passwordResetToken = passwordResetTokenRepository.findByToken(tokenValue)
        if (passwordResetToken != null) {
            passwordResetTokenRepository.markAsUsed(passwordResetToken.id)
        }
    }

    /**
     * Helper para obtener el tiempo de expiración del Access Token en segundos.
     * @return El tiempo de expiración del Access Token en segundos.
     */
    fun getAccessTokenExpirationSeconds(): Long = accessTokenExpirationMillis / 1000
}
