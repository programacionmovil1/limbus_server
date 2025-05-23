package com.limbus_server.auth.application.exception

// Excepción personalizada lanzada cuando se intenta crear un usuario
// con un identificador (ej: email) que ya existe en el sistema.
// Hereda de Exception para ser una excepción verificada (checked exception),
// o de RuntimeException si se prefiere no forzar su manejo explícito (unchecked exception).
// Generalmente, para errores de negocio, RuntimeException es común en servicios backend.
class UserAlreadyExistsException(
    // Mensaje opcional que describe la causa del error.
    // Por defecto, proporciona un mensaje genérico si no se especifica uno.
    message: String = "User with this identifier already exists"
) : RuntimeException(message) // Hereda de RuntimeException y pasa el mensaje al constructor base

// Notas:
// - Esta clase es una excepción de negocio específica.
// - Se lanza desde la capa de servicio (application/service) cuando se detecta la duplicidad.
// - La capa de routing (infrastructure/routing) o un manejador de StatusPages
//   capturará esta excepción y la traducirá a una respuesta HTTP adecuada (ej: 409 Conflict).
