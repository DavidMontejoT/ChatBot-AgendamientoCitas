# Guía de Deploy en Producción

Guía completa para desplegar el Sistema de Citas Médicas en Render.

## Pre-Deploy Checklist

Antes de hacer deploy, asegúrate de:

- [ ] Tener cuenta en [Render](https://render.com/)
- [ ] Código subido a GitHub
- [ ] WhatsApp Cloud API configurado (ver WHATSAPP_SETUP.md)
- [ ] Variables de entorno documentadas

## Opción 1: Deploy en Render (Recomendado - Gratis)

### Paso 1: Preparar Repositorio en GitHub

```bash
# Inicializar git si es necesario
git init

# Agregar archivos
git add .

# Commit inicial
git commit -m "Initial commit: Sistema de Citas Médicas MVP"

# Crear repositorio en GitHub y conectar
git remote add origin https://github.com/tu-usuario/chatbox.git
git branch -M main
git push -u origin main
```

### Paso 2: Crear Database en Render

1. Ve a [Render Dashboard](https://dashboard.render.com/)
2. Clic en "New" → "PostgreSQL"
3. Configura:
   - **Name**: `citas-medicas-db`
   - **Database**: `citas_medicas`
   - **User**: `citas_user`
   - **Region**: La más cercana a tus usuarios
   - **Plan**: Free (suficiente para MVP)
4. Clic en "Create Database"

Guarda las credenciales que te muestra Render.

### Paso 3: Desplegar Backend

1. En Render Dashboard, clic en "New" → "Web Service"
2. Conecta tu repositorio de GitHub
3. Configura:
   - **Name**: `citas-backend`
   - **Environment**: Docker
   - **Region**: Misma región que la DB
   - **Branch**: `main`
   - **Dockerfile Path**: `./backend/Dockerfile`

4. Variables de Entorno:

   Agrega las siguientes variables en "Environment":

   ```bash
   # Database (Render las llena automáticamente si conectas la DB)
   DATABASE_URL=postgresql://citas_user:password@host:5432/citas_medicas
   SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/citas_medicas
   SPRING_DATASOURCE_USERNAME=citas_user
   SPRING_DATASOURCE_PASSWORD=tu_password_db

   # WhatsApp (Manual)
   WHATSAPP_TOKEN=tu_whatsapp_access_token
   WHATSAPP_PHONE_NUMBER_ID=tu_phone_number_id

   # Application
   SERVER_PORT=8080
   SPRING_PROFILES_ACTIVE=prod
   ```

5. Clic en "Create Web Service"

Espera a que el build termine (primer deploy puede tomar 5-10 minutos).

### Paso 4: Verificar Deploy

1. En el dashboard de Render, clic en tu servicio
2. Copia la URL (ej: `https://citas-backend.onrender.com`)
3. Probar health check:

```bash
curl https://citas-backend.onrender.com/api/citas/health
```

Deberías devolver: `"API funcionando correctamente"`

### Paso 5: Configurar Webhook de WhatsApp

1. Ve a [Meta for Developers](https://developers.facebook.com/)
2. Ve a tu app de WhatsApp
3. Configura webhook:
   - **URL**: `https://citas-backend.onrender.com/api/whatsapp/webhook`
   - **Verify Token**: `chatbox_verify_token_2024`
4. Clic en "Verify and Save"

### Paso 6: Probar Integración Completa

1. Crear una cita:

```bash
curl -X POST https://citas-backend.onrender.com/api/citas \
  -H "Content-Type: application/json" \
  -d '{
    "nombrePaciente": "Paciente Test",
    "telefono": "+521234567890",
    "fechaHora": "2025-12-31T15:00:00",
    "doctor": "Dr. Test"
  }'
```

2. Deberías recibir un mensaje de confirmación en WhatsApp.

## Opción 2: Deploy del Frontend (Vite + React)

### Opción A: Deploy en Vercel (Recomendado)

1. Ve a [Vercel](https://vercel.com/)
2. Importa tu repositorio
3. Configura:
   - **Root Directory**: `frontend`
   - **Framework Preset**: Vite
4. Agrega variable de entorno:
   - `VITE_API_URL`: `https://citas-backend.onrender.com`
5. Deploy!

### Opción B: Deploy en Render

1. Crea nuevo "Web Service"
2. Root Directory: `frontend`
3. Build Command: `npm run build`
4. Start Command: `npm run preview`

### Opción C: Deploy en Netlify

1. Ve a [Netlify](https://www.netlify.com/)
2. "Add new site" → "Import an existing project"
3. Conecta tu repo
4. Configura:
   - **Build command**: `npm run build`
   - **Publish directory**: `frontend/dist`
5. Agrega variable: `VITE_API_URL`

## Opción 3: Deploy Completo con render.yaml

El proyecto incluye un archivo `render.yaml` que configura todo automáticamente.

### Usar Blueprint de Render

1. Ve a [Render Dashboard](https://dashboard.render.com/)
2. Clic en "New" → "Blueprint"
3. Conecta tu repositorio
4. Selecciona el archivo `render.yaml`
5. Review resources y clic en "Apply Blueprint"

Esto creará automáticamente:
- Database PostgreSQL
- Backend Service
- Configuración de variables de entorno

## Monitoreo y Logs

### Ver Logs en Render

1. Ve al servicio en Render Dashboard
2. Clic en "Logs"
3. Puedes filtrar por:
   - `stdout` - Logs normales
   - `stderr` - Logs de error

### Logs Típicos

**Éxito:**
```
2024-02-24 10:00:00 INFO  o.s.b.w.EmbeddedServletContainer : Tomcat started on port(s): 8080
2024-02-24 10:00:00 INFO  com.chatbox.citas.CitasApplication  : Started CitasApplication in 15.5s
```

**Error DB:**
```
ERROR HHH000346: Error during schema action
java.sql.SQLException: Connection refused
```

### Métricas de Render

- **CPU**: Máximo 100% (free tier)
- **Memory**: 512MB (free tier)
- **Request time**: Idealmente < 500ms
- **Uptime**: Debería ser > 99%

## Configuración de Dominio Personalizado

### Paso 1: Comprar Dominio

Compra en Namecheap, GoDaddy, etc.

### Paso 2: Configurar DNS

Agrega registros en tu proveedor de dominio:

```
Type: CNAME
Name: api
Value: <tu-render-app>.onrender.com

Type: CNAME
Name: www
Value: <tu-frontend>.vercel.app (si usas Vercel)
```

### Paso 3: Configurar en Render

1. Ve a tu servicio en Render
2. Settings → Custom Domains
3. Agrega: `api.tu-dominio.com`
4. Render te dará registros DNS adicionales

### Paso 4: Actualizar Webhook de WhatsApp

En Meta for Developers:
- URL: `https://api.tu-dominio.com/api/whatsapp/webhook`

## Escalado

### Cuando escalar (Signos)

- CPU > 80% frecuente
- Request time > 1s
- Muchos 50x errors
- Uptime < 95%

### Opciones de Escalado en Render

**Plan Free:**
- 512MB RAM
- 0.1 CPU
- 750 horas/mes
- Spin-down luego de 15min inactividad

**Plan Starter ($7/mes):**
- 512MB RAM
- 0.5 CPU
- Sin spin-down

**Plan Standard ($25/mes):**
- 2GB RAM
- 1 CPU
- Better performance

## Backups

### Backups Automáticos (Render)

- **Free tier**: Sin backups automáticos
- **Starter+**: Backups diarios

### Backups Manuales

```bash
# Exportar base de datos
pg_dump $DATABASE_URL > backup_$(date +%Y%m%d).sql

# Importar base de datos
psql $DATABASE_URL < backup_20240224.sql
```

### Usar Render Dashboard

1. Ve a la database
2. Clic en "Backups"
3. "Download" o "Restore"

## Seguridad en Producción

### 1. Variables de Entorno

Nunca hardcodear credenciales:

```java
// MAL
String token = "EAABwzLixnjYBAO...";

// BIEN
@Value("${whatsapp.api.token}")
private String apiToken;
```

### 2. CORS

Configura correctamente:

```properties
cors.allowed-origins=https://tu-frontend.com
```

### 3. Rate Limiting (Futuro)

Considerar agregar:
- Spring Boot Starter Actuator + Rate limiting

### 4. HTTPS

Render proporciona HTTPS automático. Asegúrate de:
- No usar HTTP en producción
- Redirigir HTTP → HTTPS

## Actualizaciones y Deploy Continuo

### Flujo de Trabajo

```bash
# 1. Hacer cambios
git checkout -b feature/nueva-funcionalidad

# 2. Commit y push
git add .
git commit -m "Add new feature"
git push origin feature/nueva-funcionalidad

# 3. Pull request en GitHub

# 4. Después de aprobación, merge a main

# 5. Render hace deploy automático
```

### Deploy Manual

Si necesitas hacer deploy manual:

1. En Render Dashboard
2. Clic en "Manual Deploy"
3. Selecciona branch y commit
4. Clic en "Deploy"

## Troubleshooting

### El servicio no inicia

**Verificar:**
1. Logs en Render
2. Variables de entorno configuradas
3. Database accessible
4. Suficientes recursos (CPU/RAM)

**Solución:**
- Upgrade plan si es necesario
- Optimizar código
- Agregar más memoria

### Error 502 Bad Gateway

**Causas:**
- Servidor no responde
- Database timeout
- Out of memory

**Soluciones:**
- Verificar health check
- Revisar connection pool de DB
- Aumentar recursos

### Webhook no funciona

**Verificar:**
1. URL correcta
2. Verify token correcto
3. Servidor responde a GET request
4. Firewall no bloquea

**Probar:**
```bash
curl https://tu-backend.com/api/whatsapp/webhook?hub.verify_token=chatbox_verify_token_2024&hub.challenge=test
```

## Costos Estimados

### MVP (Free Tier)

- **Database**: $0 (Render free tier)
- **Backend**: $0 (Render free tier)
- **Frontend**: $0 (Vercel/Netlify free tier)
- **WhatsApp**: $0 (sandbox - 1000 conversaciones)
- **Dominio**: $10-15/año (opcional)

**Total: ~$15/año**

### Producción Ligera

- **Database**: $7/mes (Render Starter)
- **Backend**: $7/mes (Render Starter)
- **Frontend**: $0
- **WhatsApp**: Variable según uso
- **Dominio**: $10-15/año

**Total: ~$20-30/mes + WhatsApp**

## Soporte

- [Render Documentation](https://render.com/docs)
- [Render Community](https://community.render.com)
- [Meta for Developers](https://developers.facebook.com/)
- GitHub Issues del proyecto
