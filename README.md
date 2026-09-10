# Usuarios API

API RESTful para la gestión de usuarios con autenticación básica. Desarrollada con Spring Boot 3, PostgreSQL y arquitectura en capas siguiendo principios SOLID.

**Autor:** Geovany Guadalupe Gomez Rodas  

**Repositorio:** https://github.com/Dark-Itan/usuarios-api

**API en producción:** https://usuarios-api-production-2de8.up.railway.app

---

## Tabla de Contenidos

1. [Investigación Teórica](#1-investigación-teórica)
2. [Tecnologías Utilizadas](#2-tecnologías-utilizadas)
3. [Arquitectura del Proyecto](#3-arquitectura-del-proyecto)
4. [Base de Datos](#4-base-de-datos)
5. [Endpoints de la API](#5-endpoints-de-la-api)
6. [Ejemplos de Peticiones](#6-ejemplos-de-peticiones)
7. [Ejecución Local](#7-ejecución-local)
8. [Estructura del Proyecto](#8-estructura-del-proyecto)
9. [Colección de Postman](#9-colección-de-postman)

---

## 1. Investigación Teórica

### 1.1 Arquitectura en Capas

#### Controller (Capa de Presentación)
- **Responsabilidad**: Exponer los endpoints REST y manejar el protocolo HTTP.
- **Funciones**:
  - Recibir y validar datos de entrada mediante DTOs.
  - Delegar la lógica al Service.
  - Construir respuestas HTTP con ResponseEntity.
  - Manejar códigos de estado (200, 201, 400, 401, 403, 404, 500).
- **No debe**: Contener lógica de negocio ni acceder al Repository.

**Pros:**
- Separación clara entre HTTP y lógica de negocio.
- Facilita el testing unitario.

**Contras:**
- Puede volverse "delgado" si toda la lógica está en el Service.
- Riesgo de acoplamiento si se accede directamente al Repository.

**Solución:**
- Usar DTOs para entrada/salida.
- Manejar excepciones de forma centralizada con `@ControllerAdvice`.

#### Service (Capa de Negocio)
- **Responsabilidad**: Implementar toda la lógica de negocio y reglas del dominio.
- **Funciones**:
  - Orquestar operaciones entre Controller y Repository.
  - Aplicar reglas de negocio y validaciones.
  - Manejar transacciones con `@Transactional`.
  - Convertir Entities a DTOs.
- **No debe**: Manejar detalles HTTP ni acceder directamente a la base de datos.

**Pros:**
- Centraliza la lógica de negocio.
- Facilita la reutilización y el testing.

**Contras:**
- Riesgo de convertirse en "clases dios".

**Solución:**
- Usar interfaces para definir contratos (ISP de SOLID).
- Inyectar dependencias por constructor.

#### Repository (Capa de Acceso a Datos)
- **Responsabilidad**: Abstraer la persistencia con la base de datos.
- **Funciones**:
  - Realizar operaciones CRUD.
  - Ejecutar consultas personalizadas.
  - Implementar proyecciones con `@Query`.
- **No debe**: Contener lógica de negocio.

**Pros:**
- Abstrae la complejidad de la base de datos.
- Spring Data genera implementaciones automáticamente.

**Contras:**
- Query methods complejos pueden ser difíciles de mantener.
- Riesgo de N+1 queries.

**Solución:**
- Usar nombres de métodos descriptivos.
- Implementar proyecciones con `@Query` para traer solo lo necesario.

#### Entity (Capa de Modelo de Datos)
- **Responsabilidad**: Representar las tablas de la base de datos.
- **Funciones**:
  - Definir la estructura de los datos.
  - Mapear relaciones con JPA.
  - Establecer restricciones (unique, not null).
- **No debe**: Ser expuesta directamente en respuestas HTTP.

**Pros:**
- Mapeo objeto-relacional automático.
- Validaciones integradas con Bean Validation.

**Contras:**
- Riesgo de exponer datos sensibles en respuestas.

**Solución:**
- Usar DTOs para comunicación externa.
- No exponer Entities en las respuestas HTTP.

---

### 1.2 Manejo de Contraseñas

#### ¿Por qué NUNCA guardar contraseñas en texto plano?

**Riesgos:**
1. Si la base de datos es comprometida, todas las contraseñas quedan expuestas.
2. Los usuarios reutilizan contraseñas en múltiples servicios.
3. Violación de leyes de protección de datos.
4. Empleados internos podrían ver información sensible.

**Solución:**
- Usar BCrypt para encriptar contraseñas.
- Nunca exponer contraseñas en respuestas HTTP.
- Usar variables de entorno para credenciales de BD.

#### ¿Qué función cumple BCrypt?

**Características:**
- Algoritmo de hashing diseñado específicamente para contraseñas.
- Genera hash irreversible.
- Incluye salt automático (valor aleatorio único por hash).
- Es computacionalmente costoso (previene fuerza bruta).
- Tiene factor de costo configurable.

**Pros:**
- Estándar de la industria.
- Resistente a ataques de fuerza bruta.
- Salt automático previene ataques rainbow table.

**Contras:**
- Más lento que otros algoritmos (intencional).
- Consume más CPU en aplicaciones con muchos logins.

**Solución:**
- Configurar factor de costo adecuado (10-12).

**Configuración en el proyecto:**
```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

### 1.3 Lógica de Autenticación Básica

#### Registro (Crear Usuario)

**Flujo:**
1. Cliente envía nombre, email y contraseña.
2. API valida que el email no exista previamente.
3. Se encripta la contraseña con BCrypt.
4. Se crea el usuario con estado "activo".
5. Se persiste en la base de datos.
6. Retorna 201 Created.

**Operación BD:** INSERT

**Pros:**
- Proceso simple y directo.
- Validación de email único.
- Contraseña encriptada desde el primer momento.

**Contras:**
- No hay verificación de email.

**Solución:**
- Implementar verificación por email (futuro).

#### Login (Verificar Credenciales)

**Flujo:**
1. Cliente envía email y contraseña.
2. API busca usuario por email.
3. Compara contraseña con hash almacenado usando BCrypt.
4. Si coinciden: retorna 200 OK.
5. Si no coinciden: retorna 401 Unauthorized.
6. Si la cuenta está desactivada: retorna 403 Forbidden.

**Operación BD:** SELECT

**Pros:**
- Verificación segura con BCrypt.
- No revela si el email existe o no.
- Valida estado de la cuenta.

**Contras:**
- No hay tokens de sesión (JWT).
- No hay rate limiting.

**Solución:**
- Implementar JWT para sesiones (futuro).
- Agregar rate limiting (futuro).

#### Diferencia Conceptual Clave

| Aspecto | Registro | Login |
|---------|----------|-------|
| Propósito | Crear nuevo usuario | Verificar identidad |
| Operación BD | INSERT | SELECT |
| Código éxito | 201 Created | 200 OK |
| Código error | 400 Bad Request | 401 Unauthorized |
| Modifica datos | Sí | No |

### 1.4 Buenas Prácticas REST

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
| 403 Forbidden | Prohibido | Cuenta desactivada |
| 404 Not Found | No encontrado | ID inexistente |
| 500 Internal Server Error | Error servidor | Error inesperado |

**Pros:**
- Códigos estándar facilitan la integración.
- Separación entre errores del cliente y del servidor.

**Contras:**
- Requiere disciplina para mantener consistencia.

**Solución:**
- Documentar todos los códigos en el README.
- Usar un GlobalExceptionHandler centralizado.

---

## 2. Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 LTS | Lenguaje de programación |
| Spring Boot | 3.3.4 | Framework principal |
| Spring Data JPA | 3.3.4 | Persistencia y ORM |
| Spring Security | 6.3.3 | Seguridad y BCrypt |
| Hibernate | 6.5.3 | Implementación JPA |
| PostgreSQL | 16 | Base de datos relacional |
| HikariCP | 5.1.0 | Connection Pool |
| Gradle | 9.3.0 | Build Tool |
| Lombok | 1.18.34 | Reducción de boilerplate |

---

## 3. Arquitectura del Proyecto

El proyecto sigue una **arquitectura en capas** con separación clara de responsabilidades:

- **Controller**: Recibe peticiones HTTP, valida DTOs y retorna ResponseEntity.
- **Service**: Lógica de negocio, transacciones y conversión Entity ↔ DTO.
- **Repository**: Operaciones CRUD y consultas personalizadas.
- **Entity**: Mapeo JPA, restricciones e índices.

**Principios aplicados:**
- **SRP (Single Responsibility)**: Cada capa tiene una responsabilidad única.
- **DIP (Dependency Inversion)**: Inyección por constructor, no `@Autowired` en campos.
- **ISP (Interface Segregation)**: Interfaces específicas para cada Service.

---

## 4. Base de Datos

### 4.1 Tecnología

- **Motor**: PostgreSQL 16
- **Hosting**: Aiven (nube)
- **ORM**: Spring Data JPA + Hibernate
- **Pool de conexiones**: HikariCP
- **SSL**: Requerido (`sslmode=require`)

### 4.2 Estructura de la Tabla `usuarios`

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| id | UUID | PRIMARY KEY | Identificador único |
| nombre | VARCHAR(100) | NOT NULL | Nombre del usuario |
| email | VARCHAR(150) | NOT NULL, UNIQUE | Email único para login |
| password | VARCHAR(255) | NOT NULL | Hash BCrypt |
| estado | BOOLEAN | NOT NULL, DEFAULT true | Activo/Inactivo |

### 4.3 Índices

| Índice | Columna | Tipo | Propósito |
|--------|---------|------|-----------|
| idx_usuarios_email | email | UNIQUE | Búsquedas rápidas por email |
| idx_usuarios_estado | estado | Normal | Filtrado por estado |

**¿Por qué estos índices?**
- `email`: Se usa en login y validación de duplicados.
- `estado`: Se usa para filtrar usuarios activos/inactivos.

**¿Por qué NO hay índice en `nombre`?**
- Poca selectividad.
- No se busca por nombre en los endpoints críticos.

### 4.4 Script SQL

```sql
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_estado ON usuarios(estado);
```

### 4.5 Configuración de Conexión

La conexión se configura mediante **variables de entorno**:

| Variable | Descripción |
|----------|-------------|
| DB_URL | URL JDBC de PostgreSQL |
| DB_USERNAME | Usuario de la base de datos |
| DB_PASSWORD | Contraseña de la base de datos |
| PORT | Puerto del servidor |

**Ventajas:**
- No se exponen credenciales en el código.
- Fácil cambio entre entornos.

**Contras:**
- Requiere configuración adicional.

**Solución:**
- Usar archivo `.env` para desarrollo local.
- Nunca subir `.env` a GitHub.

### 4.6 Soft Delete

En lugar de eliminar físicamente los usuarios, se **desactivan** cambiando el campo `estado` a `false`.

**Pros:**
- Permite recuperar cuentas.
- Mantiene historial para auditoría.

**Contras:**
- La base de datos crece con registros "muertos".

**Solución:**
- Agregar un job programado para eliminar usuarios desactivados después de X tiempo (futuro).

---

## 5. Endpoints de la API

### 5.1 Autenticación

| Método | Endpoint | Descripción | Códigos |
|--------|----------|-------------|---------|
| POST | `/api/v1/auth/register` | Registrar nuevo usuario | 201, 400 |
| POST | `/api/v1/auth/login` | Iniciar sesión | 200, 401, 403 |

### 5.2 Usuarios

| Método | Endpoint | Descripción | Códigos |
|--------|----------|-------------|---------|
| GET | `/api/v1/users` | Obtener todos los usuarios | 200 |
| GET | `/api/v1/users/{id}` | Obtener usuario por ID | 200, 400, 404 |
| PUT | `/api/v1/users/{id}` | Actualizar usuario | 200, 400, 404 |
| DELETE | `/api/v1/users/{id}` | Desactivar usuario (soft delete) | 200, 400, 404 |

### 5.3 Detalle de Códigos de Estado

| Código | Significado | Cuándo ocurre |
|--------|-------------|---------------|
| 200 OK | Éxito | Operación exitosa |
| 201 Created | Creado | Registro exitoso |
| 400 Bad Request | Datos inválidos | Validación fallida, email duplicado, ID inválido |
| 401 Unauthorized | No autorizado | Credenciales incorrectas |
| 403 Forbidden | Prohibido | Cuenta desactivada |
| 404 Not Found | No encontrado | ID inexistente |
| 500 Internal Server Error | Error servidor | Error inesperado |

---

## 6. Ejemplos de Peticiones

### 6.1 Registro de Usuario

**cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Geovany",
    "email": "geovany@example.com",
    "password": "password123"
  }'
```

**Respuesta exitosa (201 Created):**
```json
{
    "id": "d79aea18-bb2d-4cd0-abb5-6199ac28bd89",
    "nombre": "Geovany",
    "email": "geovany@example.com",
    "estado": true
}
```

**Respuesta error (400 Bad Request):**
```json
{
    "status": 400,
    "message": "Este correo ya está registrado. Intenta con otro.",
    "timestamp": "2026-09-09T20:00:00"
}
```

### 6.2 Login

**cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "geovany@example.com",
    "password": "password123"
  }'
```

**Respuesta exitosa (200 OK):**
```
Login exitoso
```

**Respuesta error (401 Unauthorized):**
```json
{
    "status": 401,
    "message": "Correo o contraseña incorrectos. Verifica e intenta de nuevo.",
    "timestamp": "2026-09-09T20:00:00"
}
```

**Respuesta error (403 Forbidden):**
```json
{
    "status": 403,
    "message": "Tu cuenta está desactivada. Contacta al administrador para más información.",
    "timestamp": "2026-09-09T20:00:00"
}
```

### 6.3 Obtener Todos los Usuarios

**cURL:**
```bash
curl -X GET http://localhost:8080/api/v1/users
```

**Respuesta exitosa (200 OK):**
```json
[
    {
        "id": "d79aea18-bb2d-4cd0-abb5-6199ac28bd89",
        "nombre": "Geovany",
        "email": "geovany@example.com",
        "estado": true
    }
]
```

### 6.4 Obtener Usuario por ID

**cURL:**
```bash
curl -X GET http://localhost:8080/api/v1/users/d79aea18-bb2d-4cd0-abb5-6199ac28bd89
```

**Respuesta exitosa (200 OK):**
```json
{
    "id": "d79aea18-bb2d-4cd0-abb5-6199ac28bd89",
    "nombre": "Geovany",
    "email": "geovany@example.com",
    "estado": true
}
```

**Respuesta error (400 Bad Request):**
```json
{
    "status": 400,
    "message": "El identificador proporcionado no es válido. Verifica e intenta de nuevo.",
    "timestamp": "2026-09-09T20:00:00"
}
```

**Respuesta error (404 Not Found):**
```json
{
    "status": 404,
    "message": "No encontramos lo que buscas. Verifica e intenta de nuevo.",
    "timestamp": "2026-09-09T20:00:00"
}
```

### 6.5 Actualizar Usuario

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/v1/users/d79aea18-bb2d-4cd0-abb5-6199ac28bd89 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Geovany Actualizado",
    "email": "geovany.nuevo@example.com"
  }'
```

**Respuesta exitosa (200 OK):**
```json
{
    "id": "d79aea18-bb2d-4cd0-abb5-6199ac28bd89",
    "nombre": "Geovany Actualizado",
    "email": "geovany.nuevo@example.com",
    "estado": true
}
```

### 6.6 Eliminar Usuario (Soft Delete)

**cURL:**
```bash
curl -X DELETE http://localhost:8080/api/v1/users/d79aea18-bb2d-4cd0-abb5-6199ac28bd89
```

**Respuesta exitosa (200 OK):**
```
Usuario desactivado exitosamente
```

---

## 7. Ejecución Local

### 7.1 Requisitos Previos

- **Java 21** instalado
- **Git** instalado
- **PostgreSQL** (o usar Aiven)
- **Postman** (opcional)

### 7.2 Pasos

#### 1. Clonar el repositorio

```bash
git clone https://github.com/Dark-Itan/usuarios-api.git
cd usuarios-api
```

#### 2. Configurar variables de entorno

Crear archivo `.env` en la raíz del proyecto:

```properties
DB_URL=jdbc:postgresql://tu-host:puerto/tu-base-de-datos?sslmode=require
DB_USERNAME=tu-usuario
DB_PASSWORD=tu-password
PORT=8080
```

**Nota:** Este archivo **NO** se sube a GitHub (está en `.gitignore`).

#### 3. Ejecutar la aplicación

**Windows (PowerShell):**
```powershell
./run.ps1
```

**Linux/Mac:**
```bash
./gradlew bootRun
```

#### 4. Verificar que arrancó

Abrir en el navegador:

```
http://localhost:8080
```

Debería mostrar: `Usuarios API - Corriendo correctamente`

---

## 8. Estructura del Proyecto

```
usuarios-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ejemplo/
│   │   │           └── usuarios/
│   │   │               ├── UsuariosApplication.java
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── StartupLogger.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── HomeController.java
│   │   │               │   └── UsuarioController.java
│   │   │               ├── dto/
│   │   │               │   ├── LoginRequestDTO.java
│   │   │               │   ├── RegisterRequestDTO.java
│   │   │               │   ├── UsuarioResponseDTO.java
│   │   │               │   └── UsuarioUpdateDTO.java
│   │   │               ├── entity/
│   │   │               │   └── Usuario.java
│   │   │               ├── exception/
│   │   │               │   ├── AccountDeactivatedException.java
│   │   │               │   ├── EmailAlreadyExistsException.java
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── InvalidCredentialsException.java
│   │   │               │   └── ResourceNotFoundException.java
│   │   │               ├── repository/
│   │   │               │   └── UsuarioRepository.java
│   │   │               └── service/
│   │   │                   ├── UsuarioService.java
│   │   │                   └── impl/
│   │   │                       └── UsuarioServiceImpl.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           └── favicon.ico
│   └── test/
│       └── java/
├── .env.example
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── Procfile
├── postman_collection.json
├── postman_environment.json
└── README.md
```

---

## 9. Colección de Postman

### 9.1 Importar la Colección

1. Abrir Postman
2. Click en **"Import"**
3. Seleccionar `postman_collection.json`
4. Click en **"Import"**

### 9.2 Importar el Entorno

1. Click en **"Environments"** (barra lateral)
2. Click en **"Import"**
3. Seleccionar `postman_environment.json`
4. Click en **"Import"**
5. Seleccionar el entorno `Usuarios API - Producción`

### 9.3 Ejecutar la Colección

1. Click derecho en la colección
2. Click en **"Run collection"**
3. Click en **"Run Usuarios API - Producción"**

**Resultado esperado:** 6/6 peticiones exitosas.

---

## Conclusión

Este proyecto implementa una API RESTful completa para la gestión de usuarios, siguiendo:

- ✅ **Arquitectura en capas** (Controller, Service, Repository, Entity)
- ✅ **Principios SOLID** (SRP, DIP, ISP)
- ✅ **Buenas prácticas REST** (verbos HTTP, códigos de estado)
- ✅ **Seguridad** (BCrypt, UUID, variables de entorno)
- ✅ **Manejo de errores** (GlobalExceptionHandler)
- ✅ **Documentación** (README, Postman, cURL)

**Tecnologías:** Java 21, Spring Boot 3, PostgreSQL, HikariCP, Gradle, Lombok.

**Autor:** Geovany Guadalupe Gomez Rodas

---

## Licencia

Este proyecto es de uso educativo.