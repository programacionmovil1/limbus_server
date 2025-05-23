package com.limbus_server.auth.application.dto.response

import kotlinx.serialization.Serializable

// Data class que representa la respuesta de una operación de autenticación exitosa (login o refresh token).
// Contiene los tokens necesarios para que el cliente interactúe con los servicios protegidos.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class AuthResponse(
    // El token de acceso (generalmente un JWT).
    // Este token se incluye en las cabeceras de peticiones a servicios protegidos.
    val accessToken: String,

    // El token de refresco.
    // Se utiliza para obtener un nuevo token de acceso cuando el actual expire,
    // sin necesidad de que el usuario vuelva a ingresar sus credenciales.
    val refreshToken: String,

    // Opcional: Tiempo de expiración del token de acceso en segundos.
    // El cliente puede usar esto para saber cuándo debe intentar refrescar el token.
    val expiresIn: Long? = null // Marcado como nullable si decides no incluirlo siempre
)

// Notas:
// - Esta clase solo contiene los datos que se envían al cliente como respuesta.
// - No debe contener lógica de negocio.
// - Los nombres de las propiedades deben coincidir con las claves que el cliente espera en el JSON de respuesta.
