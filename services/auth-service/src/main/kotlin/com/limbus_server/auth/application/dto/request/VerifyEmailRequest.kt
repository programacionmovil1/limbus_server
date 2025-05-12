package com.limbus_server.auth.application.dto.request

import kotlinx.serialization.Serializable

// Data class que representa los datos de la petición para verificar el correo electrónico.
// Se utiliza para recibir el token de verificación del cliente.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class VerifyEmailRequest(
    // El token de verificación que se envió al correo electrónico del usuario.
    // Este token se utiliza para confirmar que el usuario es propietario del email.
    val verificationToken: String
)

// Notas:
// - Esta clase solo contiene el token de verificación.
// - No debe contener lógica de negocio ni validaciones (las validaciones se hacen en la capa de servicio o dominio).
// - El nombre de la propiedad debe coincidir con la clave esperada en el JSON de la petición.
