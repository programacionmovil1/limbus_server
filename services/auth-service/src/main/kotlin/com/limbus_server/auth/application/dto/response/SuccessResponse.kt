package com.limbus_server.auth.application.dto.response

import kotlinx.serialization.Serializable

// Data class que representa una respuesta genérica de éxito.
// Se utiliza cuando una operación se completa correctamente y no hay datos específicos que devolver.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class SuccessResponse(
    // Un mensaje que indica que la operación fue exitosa.
    // Puedes personalizar este mensaje según la operación realizada (ej: "Password reset successful").
    val message: String = "Operation successful" // Mensaje por defecto
)

// Notas:
// - Esta clase es simple y solo contiene un mensaje de éxito.
// - Es útil para endpoints que no devuelven datos específicos en caso de éxito.
// - El mensaje por defecto puede ser sobrescrito al crear la instancia.
