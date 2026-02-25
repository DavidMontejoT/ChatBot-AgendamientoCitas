# Sistema de Citas Médicas con WhatsApp

Sistema MVP para agendamiento de citas médicas con integración de WhatsApp Cloud API y recordatorios automáticos.

## Características

✅ Agendamiento de citas vía web o WhatsApp
✅ Confirmación inmediata por WhatsApp
✅ Recordatorios automáticos (24h y 1h antes)
✅ Panel de administración web
✅ Integración con WhatsApp Cloud API

## Stack Tecnológico

### Backend
- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL
- Spring Scheduling
- WebClient (para WhatsApp API)

### Frontend
- React 18
- Vite
- Tailwind CSS
- Axios
- Lucide Icons

### Deploy
- Docker
- Docker Compose (local)
- Render (producción)

## Inicio Rápido

### Prerrequisitos

- Java 17+
- Maven
- Node.js 18+
- PostgreSQL o Docker

### Ejecutar con Docker Compose (Recomendado)

```bash
# Clonar el repositorio
git clone <repositorio>
cd chatbox

# Configurar variables de entorno
cp backend/.env.example backend/.env
# Editar backend/.env con tus credenciales de WhatsApp

# Iniciar servicios
docker-compose up -d

# El backend estará disponible en http://localhost:8080
```

### Ejecutar Localmente

#### Backend

```bash
cd backend

# Configurar base de datos PostgreSQL
# Editar src/main/resources/application.properties

# Ejecutar
mvn spring-boot:run
```

#### Frontend

```bash
cd frontend

# Instalar dependencias
npm install

# Configurar variables de entorno
cp .env.example .env

# Ejecutar
npm run dev
```

## Configuración de WhatsApp

### 1. Crear App en Meta for Developers

1. Ve a [Meta for Developers](https://developers.facebook.com/)
2. Crea una nueva app
3. Selecciona "Business" type
4. Agrega el producto "WhatsApp"

### 2. Configurar WhatsApp Cloud API

1. En tu dashboard de WhatsApp, obtén:
   - `WhatsApp Access Token` (permanente)
   - `Phone Number ID`

2. Configura el webhook:
   - URL: `https://tu-render-backend.com/api/whatsapp/webhook`
   - Verify Token: `chatbox_verify_token_2024`

3. Suscríbete a los campos:
   - `messages`

### 3. Variables de Entorno

```bash
WHATSAPP_TOKEN=tu_access_token_aqui
WHATSAPP_PHONE_NUMBER_ID=tu_phone_number_id_aqui
```

## API Endpoints

### Citas

- `POST /api/citas` - Crear nueva cita
- `GET /api/citas/{id}` - Obtener cita por ID
- `GET /api/citas/paciente/{telefono}` - Listar citas por paciente
- `PUT /api/citas/{id}/cancelar` - Cancelar cita

### WhatsApp

- `POST /api/whatsapp/enviar` - Enviar mensaje
- `GET /api/whatsapp/webhook` - Verificar webhook (Meta)
- `POST /api/whatsapp/webhook` - Recibir mensajes de WhatsApp

## Ejemplo de Uso

### Agendar Cita (cURL)

```bash
curl -X POST http://localhost:8080/api/citas \
  -H "Content-Type: application/json" \
  -d '{
    "nombrePaciente": "Juan Pérez",
    "telefono": "+521234567890",
    "email": "juan@example.com",
    "fechaHora": "2025-02-25T15:00:00",
    "doctor": "Dr. Pérez"
  }'
```

### Mensaje WhatsApp desde el Paciente

```
Quiero cita con el Dr. Pérez el 25/02/2025 a las 15:00
```

El sistema responderá con instrucciones y confirmará la cita.

## Deploy en Render

1. Push el código a GitHub
2. Conectar repositorio en [Render](https://render.com/)
3. Crear nuevo "Web Service" con `render.yaml`
4. Crear "PostgreSQL" database
5. Configurar variables de entorno:
   - `WHATSAPP_TOKEN`
   - `WHATSAPP_PHONE_NUMBER_ID`
6. Deploy!

## Desarrollo

### Estructura del Proyecto

```
chatbox/
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/chatbox/citas/
│   │       │   ├── config/         # Configuraciones
│   │       │   ├── controller/     # Controladores REST
│   │       │   ├── dto/            # Objetos de transferencia
│   │       │   ├── model/          # Entidades JPA
│   │       │   ├── repository/     # Repositorios
│   │       │   └── service/        # Lógica de negocio
│   │       └── resources/
│   │           └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/     # Componentes React
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml
└── render.yaml
```

## Troubleshooting

### WhatsApp no envía mensajes

- Verifica que el token sea válido
- Confirma que el Phone Number ID es correcto
- Revisa los logs del backend

### Recordatorios no se envían

- Verifica que `reminder.enabled=true` en application.properties
- Confirma que el @Scheduled se está ejecutando (revisa logs)
- Chequea la zona horaria del servidor

## Contribuir

Las contribuciones son bienvenidas. Por favor abre un issue primero.

## Licencia

MIT

## Soporte

Para soporte, abre un issue en GitHub.
