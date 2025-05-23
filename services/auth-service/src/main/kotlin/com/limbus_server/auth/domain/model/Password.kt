package com.limbus_server.auth.domain.model

// Modelo de dominio (objeto de valor) que representa una contraseña, típicamente en su forma hasheada.
// Encapsula el valor del hash y puede incluir validaciones básicas de formato si es necesario.
// Esta clase es agnóstica a la base de datos y a la serialización.
data class Password(
    // El valor del hash de la contraseña.
    // Se almacena aquí en lugar de la contraseña en texto plano.
    val hash: String
) {
    // Puedes añadir validaciones básicas aquí si el formato del hash tiene alguna regla específica.
    // Por ejemplo, verificar que no esté vacío o que cumpla una longitud mínima si tu algoritmo lo garantiza.
    init {
        require(hash.isNotBlank()) { "Password hash cannot be blank" }
        // require(hash.length >= MIN_HASH_LENGTH) { "Password hash is too short" } // Ejemplo
    }

    // Notas:
    // - Este es un objeto de valor: su identidad se basa en el valor de su propiedad 'hash'.
    // - NO debe contener la contraseña en texto plano.
    // - La lógica de hashing y verificación se maneja externamente por un PasswordHasher.
    // - Su uso es opcional si prefieres manejar el hash como String directamente en el modelo User.
}

// Puedes definir constantes relacionadas si es necesario
// private const val MIN_HASH_LENGTH = 60 // Ejemplo para BCrypt
