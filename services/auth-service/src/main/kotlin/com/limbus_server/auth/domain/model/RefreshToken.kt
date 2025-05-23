package com.limbus_server.auth.domain.model

import kotlinx.datetime.LocalDateTime

// Modelo de dominio que representa un Token de Refresco.
// Se utiliza para obtener nuevos tokens de acceso sin requerir las credenciales del usuario.
data class RefreshToken(
    // Identificador único del token de refresco (si se almacena en BD).
    val id: Int,

    // El valor del token de refresco (la cadena de texto).
    val token: String,

    // El ID del usuario al que pertenece este token de refresco.
    val userId: Int,

    // Marca de tiempo de creación del token.
    val createdAt: LocalDateTime,

    // Marca de tiempo de expiración del token.
    val expiresAt: LocalDateTime,

    // Opcional: Indica si el token ha sido revocado o usado (si implementas rotación de tokens de refresco).
    val isUsed: Boolean = false
) {
    // Puedes añadir lógica de dominio simple aquí, si es relevante.
    // Por ejemplo, verificar si el token ha expirado.
    fun isExpired(currentTime: LocalDateTime): Boolean {
        return expiresAt < currentTime
    }
}

// Notas:
// - Este modelo representa el concepto de un token de refresco en el dominio.
// - Contiene las propiedades esenciales para su gestión (ID, valor, usuario, expiración, estado).
// - Los repositorios (en infrastructure/repository) serán responsables de mapear
//   los resultados de la base de datos a este modelo.
// - Los servicios de aplicación y de seguridad operarán sobre este modelo.
