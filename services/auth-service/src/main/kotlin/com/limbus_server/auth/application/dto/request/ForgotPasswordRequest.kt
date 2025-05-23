package com.limbus_server.auth.application.dto.request

import kotlinx.serialization.Serializable

// Data class que representa los datos de la petición para iniciar el proceso de "Olvidé mi contraseña".
// Se utiliza para recibir el identificador del usuario (email) del cliente.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class ForgotPasswordRequest(
    // El correo electrónico del usuario que ha olvidado su contraseña.
    // El servidor usará este email para identificar al usuario y enviar instrucciones de restablecimiento.
    val email: String
)

// Notas:
// - Esta clase solo contiene el email.
// - No debe contener lógica de negocio ni validaciones (las validaciones se hacen en la capa de servicio o dominio).
// - El nombre de la propiedad debe coincidir con la clave esperada en el JSON de la petición.
