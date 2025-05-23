package com.limbus_server.auth.domain.service

// Interfaz que define las operaciones para hashear y verificar contraseñas.
// Esta interfaz es parte de la capa de dominio y es agnóstica al algoritmo de hashing específico.
interface PasswordHasher {

    /**
     * Hashea una contraseña en texto plano.
     *
     * @param password La contraseña en texto plano a hashear.
     * @return El hash generado de la contraseña.
     */
    fun hashPassword(password: String): String

    /**
     * Verifica si una contraseña en texto plano coincide con un hash dado.
     *
     * @param password La contraseña en texto plano a verificar.
     * @param hashedPassword El hash con el que comparar la contraseña.
     * @return true si la contraseña coincide con el hash, false en caso contrario.
     */
    fun verifyPassword(password: String, hashedPassword: String): Boolean
}

// Notas:
// - Esta interfaz solo define QUÉ operaciones de hashing y verificación existen.
// - NO contiene la lógica de implementación (cómo se realiza el hashing o la verificación).
// - La implementación concreta de esta interfaz (PasswordHasherImpl) residirá en la capa de infraestructura (infrastructure/security).
// - El AuthService (en la capa de aplicación) dependerá de esta interfaz para manejar contraseñas.
