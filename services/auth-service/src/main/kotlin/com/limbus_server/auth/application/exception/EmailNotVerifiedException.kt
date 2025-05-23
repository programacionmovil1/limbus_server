package com.limbus_server.auth.application.exception

// Excepción personalizada lanzada cuando un usuario intenta iniciar sesión
// pero su dirección de correo electrónico aún no ha sido verificada.
// Hereda de RuntimeException para ser una excepción no verificada (unchecked).
class EmailNotVerifiedException(
    // Mensaje opcional que describe la causa del error.
    message: String = "Email address has not been verified"
) : RuntimeException(message) // Hereda de RuntimeException y pasa el mensaje al constructor base

// Notas:
// - Esta clase es una excepción de negocio específica para emails no verificados.
// - Se lanza desde la capa de servicio (application/service) durante el proceso de login
//   después de verificar que el usuario existe pero antes de verificar la contraseña,
//   si el estado de verificación del email indica que no está verificado.
// - La capa de routing (infrastructure/routing) o un manejador de StatusPages
//   capturará esta excepción y la traducirá a una respuesta HTTP adecuada (ej: 401 Unauthorized o 403 Forbidden).
