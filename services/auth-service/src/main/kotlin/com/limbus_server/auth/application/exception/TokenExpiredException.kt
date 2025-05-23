package com.limbus_server.auth.application.exception

// Excepción personalizada lanzada cuando un token proporcionado (ej: JWT)
// ha expirado y ya no es válido para autenticar peticiones.
// Hereda de RuntimeException para ser una excepción no verificada (unchecked).
class TokenExpiredException(
    // Mensaje opcional que describe la causa del error.
    // Por defecto, proporciona un mensaje genérico si no se especifica uno.
    message: String = "Token has expired"
) : RuntimeException(message) // Hereda de RuntimeException y pasa el mensaje al constructor base

// Notas:
// - Esta clase es una excepción de negocio específica para tokens expirados.
// - Se lanza desde la capa de servicio (application/service) o la capa de seguridad
//   (infrastructure/security) cuando se valida un token y se determina que ha expirado.
// - La capa de routing (infrastructure/routing) o un manejador de StatusPages
//   capturará esta excepción y la traducirá a una respuesta HTTP adecuada (ej: 401 Unauthorized).
// - Puede ser manejada de forma diferente a InvalidTokenException si se necesita
//   distinguir entre un token inválido y uno expirado en el frontend.
