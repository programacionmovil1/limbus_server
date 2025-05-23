package com.limbus_server.auth.domain.model

import kotlinx.datetime.LocalDateTime

// Modelo de dominio que representa un Token de Restablecimiento de Contraseña.
// Se utiliza para permitir a un usuario cambiar su contraseña olvidada.
data class PasswordResetToken(
    // Identificador único del token (si se almacena en BD).
    val id: Int,

    // El valor del token de restablecimiento (la cadena de texto).
    val token: String,

    // El ID del usuario al que pertenece este token.
    val userId: Int,

    // Marca de tiempo de creación del token.
    val createdAt: LocalDateTime,

    // Marca de tiempo de expiración del token.
    val expiresAt: LocalDateTime,

    // Opcional: Indica si el token ya ha sido usado para restablecer la contraseña.
    val isUsed: Boolean = false
) {
    // Puedes añadir lógica de dominio simple aquí.
    fun isExpired(currentTime: LocalDateTime): Boolean {
        return expiresAt < currentTime
    }
}

// Notas:
// - Este modelo representa el concepto de un token de restablecimiento en el dominio.
// - Contiene las propiedades esenciales para su gestión.
// - Los repositorios (en infrastructure/repository) serán responsables de mapear
//   los resultados de la base de datos a este modelo.
// - Los servicios de aplicación y de seguridad operarán sobre este modelo.
