package com.limbus_server.auth.application.dto.request
import kotlinx.serialization.Serializable

// Data class que representa los datos de la petición para restablecer la contraseña.
// Se utiliza para recibir el token de restablecimiento y la nueva contraseña del cliente.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class ResetPasswordRequest(
    // El token de restablecimiento que se envió al usuario.
    // Este token valida la autorización para cambiar la contraseña.
    val resetToken: String,

    // La nueva contraseña que el usuario desea establecer.
    // Esta contraseña será hasheada y almacenada en la base de datos.
    val newPassword: String,

    // Opcional: Confirmación de la nueva contraseña.
    // Útil para validación en el cliente, pero la validación final de que coinciden
    // debe hacerse en el servidor (en la capa de servicio).
    // val confirmNewPassword: String // Puedes incluirlo si lo necesitas
)

// Notas:
// - Esta clase contiene el token de restablecimiento y la nueva contraseña.
// - No debe contener lógica de negocio ni validaciones complejas (las validaciones se hacen en la capa de servicio o dominio).
// - Los nombres de las propiedades deben coincidir con las claves esperadas en el JSON de la petición.
