
## ¿Qué significa Stateful?

Una aplicación **stateful** mantiene estado (información de sesión) entre peticiones.

## ¿Qué significa Stateless?

En una aplicación stateless:
El servidor NO guarda información de sesión
entre peticiones.

## La diferencia importante es esta:
**Stateful**
Servidor recuerda quién eres.

**Stateless**
Cada petición le vuelve a decir al servidor quién eres. mediante el JWT.

Por eso tienes esto En tu **SecurityConfig**:
    .sessionManagement(
        s -> s.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
        )
    )

**Ejemplo visual**

**Stateful**

Login
↓
Servidor crea sesión
↓
Sesión 123
↓
Petición A
Petición B
Petición C
↓
Todas usan Sesión 123

**Stateless**

Login
↓
JWT
↓

Petición A + JWT
Petición B + JWT
Petición C + JWT

Cada petición es autónoma.

**Ventajas de usar staleless**

✅ Escala mejor.

✅ Menos memoria en el servidor.

✅ Muy útil para microservicios.

✅ Ideal para Angular, React, Flutter y aplicaciones móviles.

✅ No depende de sesiones.

Notas:
No se guarda para la siguiente.

En la próxima petición se vuelve a construir desde el JWT.

Eso es precisamente el corazón de una arquitectura stateless: 
el servidor no recuerda al usuario entre peticiones; 
el cliente envía el JWT en cada llamada y 
el servidor reconstruye el contexto de seguridad cada vez. 🚀