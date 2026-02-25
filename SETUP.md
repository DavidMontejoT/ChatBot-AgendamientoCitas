# Guía de Configuración Local

Esta guía te ayudará a configurar el entorno de desarrollo local para el Sistema de Citas Médicas.

## Prerrequisitos

Antes de comenzar, asegúrate de tener instalado:

### Opcionales (si no usas Docker)
- **Java 17+**: [Descargar JDK](https://adoptium.net/)
- **Maven 3.8+**: [Descargar Maven](https://maven.apache.org/download.cgi)
- **Node.js 18+**: [Descargar Node.js](https://nodejs.org/)
- **PostgreSQL 15+**: [Descargar PostgreSQL](https://www.postgresql.org/download/)

### Herramientas Recomendadas
- **Docker Desktop**: [Descargar Docker](https://www.docker.com/products/docker-desktop/)
- **Git**: [Descargar Git](https://git-scm.com/downloads)
- **IDE recomendado**: IntelliJ IDEA o VS Code

## Opción 1: Docker Compose (Más Fácil)

Esta opción configura todo automáticamente (base de datos + backend).

### Paso 1: Clonar el Repositorio

```bash
git clone <repositorio>
cd chatbox
```

### Paso 2: Configurar Variables de Entorno

```bash
cp backend/.env.example backend/.env
```

Edita el archivo `backend/.env`:

```env
# WhatsApp Cloud API (obtener de Meta for Developers)
WHATSAPP_TOKEN=tu_token_permanente_aqui
WHATSAPP_PHONE_NUMBER_ID=tu_phone_number_id_aqui

# Database (Docker Compose configura esto automáticamente)
DATABASE_URL=jdbc:postgresql://postgres:5432/citas_medicas
```

### Paso 3: Iniciar Servicios

```bash
docker-compose up -d
```

Esto iniciará:
- PostgreSQL en puerto 5432
- Backend API en puerto 8080

### Paso 4: Verificar

```bash
# Verificar backend
curl http://localhost:8080/api/citas/health

# Debería devolver: "API funcionando correctamente"
```

### Paso 5: Configurar Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

El frontend estará disponible en: http://localhost:5173

## Opción 2: Configuración Manual

### Paso 1: Base de Datos PostgreSQL

#### Mac (Homebrew)
```bash
brew install postgresql@15
brew services start postgresql@15
psql postgres
```

#### Windows
Descargar e instalar desde [postgresql.org](https://www.postgresql.org/download/windows/)

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### Paso 2: Crear Base de Datos

```bash
# Conectar a PostgreSQL
psql -U postgres

# Crear base de datos
CREATE DATABASE citas_medicas;

# Salir
\q
```

### Paso 3: Configurar Backend

```bash
cd backend

# Editar application.properties
nano src/main/resources/application.properties
```

Configura estos valores:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/citas_medicas
spring.datasource.username=postgres
spring.datasource.password=tu_password_postgres
```

### Paso 4: Ejecutar Backend

```bash
cd backend
mvn spring-boot:run
```

El backend estará disponible en: http://localhost:8080

### Paso 5: Ejecutar Frontend

```bash
cd frontend
npm install
npm run dev
```

El frontend estará disponible en: http://localhost:5173

## Configuración de WhatsApp (Opcional para Desarrollo)

Para probar la integración de WhatsApp en local:

### 1. Crear Tunel con ngrok

```bash
# Instalar ngrok
brew install ngrok  # Mac
# o descargar de ngrok.com

# Crear tunel
ngrok http 8080
```

Copia la URL HTTPS generada (ej: `https://abc123.ngrok.io`)

### 2. Configurar Webhook en Meta

1. Ve a [Meta for Developers](https://developers.facebook.com/)
2. Selecciona tu app de WhatsApp
3. Configura webhook:
   - URL: `https://abc123.ngrok.io/api/whatsapp/webhook`
   - Verify Token: `chatbox_verify_token_2024`

### 3. Variables de Entorno

```bash
export WHATSAPP_TOKEN=tu_token
export WHATSAPP_PHONE_NUMBER_ID=tu_phone_id
```

## Verificar Instalación

### Backend Health Check

```bash
curl http://localhost:8080/api/citas/health
```

### Probar Crear Cita

```bash
curl -X POST http://localhost:8080/api/citas \
  -H "Content-Type: application/json" \
  -d '{
    "nombrePaciente": "Test User",
    "telefono": "+521234567890",
    "email": "test@example.com",
    "fechaHora": "2025-12-31T15:00:00",
    "doctor": "Dr. Test"
  }'
```

## Logs y Debugging

### Ver Logs de Docker

```bash
# Todos los servicios
docker-compose logs -f

# Solo backend
docker-compose logs -f backend

# Solo base de datos
docker-compose logs -f postgres
```

### Reiniciar Servicios

```bash
docker-compose restart
```

### Detener Todo

```bash
docker-compose down

# Detener y eliminar volúmenes (cuidado: borra datos)
docker-compose down -v
```

## Problemas Comunes

### Puerto 8080 en uso

```bash
# Mac/Linux
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Puerto 5432 en uso (PostgreSQL)

```bash
# Mac/Homebrew
brew services stop postgresql@15

# Linux
sudo systemctl stop postgresql

# Windows
# Services → PostgreSQL → Stop
```

### Error de Conexión a Base de Datos

1. Verifica que PostgreSQL esté corriendo
2. Verifica credenciales en `application.properties`
3. Verifica que la base de datos `citas_medicas` exista

### Frontend no conecta al Backend

1. Verifica que el backend esté corriendo en puerto 8080
2. Verifica CORS en `application.properties`
3. Revisa la consola del navegador para errores

## Siguiente Paso

Una vez configurado el entorno local, revisa [DEPLOYMENT.md](DEPLOYMENT.md) para instrucciones de despliegue en producción.
