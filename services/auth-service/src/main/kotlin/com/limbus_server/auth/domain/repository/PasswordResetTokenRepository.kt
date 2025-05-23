package com.limbus_server.auth.domain.repository

import com.limbus_server.auth.domain.model.PasswordResetToken
import kotlinx.datetime.LocalDateTime

// Interfaz que define las operaciones de acceso a datos para la entidad PasswordResetToken.
// Es parte de la capa de dominio y agnóstica a la implementación de persistencia.
interface PasswordResetTokenRepository {

    /**
     * Crea un nuevo token de restablecimiento de contraseña en el sistema.
     * @param token El modelo de dominio PasswordResetToken a crear.
     */
    suspend fun create(token: PasswordResetToken)

    /**
     * Busca un token de restablecimiento por su valor.
     * @param token El valor del token de restablecimiento a buscar.
     * @return El modelo de dominio PasswordResetToken si se encuentra, o null.
     */
    suspend fun findByToken(token: String): PasswordResetToken?

    /**
     * Busca el token de restablecimiento activo para un usuario específico.
     * (Asumiendo que un usuario solo tiene un token de restablecimiento activo a la vez).
     * @param userId El ID del usuario.
     * @return El modelo de dominio PasswordResetToken si se encuentra, o null.
     */
    suspend fun findActiveByUserId(userId: Int): PasswordResetToken?

    /**
     * Marca un token de restablecimiento como usado o inactivo.
     * @param tokenId El ID del token a marcar.
     */
    suspend fun markAsUsed(tokenId: Int)

    /**
     * Elimina un token de restablecimiento por su ID.
     * @param tokenId El ID del token a eliminar.
     */
    suspend fun deleteById(tokenId: Int)

    /**
     * Elimina tokens de restablecimiento expirados o usados para un usuario específico.
     * @param userId El ID del usuario.
     * @param currentTime La hora actual para verificar la expiración.
     */
    suspend fun cleanExpiredAndUsedTokens(userId: Int, currentTime: LocalDateTime)

    /**
     * Elimina todos los tokens de restablecimiento asociados a un usuario.
     * @param userId El ID del usuario.
     */
    suspend fun deleteAllByUserId(userId: Int)

    // Puedes añadir otras operaciones si la lógica de restablecimiento de contraseña lo requiere.
}

// Notas:
// - Esta interfaz define el contrato para la gestión de tokens de restablecimiento persistidos.
// - La implementación concreta (PasswordResetTokenRepositoryImpl) residirá en infrastructure/repository.
// - El TokenService (en infrastructure/security) o el AuthService (en application/service)
//   dependerán de esta interfaz.
