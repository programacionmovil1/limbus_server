package com.limbus_server.auth.application.dto.request

import kotlinx.serialization.Serializable

// Data class que representa los datos de la petición de inicio de sesión.
// Se utiliza para recibir las credenciales del usuario del cliente.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class LoginRequest(
    // El correo electrónico del usuario que intenta iniciar sesión.
    // Debe ser una cadena de texto y es un campo obligatorio.
    val email: String,

    // La contraseña del usuario en texto plano (antes de ser verificada contra el hash en el backend).
    // Debe ser una cadena de texto y es un campo obligatorio.
    val password: String
)

// Notas:
// - Esta clase solo contiene el email y la contraseña, que son las credenciales de login.
// - No debe contener lógica de negocio ni validaciones (las validaciones se hacen en la capa de servicio o dominio).
// - Los nombres de las propiedades deben coincidir con las claves esperadas en el JSON de la petición de login.
