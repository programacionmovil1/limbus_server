package com.limbus_server.auth.application.exception

// Excepción personalizada lanzada cuando un token (de acceso o de refresco)
// proporcionado por el cliente no es válido.
// Hereda de RuntimeException para ser una excepción no verificada (unchecked).
class InvalidTokenException(
    // Mensaje opcional que describe la causa del error.
    // Por defecto, proporciona un mensaje genérico si no se especifica uno.
    message: String = "Invalid token"
) : RuntimeException(message) // Hereda de RuntimeException y pasa el mensaje al constructor base

// Notas:
// - Esta clase es una excepción de negocio específica para errores relacionados con tokens.
// - Se lanza desde la capa de servicio (application/service) o la capa de seguridad
//   (infrastructure/security) cuando la validación de un token falla.
// - La capa de routing (infrastructure/routing) o un manejador de StatusPages
//   capturará esta excepción y la traducirá a una respuesta HTTP adecuada (ej: 401 Unauthorized o 400 Bad Request).
