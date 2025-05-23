package com.limbus_server.auth.application.dto.response

import kotlinx.serialization.Serializable

// Data class que representa la respuesta de una operación de registro de usuario exitosa.
// Contiene el identificador del nuevo usuario y los tokens de autenticación si se inicia sesión automáticamente.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class RegisterResponse(
    // El identificador único del usuario recién creado.
    val userId: Int,

    // El token de acceso (generalmente un JWT).
    // Incluido si el usuario inicia sesión automáticamente después del registro.
    val accessToken: String,

    // El token de refresco.
    // Incluido si el usuario inicia sesión automáticamente después del registro.
    val refreshToken: String,

    // Opcional: Tiempo de expiración del token de acceso en segundos.
    // Útil para que el cliente sepa cuándo refrescar el token.
    val expiresIn: Long? = null, // Marcado como nullable si decides no incluirlo siempre

    // TEMPORAL PARA DESARROLLO/DEBUGGING: El token de verificación de email.
    // ¡REMUEVE ESTO PARA PRODUCCIÓN! No debes exponer tokens de verificación en la respuesta de registro por seguridad.
    val verificationToken: String? = null // Añadido temporalmente
)

// Notas:
// - Esta clase contiene los datos que se envían al cliente como respuesta de un registro exitoso.
// - Incluye el ID del usuario y los tokens de autenticación para un inicio de sesión automático.
// - No debe contener lógica de negocio.
// - Los nombres de las propiedades deben coincidir con las claves que el cliente espera en el JSON de respuesta.