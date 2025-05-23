package com.limbus_server.auth.application.dto.response

import kotlinx.serialization.Serializable

// Data class que representa la información básica de un usuario que se envía al cliente.
// Se utiliza en respuestas donde se necesita identificar o mostrar datos del usuario.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class UserResponse(
    // El identificador único del usuario.
    val id: Int,

    // El correo electrónico del usuario.
    // Generalmente es un identificador público del usuario.
    val email: String,

    // El nombre del usuario.
    // Basado en tu UsersSchema, este campo es opcional (nullable).
    val name: String? = null // Marcado como nullable si el nombre puede ser null en la BD
)

// Notas:
// - Esta clase contiene los datos del usuario que son seguros para exponer al cliente.
// - NO debe incluir información sensible como el passwordHash.
// - No debe contener lógica de negocio.
// - Los nombres de las propiedades deben coincidir con las claves que el cliente espera en el JSON de respuesta.
