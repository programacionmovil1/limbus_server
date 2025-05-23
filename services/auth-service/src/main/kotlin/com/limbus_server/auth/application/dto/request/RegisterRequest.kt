package com.limbus_server.auth.application.dto.request

import kotlinx.serialization.Serializable

// Data class que representa los datos de la petición de registro de usuario.
// Se utiliza para recibir los datos del cliente (aplicación móvil) a través de la API.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class RegisterRequest(
    // El correo electrónico del usuario.
    // Debe ser una cadena de texto y es un campo obligatorio para el registro.
    val email: String,

    // La contraseña del usuario en texto plano (antes de ser hasheada en el backend).
    // Debe ser una cadena de texto y es un campo obligatorio.
    val password: String,

    // El nombre del usuario.
    // Basado en el UsersSchema, este campo es opcional (nullable).
    // Si lo pides en el registro inicial, inclúyelo aquí.
    // Si no lo pides en el registro, puedes omitirlo o hacerlo nullable.
    val name: String? = null // Marcado como nullable y con valor por defecto null
)

// Notas:
// - Esta clase solo contiene las propiedades necesarias para la petición.
// - No debe contener lógica de negocio ni validaciones complejas (las validaciones se hacen en la capa de servicio o dominio).
// - Los nombres de las propiedades deben coincidir con las claves esperadas en el JSON de la petición.
