package com.limbus_server.auth.domain.repository

import com.limbus_server.auth.domain.model.RefreshToken
import kotlinx.datetime.LocalDateTime

// Interfaz que define las operaciones de acceso a datos para la entidad RefreshToken.
// Es parte de la capa de dominio y agnóstica a la implementación de persistencia.
interface RefreshTokenRepository {

    /**
     * Crea un nuevo token de refresco en el sistema.
     * @param token El modelo de dominio RefreshToken a crear.
     */
    suspend fun create(token: RefreshToken)

    /**
     * Busca un token de refresco por su valor (la cadena del token).
     * @param token El valor del token de refresco a buscar.
     * @return El modelo de dominio RefreshToken si se encuentra, o null en caso contrario.
     */
    suspend fun findByToken(token: String): RefreshToken?

    /**
     * Busca tokens de refresco asociados a un usuario específico.
     * @param userId El ID del usuario.
     * @return Una lista de modelos de dominio RefreshToken.
     */
    suspend fun findByUserId(userId: Int): List<RefreshToken>

    /**
     * Marca un token de refresco como usado (si implementas rotación de tokens).
     * @param tokenId El ID del token a marcar.
     */
    suspend fun markAsUsed(tokenId: Int)

    /**
     * Elimina un token de refresco por su ID.
     * @param tokenId El ID del token a eliminar.
     */
    suspend fun deleteById(tokenId: Int)

    /**
     * Elimina tokens de refresco expirados o usados para un usuario específico.
     * @param userId El ID del usuario.
     * @param currentTime La hora actual para verificar la expiración.
     */
    suspend fun cleanExpiredAndUsedTokens(userId: Int, currentTime: LocalDateTime)

    /**
     * Elimina todos los tokens de refresco asociados a un usuario.
     * @param userId El ID del usuario.
     */
    suspend fun deleteAllByUserId(userId: Int)

    // Puedes añadir otras operaciones si la lógica de gestión de refresh tokens lo requiere.
}

// Notas:
// - Esta interfaz define el contrato para la gestión de tokens de refresco persistidos.
// - La implementación concreta (RefreshTokenRepositoryImpl) residirá en infrastructure/repository.
// - El TokenService (en infrastructure/security) o el AuthService (en application/service)
//   dependerán de esta interfaz para interactuar con los datos de refresh tokens.
