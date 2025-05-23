package com.limbus_server.auth.application.exception

// Excepción personalizada lanzada cuando un intento de autenticación (login) falla
// debido a que las credenciales proporcionadas (email y/o contraseña) son incorrectas.
// Hereda de RuntimeException para ser una excepción no verificada (unchecked).
class InvalidCredentialsException(
    // Mensaje opcional que describe la causa del error.
    // Por defecto, proporciona un mensaje genérico si no se especifica uno.
    message: String = "Invalid email or password"
) : RuntimeException(message) // Hereda de RuntimeException y pasa el mensaje al constructor base

// Notas:
// - Esta clase es una excepción de negocio específica para errores de autenticación.
// - Se lanza desde la capa de servicio (application/service) durante el proceso de login
//   cuando la verificación de la contraseña falla.
// - La capa de routing (infrastructure/routing) o un manejador de StatusPages
//   capturará esta excepción y la traducirá a una respuesta HTTP adecuada (ej: 401 Unauthorized o 400 Bad Request).
