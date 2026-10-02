# Microservicio de usuarios (Spring Boot)

## Endpoints

| Método | Ruta                           | Descripción                    | Quién puede usarlo            |
|--------|--------------------------------|--------------------------------|-------------------------------|
| POST   | /api/auth/login                | Iniciar sesión y generar token | Público (solo cuentas activas) |
| POST   | /api/usuarios                  | Registrar usuario              | Público                       |
| GET    | /api/usuarios                  | Consultar usuarios             | Solo ADMIN                    |
| GET    | /api/usuarios/{id}             | Consultar un usuario           | El propio usuario o ADMIN     |
| PUT    | /api/usuarios/{id}             | Actualizar información         | El propio usuario o ADMIN     |
| PATCH  | /api/usuarios/{id}/activar     | Activar cuenta                 | Solo ADMIN                    |
| PATCH  | /api/usuarios/{id}/desactivar  | Desactivar cuenta              | Solo ADMIN                    |

## Reglas
- Solo una cuenta activa puede autenticarse.
- Al desactivar una cuenta, sus tokens JWT ya emitidos dejan de ser válidos de inmediato.
- Un administrador no puede desactivar su propia cuenta (409).
- Contraseñas con hash seguro (BCrypt).
- No ejecutar el servicio como root.
