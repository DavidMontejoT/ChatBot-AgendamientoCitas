# Guía de Pruebas Locales

Guía para probar el sistema localmente ANTES de hacer deploy.

## Opción 1: Probar Solo Backend (Sin WhatsApp)

### Paso 1: Iniciar con Docker Compose

```bash
cd chatbox
docker-compose up -d
```

Esto iniciará:
- PostgreSQL en puerto 5432
- Backend API en puerto 8080

### Paso 2: Verificar Backend

```bash
curl http://localhost:8080/api/citas/health
```

Debería responder: `"API funcionando correctamente"`

### Paso 3: Probar Crear Cita

```bash
curl -X POST http://localhost:8080/api/citas \
  -H "Content-Type: application/json" \
  -d '{
    "nombrePaciente": "Juan Pérez",
    "telefono": "+521234567890",
    "email": "juan@test.com",
    "fechaHora": "2025-12-31T15:00:00",
    "doctor": "Dr. Test"
  }'
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "nombrePaciente": "Juan Pérez",
  "telefono": "+521234567890",
  "email": "juan@test.com",
  "fechaHora": "2025-12-31T15:00:00",
  "doctor": "Dr. Test",
  "estado": "PROGRAMADA",
  "creadoEn": "2024-02-24T10:00:00"
}
```

**Nota**: El mensaje de WhatsApp fallará si no tienes credenciales válidas, pero la cita se guardará en la BD.

### Paso 4: Verificar en la Base de Datos

```bash
# Conectar a PostgreSQL
docker exec -it citas-db psql -U postgres -d citas_medicas

# Ver citas
SELECT * FROM citas;

# Ver pacientes
SELECT * FROM pacientes;

# Salir
\q
```

### Paso 5: Probar Frontend

```bash
cd frontend
npm install
npm run dev
```

Abre http://localhost:5173 y prueba:
- Agendar nueva cita
- Buscar citas por teléfono

---

## Opción 2: Probar WhatsApp Localmente (Con ngrok)

### Requisitos Previos

1. Tener credenciales de WhatsApp Cloud API (ver WHATSAPP_SETUP.md)
2. ngrok instalado

### Paso 1: Instalar ngrok

```bash
# Mac con Homebrew
brew install ngrok

# O descargar de https://ngrok.com/download
```

### Paso 2: Iniciar Backend

```bash
cd backend
mvn spring-boot:run
```

El backend estará en http://localhost:8080

### Paso 3: Crear Túnel ngrok

```bash
ngrok http 8080
```

**Output esperado:**
```
Forwarding  https://abc123.ngrok.io -> http://localhost:8080
```

**Copia la URL HTTPS**: `https://abc123.ngrok.io`

### Paso 4: Configurar Webhook en Meta

