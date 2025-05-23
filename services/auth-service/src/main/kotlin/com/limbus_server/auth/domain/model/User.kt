package com.limbus_server.auth.domain.model
import kotlinx.datetime.LocalDateTime

// Modelo de dominio que representa a un usuario en el contexto de la lógica de negocio de autenticación.
// Esta clase es agnóstica a la base de datos y a la serialización.
data class User(
    // Identificador único del usuario.
    // Este campo es relevante para la lógica de negocio (ej: identificar al usuario autenticado).
    val id: Int,

    // Correo electrónico del usuario.
    // Es un identificador clave en la lógica de autenticación.
    val email: String,

    // Hash de la contraseña del usuario.
    // La lógica de negocio de verificación de contraseña opera sobre este hash.
    val passwordHash: String, // No usar el hash y optar por el string
//    val passwordHash: Password, // Optar por usar la logica del hash

    // Nombre del usuario.
    val name: String?,

    // Marca de tiempo de creación del usuario.
    // Relevante para lógica de negocio (ej: antigüedad de la cuenta).
    val createdAt: LocalDateTime,

    // Marca de tiempo de la última actualización del usuario (opcional).
    val updatedAt: LocalDateTime?,

    // Opcional: Estado de verificación del correo electrónico.
    // Relevante para la lógica de negocio (ej: permitir login solo si está verificado).
    val isEmailVerified: Boolean = false, // Valor por defecto

    // Opcional: Estado de bloqueo de la cuenta.
    // Relevante para la lógica de negocio (ej: impedir login si está bloqueada).
    val isAccountLocked: Boolean = false // Valor por defecto
)

// Notas:
// - Esta clase es un modelo de dominio puro.
// - No debe contener anotaciones de serialización (@Serializable) ni de mapeo de base de datos.
// - Contiene las propiedades esenciales para la lógica de negocio de autenticación.
// - Los repositorios (en infrastructure/repository) serán responsables de mapear
//   los resultados de la base de datos a este modelo de dominio.
// - Los servicios de aplicación (application/service) operarán sobre este modelo.
