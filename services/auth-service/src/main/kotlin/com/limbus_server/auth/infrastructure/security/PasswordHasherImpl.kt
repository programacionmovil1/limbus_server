package com.limbus_server.auth.infrastructure.security

import com.limbus_server.auth.domain.service.PasswordHasher
import org.mindrot.jbcrypt.BCrypt

// Implementación concreta de la interfaz PasswordHasher utilizando el algoritmo BCrypt.
// Esta clase es responsable de hashear contraseñas en texto plano y verificar
// si una contraseña coincide con un hash existente.
class PasswordHasherImpl : PasswordHasher { // Implementa la interfaz definida en el dominio

    /**
     * Hashea una contraseña en texto plano utilizando el algoritmo BCrypt.
     * BCrypt genera un "salt" aleatorio y lo incorpora al hash, lo que lo hace muy seguro
     * contra ataques de tablas arcoíris y colisiones.
     *
     * @param password La contraseña en texto plano a hashear.
     * @return El hash generado de la contraseña, incluyendo el salt.
     */
    override fun hashPassword(password: String): String {
        // BCrypt.gensalt() genera un salt aleatorio con una complejidad por defecto (generalmente 10).
        // BCrypt.hashpw() realiza el hashing de la contraseña con el salt generado.
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt dado.
     * Este método compara la contraseña proporcionada con el hash almacenado.
     * El hash ya contiene el salt, por lo que BCrypt lo extrae y lo usa para la verificación.
     *
     * @param password La contraseña en texto plano a verificar.
     * @param hashedPassword El hash BCrypt almacenado con el que comparar la contraseña.
     * @return true si la contraseña coincide con el hash, false en caso contrario.
     */
    override fun verifyPassword(password: String, hashedPassword: String): Boolean {
        // BCrypt.checkpw() es una función segura que maneja la extracción del salt
        // y la comparación del hash de forma correcta.
        return BCrypt.checkpw(password, hashedPassword)
    }
}