1. Ve a [Meta for Developers](https://developers.facebook.com/)
2. Tu App → WhatsApp → Configuration
3. Webhook:
   - **URL**: `https://abc123.ngrok.io/api/whatsapp/webhook`
   - **Verify Token**: `chatbox_verify_token_2024`
4. Clic en "Verify and Save"

### Paso 5: Suscribirse a Eventos

1. En el mismo webhook configuration
2. Suscribir a campos: `messages`

### Paso 6: Configurar Credenciales

Crea `backend/.env`:

```env
WHATSAPP_TOKEN=tu_token_permanente
WHATSAPP_PHONE_NUMBER_ID=tu_phone_id
```

O pasa las variables al ejecutar:

```bash
export WHATSAPP_TOKEN=tu_token
export WHATSAPP_PHONE_NUMBER_ID=tu_phone_id
mvn spring-boot:run
```

### Paso 7: Probar WhatsApp

**Opción A: Enviar un mensaje desde tu teléfono**

1. Envía un mensaje al número de WhatsApp Business configurado
2. Deberías recibir una respuesta automática
3. Revisa los logs del backend

**Opción B: Crear cita vía API**

```bash
curl -X POST http://localhost:8080/api/citas \
  -H "Content-Type: application/json" \
  -d '{
    "nombrePaciente": "Test WhatsApp",
    "telefono": "+5255YOURNUMBER",
    "fechaHora": "2025-12-31T15:00:00",
    "doctor": "Dr. Test"
  }'
```

Deberías recibir un WhatsApp en tu teléfono.

### Paso 8: Revisar Logs

```bash
# Logs del backend
# Deberías ver algo como:
Mensaje enviado a +5255YOURNUMBER: {...}
Message received from +5255YOURNUMBER: Quiero cita
```

---

## Opción 3: Probar con Frontend Local + ngrok

### Paso 1: Backend con ngrok

```bash
# Terminal 1
cd backend
ngrok http 8080
```

Copia la URL HTTPS de ngrok.

### Paso 2: Configurar Frontend

Edita `frontend/.env`:

```env
VITE_API_URL=https://abc123.ngrok.io
```

### Paso 3: Iniciar Frontend

```bash
# Terminal 2
cd frontend
npm install
npm run dev
```

### Paso 4: Probar

1. Abre http://localhost:5173
2. Crea una cita nueva
3. Deberías recibir confirmación por WhatsApp

---

## Troubleshooting Pruebas Locales

### ngrok no funciona

**Problema**: URL de ngrok no responde

**Solución**:
- Verifica que ngrok esté corriendo
- Revisa que el backend esté en puerto 8080
- Prueba la URL de ngrok en el navegador

### Webhook verification falla

**Problema**: Meta dice que el verify token es incorrecto

**Solución**:
- Verifica que sea: `chatbox_verify_token_2024`
- Revisa los logs del backend
- Confirma que ngrok esté corriendo

### WhatsApp no envía mensajes

**Problema**: Error 401 o 403 al enviar mensajes

**Solución**:
- Verifica que el token sea correcto
- Confirma que el Phone Number ID sea correcto
- Revisa que el token tenga permisos necesarios
- El token debe ser "Permanent access token"

### Mensajes no llegan

**Problema**: No recibes mensajes en tu teléfono

**Solución**:
- Verifica el número de destino (con +52)
- Confirma que el número de WhatsApp Business esté verificado
- Revisa los límites del tier gratuito (1000 conversaciones)

---

## Checklist Antes de Deploy

- [ ] Backend funciona localmente
- [ ] Frontend funciona localmente
- [ ] Puedes crear citas vía API
- [ ] Las citas se guardan en BD
- [ ] WhatsApp envía confirmaciones (con ngrok)
- [ ] Webhook recibe mensajes (con ngrok)
- [ ] Recordatorios funcionan (ver logs)

---

## Mock de WhatsApp (Para desarrollo sin credenciales)

Si NO tienes credenciales de WhatsApp, puedes simular el envío:

### 1. Comentar servicio de WhatsApp

En `CitaService.java`, comenta:

```java
// whatsAppService.enviarConfirmacionCita(...);
```

### 2. Verificar en logs en su lugar

```java
log.info("MOCK: Enviar confirmación a {}: {}", telefono, mensaje);
```

Esto te permite probar toda la lógica sin necesitar WhatsApp.

---

## Próximos Pasos

Una vez que las pruebas locales funcionen:

1. **Deploy backend en Render** (ver DEPLOYMENT.md)
2. **Deploy frontend en Vercel** (ver DEPLOYMENT.md)
3. **Configurar webhook con URL de producción**
4. **Probar flujo completo en producción**

---

## Resumen Rápido

| Prueba | ¿Funciona Local? | ¿Requiere ngrok? | ¿Requiere WhatsApp? |
|--------|------------------|------------------|---------------------|
| Health check | ✅ Sí | ❌ No | ❌ No |
| Crear cita | ✅ Sí | ❌ No | ⚠️ Parcial* |
| Enviar WhatsApp | ❌ No | ✅ Sí | ✅ Sí |
| Recibir WhatsApp | ❌ No | ✅ Sí | ✅ Sí |
| Frontend | ✅ Sí | ❌ No | ⚠️ Parcial* |

*Sin credenciales válidas de WhatsApp, la lógica funciona pero no se envían mensajes reales.
