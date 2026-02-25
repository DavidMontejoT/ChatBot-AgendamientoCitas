# Documentación de la API

Documentación completa de los endpoints REST del Sistema de Citas Médicas.

## Base URL

- **Desarrollo**: `http://localhost:8080`
- **Producción**: `https://citas-backend.onrender.com`

## Autenticación

Actualmente la API no requiere autenticación. Para producción, considera agregar:
- JWT tokens
- OAuth 2.0
- API keys

## Headers

Todos los endpoints POST/PUT deben incluir:

```
Content-Type: application/json
```

## Response Codes

| Code | Descripción |
|------|-------------|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request - Validación falló |
| 404 | Not Found - Recurso no existe |
| 500 | Internal Server Error |

---

## Endpoints

### Health Check

Verifica que el API esté funcionando.

```http
GET /api/citas/health
```

**Response:**
```json
"API funcionando correctamente"
```

---

### Citas

#### Crear Nueva Cita

Crea una nueva cita y envía confirmación por WhatsApp.

```http
POST /api/citas
```

**Request Body:**
```json
{
  "nombrePaciente": "Juan Pérez",
  "telefono": "+521234567890",
  "email": "juan@example.com",
  "fechaHora": "2025-12-31T15:00:00",
  "doctor": "Dr. Pérez"
}
```

**Campos:**

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| nombrePaciente | string | Sí | Nombre completo del paciente |
| telefono | string | Sí | Teléfono con código de país (+52...) |
| email | string | No | Email del paciente |
| fechaHora | datetime | Sí | Fecha y hora en ISO 8601 |
| doctor | string | Sí | Nombre del doctor |

**Validaciones:**

- `telefono`: Debe empezar con `+` y tener formato internacional
- `fechaHora`: Debe ser una fecha futura
- Todos los campos marcados como requeridos

**Response 201 Created:**
```json
{
  "id": 1,
  "nombrePaciente": "Juan Pérez",
  "telefono": "+521234567890",
  "email": "juan@example.com",
  "fechaHora": "2025-12-31T15:00:00",
  "doctor": "Dr. Pérez",
  "estado": "PROGRAMADA",
  "creadoEn": "2024-02-24T10:00:00"
}
```

**WhatsApp:**
Se envía automáticamente un mensaje de confirmación:
```
¡Hola Juan Pérez! ✅ Tu cita ha sido agendada correctamente.

📅 Fecha: 31/12/2025
⏰ Hora: 15:00
👨‍⚕️ Doctor: Dr. Pérez

Te enviaremos recordatorios antes de tu cita. ¡No olvides asistir!
```

---

#### Obtener Cita por ID

```http
GET /api/citas/{id}
```

**Parámetros:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID de la cita |

**Response 200 OK:**
```json
{
  "id": 1,
  "nombrePaciente": "Juan Pérez",
  "telefono": "+521234567890",
  "email": "juan@example.com",
  "fechaHora": "2025-12-31T15:00:00",
  "doctor": "Dr. Pérez",
  "estado": "PROGRAMADA",
  "creadoEn": "2024-02-24T10:00:00"
}
```

**Response 404 Not Found:**
```json
{
  "message": "Cita no encontrada"
}
```

---

#### Obtener Citas por Paciente

Lista todas las citas de un paciente ordenadas por fecha (más recientes primero).

```http
GET /api/citas/paciente/{telefono}
```

**Parámetros:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| telefono | string | Teléfono del paciente |

**Response 200 OK:**
```json
[
  {
    "id": 2,
    "nombrePaciente": "Juan Pérez",
    "telefono": "+521234567890",
    "email": "juan@example.com",
    "fechaHora": "2025-12-31T15:00:00",
    "doctor": "Dr. Pérez",
    "estado": "PROGRAMADA",
    "creadoEn": "2024-02-24T10:00:00"
  },
  {
    "id": 1,
    "nombrePaciente": "Juan Pérez",
    "telefono": "+521234567890",
    "email": "juan@example.com",
    "fechaHora": "2024-02-20T10:00:00",
    "doctor": "Dr. López",
    "estado": "COMPLETADA",
    "creadoEn": "2024-02-20T09:00:00"
  }
]
```

---

#### Cancelar Cita

Cancela una cita existente.

```http
PUT /api/citas/{id}/cancelar
```

**Parámetros:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID de la cita |

**Response 200 OK:**
```json
{
  "id": 1,
  "nombrePaciente": "Juan Pérez",
  "telefono": "+521234567890",
  "email": "juan@example.com",
  "fechaHora": "2025-12-31T15:00:00",
  "doctor": "Dr. Pérez",
  "estado": "CANCELADA",
  "creadoEn": "2024-02-24T10:00:00"
}
```

**WhatsApp:**
Se envía mensaje de cancelación:
```
Tu cita del 31/12/2025 a las 15:00 ha sido cancelada.
Si deseas reagendar, contáctanos.
```

---

### WhatsApp

#### Enviar Mensaje

Envía un mensaje manual vía WhatsApp.

```http
POST /api/whatsapp/enviar
```

**Request Body:**
```json
{
  "from": "+521234567890",
  "message": "Hola, este es un mensaje de prueba"
}
```

**Campos:**

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| from | string | Sí | Teléfono destino (+52...) |
| message | string | Sí | Contenido del mensaje |

**Response 200 OK:**
```json
{
  "message": "Mensaje enviado correctamente"
}
```

