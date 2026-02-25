# Guía de Deploy del Frontend en Render

## Opción 1: Static Site (RECOMENDADO - Más Simple y Gratis)

Render tiene una opción específica para sitios estáticos que es perfecta para React + Vite.

### Paso 1: Preparar el entorno

Asegúrate de tener el archivo `.env` en el frontend con la URL de tu backend:

```bash
cd frontend
cat > .env << EOF
VITE_API_URL=https://citas-backend.onrender.com
EOF
```

### Paso 2: Ir a Render

1. Ve a https://dashboard.render.com/
2. Clic en **"New+"** → **"Static Site"**

### Paso 3: Configurar el Static Site

**Connect Repository:**
- Selecciona tu repo: `ChatBot-AgendamientoCitas`

**Name:**
- `citas-frontend`

**Root Directory:**
- `frontend`

**Build Command:**
- `npm install && npm run build`

**Publish Directory:**
- `dist`

**Environment:**
- **Add Environment Variable**:
  - Key: `VITE_API_URL`
  - Value: `https://citas-backend.onrender.com` (tu backend URL)

**Instance Type:**
- **Free** (al principio)

### Paso 4: Create Site

Clic en **"Create Static Site"** y espera el build.

---

## Opción 2: Web Service con Node (Más completo)

Si prefieres más control o necesitas características adicionales.

### Paso 1: Nuevo Web Service

1. Ve a Render Dashboard
2. Clic en **"New+"** → **"Web Service"**

### Paso 2: Configurar

**Name:** `citas-frontend`

**Runtime:** **Node**

**Build Command:**
```bash
cd frontend && npm install && npm run build
```

**Start Command:**
```bash
cd frontend && npm run preview
```

**Environment:**
- **VITE_API_URL** = `https://citas-backend.onrender.com`

---

## 🔧 Configuración Importante

### Archivo frontend/.env

Crea este archivo en tu máquina local:

```bash
VITE_API_URL=https://citas-backend.onrender.com
```

### Actualizar vite.config.js (si es necesario)

Verifica que el `vite.config.js` tenga esta configuración para producción:

```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true // Para exponer en la red
  }
})
```

---

## ✅ Después del Deploy

### Paso 1: Obtener la URL

Render te dará una URL como:
```
https://citas-frontend.onrender.com
```

### Paso 2: Probar

1. Abre la URL en tu navegador
2. Deberías ver el panel de citas
3. Intenta agendar una cita
4. Verifica que se conecte al backend

### Paso 3: Verificar CORS

Si tienes errores de CORS, asegúrate de que en el backend (`application.properties`) esté configurado:

```properties
cors.allowed-origins=https://citas-frontend.onrender.com,https://*.onrender.com
```

---

## 🌐 Dominio Personalizado (Opcional)

### Paso 1: Comprar dominio
Compra en Namecheap, GoDaddy, etc.

### Paso 2: Configurar en Render

1. Ve al servicio `citas-frontend` en Render
2. Settings → **Custom Domains**
3. Agrega: `www.tu-dominio.com`

### Paso 3: DNS

En tu proveedor de dominio, agrega registros CNAME:
```
Type: CNAME
Name: www
Value: citas-frontend.onrender.com
```

---

## 📊 Comparación de Opciones

| Opción | Ventajas | Desventajas |
|--------|-----------|-------------|
| **Static Site** | Gratis, más simple, build rápido | Menos control |
| **Web Service** | Más control, servidor Node real | Usa más recursos |
| **Vercel** | Mejor para React, deploy instantáneo | Otra plataforma |

---

## 🎯 Recomendación

**Para tu MVP, usa Opción 1 (Static Site)** porque:
- ✅ 100% Gratis
- ✅ Build más rápido
- ✅ Perfecto para React + Vite
- ✅ CDN incluido
- ✅ HTTPS automático

---

## ✅ Checklist Antes del Deploy

- [ ] frontend/.env existe con VITE_API_URL
- [ ] package.json tiene "build": "vite build"
- [ ] vite.config.js está configurado
- [ ] El backend está funcionando
- [ ] La URL del backend es correcta

---

## 🚀 Depura el Frontend

### Para probar localmente con la URL de producción:

```bash
cd frontend
npm install
npm run build
npm run preview
```

Abre http://localhost:4173

---

## 📞 Si Tienes Problemas

### Error: Cannot find module 'vite'

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Error: Build falla

Revisa los logs en Render. Si faltan dependencias:

```bash
npm install date-fns lucide-react
```

### Error: No se conecta al backend

1. Verifica que VITE_API_URL sea correcta
2. Verifica CORS en el backend
3. Revisa los logs del navegador (F12 → Console)

---

**¿Listo para hacer deploy?** 🚀

Cuéntame qué opción prefieres (Static Site o Web Service) y te guío paso a paso.
