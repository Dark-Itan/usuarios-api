# API de Gestión de Usuarios - Spring Boot

## Descripción

API RESTful para la gestión de usuarios con autenticación básica. Implementada con Spring Boot 3, PostgreSQL y arquitectura en capas siguiendo principios SOLID y buenas prácticas de desarrollo.

---

## Parte 1: Investigación Teórica

### 1. Arquitectura en Capas

#### Controller (Capa de Presentación)
- **Responsabilidad**: Exponer los endpoints REST y manejar el protocolo HTTP
- **Funciones**:
  - Recibir y validar datos de entrada mediante DTOs
  - Delegar la lógica al Service
  - Construir respuestas HTTP con ResponseEntity
  - Manejar códigos de estado (200, 201, 400, 401, 404)
- **No debe**: Contener lógica de negocio ni acceder al Repository

#### Service (Capa de Negocio)
- **Responsabilidad**: Implementar la lógica de negocio
- **Funciones**:
  - Orquestar operaciones entre Controller y Repository
  - Aplicar reglas de negocio y validaciones
  - Manejar transacciones con @Transactional
  - Convertir Entities a DTOs
- **No debe**: Manejar detalles HTTP ni acceder directamente a la base de datos

#### Repository (Capa de Acceso a Datos)
- **Responsabilidad**: Abstraer la persistencia con la base de datos
- **Funciones**:
  - Realizar operaciones CRUD
  - Ejecutar consultas personalizadas
  - Implementar paginación
- **No debe**: Contener lógica de negocio

#### Entity (Capa de Modelo de Datos)
- **Responsabilidad**: Representar tablas de la base de datos
- **Funciones**:
  - Definir estructura de datos
  - Mapear relaciones con JPA
  - Establecer restricciones (unique, not null)
- **No debe**: Ser expuesta directamente en respuestas HTTP

---

### 2. Manejo de Contraseñas

#### ¿Por qué NUNCA guardar contraseñas en texto plano?

**Riesgos**:
1. Si la base de datos es comprometida, todas las contraseñas quedan expuestas
2. Los usuarios reutilizan contraseñas en múltiples servicios
3. Violación de leyes de protección de datos
4. Empleados internos podrían ver información sensible
5. No hay forma de verificar integridad de los datos

#### ¿Qué función cumple BCrypt?

**Características**:
- Algoritmo de hashing diseñado específicamente para contraseñas
- Genera hash irreversible (no se puede descifrar)
- Incluye salt automático (valor aleatorio único por hash)
- Es computacionalmente costoso (previene fuerza bruta)
- El mismo texto genera diferentes hashes en cada ejecución
- Tiene factor de costo configurable

**Por qué es seguro**:
- Aunque dos usuarios tengan la misma contraseña, los hashes son diferentes
- Un atacante no puede usar tablas precalculadas (rainbow tables)
- Cada intento de verificación requiere tiempo significativo de CPU

---

### 3. Lógica de Autenticación Básica

#### Registro (Crear Usuario)

**Concepto**: Proceso de alta de un nuevo usuario en el sistema.

**Flujo**:
1. Cliente envía nombre, email y contraseña
2. API valida que el email no exista previamente
3. Se encripta la contraseña con BCrypt
4. Se crea el usuario con estado "activo"
5. Se persiste en la base de datos
6. Retorna 201 Created

**Operación BD**: INSERT

#### Login (Verificar Credenciales)

**Concepto**: Proceso de verificación de identidad.

**Flujo**:
1. Cliente envía email y contraseña
2. API busca usuario por email
3. Compara contraseña con hash almacenado usando BCrypt
4. Si coinciden: retorna 200 OK
5. Si no coinciden: retorna 401 Unauthorized

**Operación BD**: SELECT

#### Diferencia Conceptual Clave

| Aspecto | Registro | Login |
|---------|----------|-------|
| Propósito | Crear nuevo usuario | Verificar identidad |
| Operación BD | INSERT | SELECT |
| Código éxito | 201 Created | 200 OK |
| Código error | 400 Bad Request | 401 Unauthorized |
| Modifica datos | Sí | No |
| Requiere auth previa | No | No (solo credenciales) |

---

### 4. Buenas Prácticas REST

#### Verbos HTTP

| Verbo | Uso | Características |
|-------|-----|-----------------|
| POST | Crear recursos | No idempotente |
| GET | Obtener recursos | Idempotente |
| PUT | Actualizar recursos | Idempotente |
| DELETE | Eliminar recursos | Idempotente |

#### Códigos de Estado

| Código | Significado | Cuándo usarlo |
|--------|-------------|---------------|
| 200 OK | Éxito | Operaciones exitosas |
| 201 Created | Creado | POST exitoso |
| 400 Bad Request | Error cliente | Datos inválidos |
| 401 Unauthorized | No autorizado | Credenciales inválidas |
| 404 Not Found | No encontrado | ID inexistente |

#### Endpoints de Autenticación

| Método HTTP | Endpoint | Respuesta Éxito | Respuesta Error |
|-------------|----------|-----------------|-----------------|
| POST | /api/v1/auth/register | 201 Created | 400 Bad Request |
| POST | /api/v1/auth/login | 200 OK | 401 Unauthorized |

#### Endpoints de Usuarios

| Método HTTP | Endpoint | Respuesta Éxito | Respuesta Error |
|-------------|----------|-----------------|-----------------|
| GET | /api/v1/users | 200 OK | - |
| GET | /api/v1/users/{id} | 200 OK | 404 Not Found |
| PUT | /api/v1/users/{id} | 200 OK | 400 Bad Request, 404 Not Found |
| DELETE | /api/v1/users/{id} | 200 OK | 404 Not Found |

---
