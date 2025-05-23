package com.limbus_server.auth.domain.repository

import com.limbus_server.auth.domain.model.VerificationToken
import kotlinx.datetime.LocalDateTime

// Interfaz que define las operaciones de acceso a datos para la entidad VerificationToken.
// Es parte de la capa de dominio y agnóstica a la implementación de persistencia.
interface VerificationTokenRepository {

    /**
     * Crea un nuevo token de verificación de email en el sistema.
     * @param token El modelo de dominio VerificationToken a crear.
     */
    suspend fun create(token: VerificationToken)

    /**
     * Busca un token de verificación por su valor.
     * @param token El valor del token de verificación a buscar.
     * @return El modelo de dominio VerificationToken si se encuentra, o null.
     */
    suspend fun findByToken(token: String): VerificationToken?

    /**
     * Busca el token de verificación activo para un usuario específico.
     * (Asumiendo que un usuario solo tiene un token de verificación activo a la vez).
     * @param userId El ID del usuario.
     * @return El modelo de dominio VerificationToken si se encuentra, o null.
     */
    suspend fun findActiveByUserId(userId: Int): VerificationToken?

    /**
     * Marca un token de verificación como usado o inactivo.
     * @param tokenId El ID del token a marcar.
     */
    suspend fun markAsUsed(tokenId: Int)

    /**
     * Elimina un token de verificación por su ID.
     * @param tokenId El ID del token a eliminar.
     */
    suspend fun deleteById(tokenId: Int)

    /**
     * Elimina tokens de verificación expirados o usados para un usuario específico.
     * @param userId El ID del usuario.
     * @param currentTime La hora actual para verificar la expiración.
     */
    suspend fun cleanExpiredAndUsedTokens(userId: Int, currentTime: LocalDateTime)

    /**
     * Elimina todos los tokens de verificación asociados a un usuario.
     * @param userId El ID del usuario.
     */
    suspend fun deleteAllByUserId(userId: Int)

    // Puedes añadir otras operaciones si la lógica de verificación de email lo requiere.
}

// Notas:
// - Esta interfaz define el contrato para la gestión de tokens de verificación persistidos.
// - La implementación concreta (VerificationTokenRepositoryImpl) residirá en infrastructure/repository.
// - El TokenService (en infrastructure/security) o el AuthService (en application/service)
//   dependerán de esta interfaz.