---

#### Verificar Webhook (GET)

Usado por Meta para verificar la URL del webhook.

```http
GET /api/whatsapp/webhook?hub.verify_token={token}&hub.challenge={challenge}
```

**Parámetros:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| hub.verify_token | string | Token de verificación |
| hub.challenge | string | Challenge de Meta |

**Response:**
- 200 OK con el challenge en el body si el token es correcto
- 401 Unauthorized si el token es incorrecto

---

#### Recibir Webhook (POST)

Recibe mensajes entrantes de WhatsApp.

```http
POST /api/whatsapp/webhook
```

**Request Body:** (formato de Meta)

```json
{
  "object": "whatsapp_business_account",
  "entry": [
    {
      "id": "123456789",
      "changes": [
        {
          "value": {
            "messaging_product": "whatsapp",
            "metadata": {
              "display_phone_number": "+15551234567"
            },
            "contacts": [
              {
                "profile": {
                  "name": "Juan Pérez"
                },
                "wa_id": "521234567890"
              }
            ],
            "messages": [
              {
                "from": "521234567890",
                "id": "wamid.ID",
                "timestamp": "1708800000",
                "text": {
                  "body": "Quiero cita"
                }
              }
            ]
          },
          "field": "messages"
        }
      ]
    }
  ]
}
```

**Response:** 200 OK (vacío)

**Procesamiento:**
El sistema analiza el mensaje y responde automáticamente:
- Si contiene "cita" o "agendar": Envía instrucciones de agendamiento
- Otro mensaje: Respuesta genérica

---

## Estados de Cita

| Estado | Descripción |
|--------|-------------|
| PROGRAMADA | Cita agendada, pendiente de realizarse |
| CONFIRMADA | Cita confirmada por el paciente |
| CANCELADA | Cita fue cancelada |
| COMPLETADA | Cita fue realizada |

---

## Errores Comunes

### 400 Bad Request

```json
{
  "timestamp": "2024-02-24T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La fecha debe ser futura",
  "path": "/api/citas"
}
```

**Causas comunes:**
- Validación fallida
- Fecha en el pasado
- Formato de teléfono incorrecto

### 404 Not Found

```json
{
  "message": "Cita no encontrada"
}
```

**Causas comunes:**
- ID no existe
- Teléfono no tiene citas

### 500 Internal Server Error

```json
{
  "timestamp": "2024-02-24T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error conectando a WhatsApp API",
  "path": "/api/citas"
}
```

**Causas comunes:**
- Error de conexión a base de datos
- Error en WhatsApp API
- Configuración incorrecta

---

## Ejemplos de Uso

### cURL

**Crear cita:**
```bash
curl -X POST http://localhost:8080/api/citas \
  -H "Content-Type: application/json" \
  -d '{
    "nombrePaciente": "María García",
    "telefono": "+525512345678",
    "email": "maria@example.com",
    "fechaHora": "2025-03-15T10:30:00",
    "doctor": "Dra. Martínez"
  }'
```

**Obtener citas del paciente:**
```bash
curl http://localhost:8080/api/citas/paciente/+525512345678
```

**Cancelar cita:**
```bash
curl -X PUT http://localhost:8080/api/citas/1/cancelar
```

### JavaScript (Axios)

```javascript
import axios from 'axios'

const API_URL = 'http://localhost:8080'

// Crear cita
const crearCita = async (cita) => {
  try {
    const response = await axios.post(`${API_URL}/api/citas`, cita)
    console.log('Cita creada:', response.data)
    return response.data
  } catch (error) {
    console.error('Error:', error.response.data)
  }
}

// Obtener citas
const obtenerCitas = async (telefono) => {
  try {
    const response = await axios.get(
      `${API_URL}/api/citas/paciente/${telefono}`
    )
    return response.data
  } catch (error) {
    console.error('Error:', error.response.data)
  }
}

// Usar
crearCita({
  nombrePaciente: 'Juan Pérez',
  telefono: '+521234567890',
  email: 'juan@example.com',
  fechaHora: '2025-12-31T15:00:00',
  doctor: 'Dr. Pérez'
})
```

### Python (requests)

```python
import requests
from datetime import datetime, timedelta

API_URL = 'http://localhost:8080'

def crear_cita(cita):
    response = requests.post(f'{API_URL}/api/citas', json=cita)
    return response.json()

def obtener_citas(telefono):
    response = requests.get(f'{API_URL}/api/citas/paciente/{telefono}')
    return response.json()

# Usar
cita = {
    'nombrePaciente': 'Juan Pérez',
    'telefono': '+521234567890',
    'email': 'juan@example.com',
    'fechaHora': (datetime.now() + timedelta(days=7)).isoformat(),
    'doctor': 'Dr. Pérez'
}

resultado = crear_cita(cita)
print(resultado)
```

---

## Rate Limiting

Actualmente no hay rate limiting implementado. Para producción, considera agregar:

- Spring Boot Starter Actuator
- Configurar límites por IP
- Implementar token bucket algorithm

## Versionado

La API actual es v1. Futuras versiones pueden incluir:

- `/api/v2/citas` - Con autenticación
- `/api/v2/doctores` - Gestión de doctores
- `/api/v2/horarios` - Horarios disponibles

## Soporte

Para issues o preguntas:
- Abrir un issue en GitHub
- Revisar logs del backend
- Consultar documentación de Spring Boot
