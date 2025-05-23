package com.limbus_server.auth.application.dto.response

import kotlinx.serialization.Serializable

// Data class que representa una respuesta genérica de error.
// Se utiliza para comunicar detalles de errores al cliente.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class ErrorResponse(
    // Un mensaje descriptivo del error.
    // Este mensaje debería ser claro y útil para el cliente (ej: "Invalid email format", "User not found").
    val message: String,

    // Opcional: Un código de error específico.
    // Puede ser útil para que el cliente identifique programáticamente el tipo de error
    // sin tener que parsear el mensaje.
    val errorCode: Int? = null, // Puedes usar Int, String, o un enum para los códigos

    // Opcional: Detalles adicionales sobre el error, como errores de validación por campo.
    // Esta lista podría contener objetos que especifiquen el campo y el mensaje de error asociado.
    val details: List<ErrorDetail>? = null // Lista de detalles del error (ej: errores de validación de campos)
)

// Data class anidada para representar detalles de errores individuales, como errores de validación de campos.
@Serializable
data class ErrorDetail(
    // Opcional: El nombre del campo que tiene el error (útil para errores de validación).
    val field: String? = null,

    // Un mensaje específico para este detalle del error.
    val message: String
)

// Notas:
// - Esta clase proporciona una estructura estandarizada para las respuestas de error en tu API.
// - Permite comunicar tanto errores generales como errores específicos (como validaciones fallidas).
// - La capa de infraestructura (ej: StatusPages o interceptores) o la capa de aplicación
//   serán responsables de crear instancias de ErrorResponse cuando ocurran errores.
