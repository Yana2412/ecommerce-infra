# Microservicio de usuarios (Spring Boot)

## Endpoints

| Método | Ruta                           | Descripción                    |
|--------|----------------------------------|----------------------------------|
| POST   | /api/auth/login                  | Iniciar sesión y generar token  |
| POST   | /api/usuarios                    | Registrar usuario                |
| GET    | /api/usuarios                    | Consultar usuarios               |
| GET    | /api/usuarios/{id}                | Consultar un usuario             |
| PUT    | /api/usuarios/{id}                | Actualizar información           |
| PATCH  | /api/usuarios/{id}/activar        | Activar cuenta                    |
| PATCH  | /api/usuarios/{id}/desactivar     | Desactivar cuenta                 |

## Reglas
- Solo una cuenta activa puede autenticarse.
- Contraseñas con hash seguro (BCrypt).
- No ejecutar el servicio como root.
