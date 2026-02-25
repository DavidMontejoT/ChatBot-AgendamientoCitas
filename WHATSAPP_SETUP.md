# Configuración de WhatsApp Cloud API

Guía paso a paso para configurar WhatsApp Cloud API para el Sistema de Citas Médicas.

## ¿Qué es WhatsApp Cloud API?

WhatsApp Cloud API es la versión cloud de la WhatsApp Business API que permite:
- Enviar mensajes programáticos
- Recibir mensajes vía webhooks
- Sin necesidad de servidor propio

## Paso 1: Crear Cuenta de Meta Developer

1. Ve a [Meta for Developers](https://developers.facebook.com/)
2. Clic en "Get Started" o "Iniciar sesión"
3. Inicia sesión con tu cuenta de Facebook (o crea una nueva)
4. Completa tu perfil de desarrollador

## Paso 2: Crear una Nueva App

1. En el dashboard, clic en "Create App"
2. Selecciona tipo de app: **"Business"**
3. Ingresa los detalles:
   - **App name**: "Sistema Citas Médicas" (o el nombre que prefieras)
   - **Contact email**: tu email
4. Clic en "Create App"

## Paso 3: Agregar Producto WhatsApp

1. En el dashboard de tu app, busca la sección "Add products"
2. Localiza **"WhatsApp"** y clic en "Add product"
3. Seleccionar **"WhatsApp Cloud API"**
4. Clic en "Continue" y luego "Set up"

## Paso 4: Configurar WhatsApp

### 4.1 Seleccionar Número de Teléfono

1. Clic en "Get started" en la sección WhatsApp
2. Elige entre:
   - **Usar número existente**: Si ya tienes un número de WhatsApp Business
   - **Crear número nuevo**: Obtener un nuevo número

### 4.2 Agregar Número de Teléfono

Si vas a crear un número nuevo:

1. Clic en "Add phone number"
2. Selecciona país (ej: México +52)
3. Ingresa un número de teléfono
4. Verifica el número ingresando el código recibido por SMS o llamada

### 4.3 Obtener credenciales

Una vez configurado el número, verás:

1. **Phone Number ID**
   - En la sección "WhatsApp API Setup"
   - Copia este valor (lo necesitarás más tarde)

2. **Access Token**
   - Clic en "Manage" o "Edit" en Permanent Token
   - Puede que debas crear un nuevo token:
     - Clic en "Create a permanent access token"
     - Selecciona el número de teléfono
     - Clic en "Generate token"
   - **IMPORTANTE**: Copia y guarda este token (no se mostrará de nuevo)

### 4.4 Configurar Webhook (Opcional - Requerido para recibir mensajes)

El webhook permite que tu backend reciba mensajes enviados por los pacientes.

#### En el servidor local (desarrollo):

```bash
# Instalar ngrok
brew install ngrok  # Mac
# o descargar de ngrok.com

# Iniciar tunel
ngrok http 8080
```

Copia la URL HTTPS generada (ej: `https://abc123.ngrok.io`)

#### Configurar en Meta:

1. En tu app de WhatsApp, ve a "Webhooks"
2. Clic en "Edit" o "Configure webhook"
3. Ingresa:
   - **Webhook URL**: `https://tu-url.com/api/whatsapp/webhook`
   - **Verify Token**: `chatbox_verify_token_2024`
4. Clic en "Verify and Save"

5. Suscribirse a campos:
   - Selecciona tu número de teléfono
   - Suscríbete a: `messages`

## Paso 5: Configurar Variables de Entorno

### En Desarrollo (Local)

Crea el archivo `backend/.env`:

```env
WHATSAPP_TOKEN=tu_token_acceso_permanente_aqui
WHATSAPP_PHONE_NUMBER_ID=tu_phone_number_id_aqui
```

### En Producción (Render)

1. Ve al dashboard de Render
2. Selecciona tu servicio
3. Ve a "Environment"
4. Agrega las variables:
   - `WHATSAPP_TOKEN`: tu token
   - `WHATSAPP_PHONE_NUMBER_ID`: tu phone number ID

## Paso 6: Probar la Integración

### Enviar Mensaje de Prueba

Puedes probar usando la API directamente:

```bash
curl -X POST 'https://graph.facebook.com/v18.0/YOUR_PHONE_NUMBER_ID/messages' \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "messaging_product": "whatsapp",
    "to": "+521234567890",
    "type": "text",
    "text": {
      "body": "¡Hola! Este es un mensaje de prueba del sistema de citas."
    }
  }'
```

### Probar con el Backend

1. Inicia el backend
2. Crea una cita nueva vía API o frontend
3. Deberías recibir un mensaje de confirmación en WhatsApp

### Probar Webhook

1. Envía un mensaje desde tu teléfono personal al número de WhatsApp configurado
2. El backend debería recibir el mensaje vía webhook
3. Revisa los logs del backend para ver el mensaje recibido

## Troubleshooting

### Error: Token inválido

- Verifica que el token sea el "Permanent access token"
- Confirma que no hay espacios extras
- Regenera el token si es necesario

### Error: Phone Number ID incorrecto

- Verifica que estás usando el ID, no el número de teléfono
- El ID es algo como: `123456789012345`

### Webhook no se verifica

- Verifica que la URL sea accesible públicamente
- Confirma que el verify token sea: `chatbox_verify_token_2024`
- Revisa los logs del backend

### Mensajes no llegan

- Verifica que el número esté verificado en Meta
- Confirma que el número de destino incluya el código de país (+52)
- Revisa los límites de la API (tier de uso)

## Límites y Costos

### Sandbox (Gratis)

- **Mensajes por día**: 1000 conversaciones de usuario (24 horas)
- **Número de destinatarios**: Ilimitado
- **Vigencia**: Permanente

### Niveles de Uso (Tiers)

Conforme crezcas, puedes actualizar a tiers pagos:

| Tier | Conversaciones/mes | Precio |
|------|-------------------|--------|
| Starter | 1,000 | Gratis |
| Tier 1 | 10,000 | ~$100-150 USD |
| Tier 2 | 100,000 | ~$800-1000 USD |
| Tier 3 | 1,000,000 | ~$7000-8000 USD |

Nota: Los precios varían por país. Consulta [Pricing de WhatsApp](https://developers.facebook.com/docs/whatsapp/pricing)

## Seguridad

### Mejores Prácticas

1. **Nunca commits de tokens en Git**
   - Usa `.env` files
   - Agrega `.env` a `.gitignore`

2. **Rotación de Tokens**
   - Cambia los tokens periódicamente
   - Regenera si hay sospecha de compromiso

3. **IP Whitelisting** (si está disponible)
   - Configura IPs permitidas en Meta

4. **Logs Seguros**
   - No logs completos de tokens
   - Mask tokens sensibles en logs

## Recursos Adicionales

- [Documentación Oficial WhatsApp Cloud API](https://developers.facebook.com/docs/whatsapp/cloud-api/)
- [Guía de Inicio Rápido](https://developers.facebook.com/docs/whatsapp/cloud-api/get-started)
- [Webhooks WhatsApp](https://developers.facebook.com/docs/whatsapp/cloud-api/webhooks)
- [Send API Reference](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/send)

## Soporte

Si tienes problemas:

1. Revisa los logs del backend
2. Verifica la configuración en Meta for Developers
3. Consulta la [documentación oficial](https://developers.facebook.com/docs/whatsapp/cloud-api/)
4. Abre un issue en GitHub
