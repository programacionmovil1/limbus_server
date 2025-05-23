package com.limbus_server.auth.application.exception

// Excepción personalizada lanzada cuando se intenta realizar una operación
// sobre un usuario que no se encuentra en el sistema.
// Hereda de RuntimeException para ser una excepción no verificada (unchecked).
class UserNotFoundException(
    // Mensaje opcional que describe la causa del error.
    // Por defecto, proporciona un mensaje genérico si no se especifica uno.
    message: String = "User not found"
) : RuntimeException(message) // Hereda de RuntimeException y pasa el mensaje al constructor base

// Notas:
// - Esta clase es una excepción de negocio específica.
// - Se lanza desde la capa de servicio (application/service) o a veces desde el repositorio
//   (infrastructure/repository) cuando una búsqueda de usuario no produce resultados.
// - La capa de routing (infrastructure/routing) o un manejador de StatusPages
//   capturará esta excepción y la traducirá a una respuesta HTTP adecuada (ej: 404 Not Found).
