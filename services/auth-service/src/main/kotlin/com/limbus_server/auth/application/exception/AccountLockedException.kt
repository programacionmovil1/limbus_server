package com.limbus_server.auth.application.exception

// Excepción personalizada lanzada cuando un usuario intenta acceder
// pero su cuenta ha sido bloqueada.
// Hereda de RuntimeException para ser una excepción no verificada (unchecked).
class AccountLockedException(
    // Mensaje opcional que describe la causa del error.
    message: String = "Account is locked"
) : RuntimeException(message) // Hereda de RuntimeException y pasa el mensaje al constructor base

// Notas:
// - Esta clase es una excepción de negocio específica para cuentas bloqueadas.
// - Se lanza desde la capa de servicio (application/service) durante el proceso de login
//   o al intentar realizar otras acciones si el estado de la cuenta indica que está bloqueada.
// - La capa de routing (infrastructure/routing) o un manejador de StatusPages
//   capturará esta excepción y la traducirá a una respuesta HTTP adecuada (ej: 401 Unauthorized o 403 Forbidden).
