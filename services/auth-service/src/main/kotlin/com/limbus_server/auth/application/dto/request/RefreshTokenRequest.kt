package com.limbus_server.auth.application.dto.request

import kotlinx.serialization.Serializable

// Data class que representa los datos de la petición para refrescar un token de acceso.
// Se utiliza para recibir el token de refresco del cliente.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class RefreshTokenRequest(
    // El token de refresco proporcionado por el cliente.
    // Este token se utiliza para verificar la identidad del usuario
    // y emitir un nuevo token de acceso sin necesidad de las credenciales de login.
    val refreshToken: String
)

// Notas:
// - Esta clase solo contiene el token de refresco.
// - No debe contener lógica de negocio ni validaciones (las validaciones se hacen en la capa de servicio o dominio).
// - El nombre de la propiedad debe coincidir con la clave esperada en el JSON de la petición de refresh token.
