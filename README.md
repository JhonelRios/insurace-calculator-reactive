# Cotizador de Seguros Vehiculares

Prueba técnica de una API para calcular la prima de un seguro vehicular aplicando reglas de negocio. Está desarrollado con **Java 21**, **Spring Boot + WebFlux**, **JWT**, **Redis**, **PostgreSQL** y se ejecuta a través de docker-compose.

---

## Requisitos

- Docker Desktop instalado

---

## Levantar el entorno con Docker

1. Ejecuta el siguiente comando en la raíz del proyecto:
```bash
docker compose up --build
```
2. Accede a la documentación con Swagger: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Observaciones

- Las credenciales de los servicios como **PostgreSQL** y **Redis** se dejaron expuestas directamente en el archivo `docker-compose.yml` por motivos de simplicidad. En un entorno real, estas deben gestionarse mediante variables de entorno seguras o servicios de configuración externa.

- Existe un usuario preconfigurado para validar la autenticación con JWT:

    - **Usuario:** `admin`
    - **Contraseña:** `1234`

  Puedes usar estas credenciales en el endpoint `/auth/login` para obtener un token JWT y probar los endpoints protegidos desde Swagger.
- El reporte de cobertura de test es: `coverage-report.pdf`