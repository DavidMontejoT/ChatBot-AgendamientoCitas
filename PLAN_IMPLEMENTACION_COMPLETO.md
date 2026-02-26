# 🎯 PLAN COMPLETO DE IMPLEMENTACIÓN
## Sociedad Urológica del Cauca - Sistema de Citas con Chatbot WhatsApp

---

## 📊 ANÁLISIS GAP (Lo que hay vs Lo que se necesita)

### ✅ LO QUE YA TENEMOS
- Backend básico con Spring Boot
- Chatbot WhatsApp simple (5 pasos)
- Panel frontend básico
- Crear/Cancelar/Ver citas
- Recordatorios automáticos
- Base de datos PostgreSQL simple

### ❌ LO QUE FALTA IMPLEMENTAR

---

## 1️⃣ BASE DE DATOS - CAMPOS ADICIONALES

### Paciente (Actual vs Requerido)
| Campo Actual | Campo Requerido | Prioridad |
|--------------|-----------------|-----------|
| nombre | ✅ (mantener) | - |
| telefono | ✅ (mantener) | - |
| email | ✅ (mantener) | - |
| ❌ tipo_identificacion | CC, TI, RC | 🔴 ALTA |
| ❌ numero_identificacion | Unique | 🔴 ALTA |
| ❌ direccion | Text | 🟡 MEDIA |
| ❌ fecha_nacimiento | Date | 🟡 MEDIA |
| ❌ eps | String | 🟡 MEDIA |
| ❌ telefono2 | String | 🟢 BAJA |

### Nuevas Entidades Necesarias
```java
// ✅ MEDICO (Con horarios)
- id
- nombre
- especialidad
- telefono
- email
- duracionCita (minutos)

// ✅ DISPONIBILIDAD (Horarios por médico)
- id
- medico_id
- diaSemana (1-7)
- horaInicio
- horaFin
- activo

// ✅ TIPO_CITA
- id
- nombre (Primera vez/Control/Cirugía)
- descripcion
- requiereDocumentos

// ✅ DOCUMENTO_PACIENTE
- id
- paciente_id
- cita_id
- tipo (Orden médica, Autorización)
- urlArchivo (S3/Cloudinary)
- fechaSubida

// ✅ USUARIO (Autenticación)
- id
- username
- password
- rol (ADMIN, RECEPCIONISTA, MEDICO)
- medico_id (si es médico)
```

---

## 2️⃣ BACKEND - NUEVOS ENDPOINTS

### Pacientes
```java
POST   /api/pacientes              // Crear con todos los campos
GET    /api/pacientes/{id}         // Obtener paciente
GET    /api/pacientes/documento/{numero}  // Buscar por documento
PUT    /api/pacientes/{id}         // Actualizar
```

### Médicos
```java
POST   /api/medicos                // Crear médico
GET    /api/medicos                // Listar todos
GET    /api/medicos/{id}            // Obtener médico
PUT    /api/medicos/{id}            // Actualizar
DELETE /api/medicos/{id}            // Eliminar
```

### Disponibilidad
```java
POST   /api/medicos/{id}/disponibilidad     // Configurar horario
GET    /api/medicos/{id}/disponibilidad     // Obtener horarios
GET    /api/disponibilidad/{fecha}         // Buscar disponibles
DELETE /api/medicos/{id}/disponibilidad/{id} // Eliminar horario
```

### Documentos
```java
POST   /api/documentos              // Subir documento
POST   /api/documentos/upload       // Upload archivo
GET    /api/pacientes/{id}/documentos  // Listar documentos
GET    /api/documentos/{id}          // Descargar
```

### Cita (Extendido)
```java
POST   /api/citas/disponibilidad    // Buscar horarios disponibles
GET    /api/citas/{id}/confirmacion   // Confirmar cita
POST   /api/citas/{id}/documento     // Adjuntar documento
```

### Email
```java
POST   /api/email/confirmacion       // Enviar confirmación
POST   /api/email/recordatorio       // Enviar recordatorio
```

---

## 3️⃣ CHATBOT WHATSAPP - FLUJO COMPLEJO

### Estados del Chatbot
```
1. MENU_PRINCIPAL
   ├── Opción 1: AGENDAR_CITA
   │   ├── PEDIR_TIPO_IDENTIFICACION
   │   ├── PEDIR_NUMERO_IDENTIFICACION
   │   ├── VALIDAR_DOCUMENTO (módulo 10)
   │   ├── PEDIR_NOMBRE
   │   ├── PEDIR_TELEFONOS
   │   ├── PEDIR_DIRECCION
   │   ├── PEDIR_FECHA_NACIMIENTO
   │   ├── PEDIR_EPS
   │   ├── PEDIR_TIPO_CITA
   │   ├── PEDIR_DOCUMENTOS (upload)
   │   ├── PEDIR_FECHA_DESEADA
   │   ├── MOSTRAR_DISPONIBILIDAD
   │   ├── SELECCIONAR_HORARIO
   │   ├── CONFIRMAR_DATOS
   │   └── CITA_CONFIRMADA
   │
   └── Opción 2: CIRUGIA_PROCEDIMIENTOS
       └── (flujo similar)
```

### Comandos Especiales
- "Atrás" → Volver al estado anterior
- "Volver" → Volver al estado anterior
- "Menu" → Ir al menú principal

### Validaciones Colombianas
```java
// Validación Cédula (Módulo 10)
boolean validarCedula(String cedula)

// Validación TI
boolean validarTI(String ti)

// Validación RC
boolean validarRC(String rc)
```

---

## 4️⃣ INTEGRACIONES EXTERNAS

### Email Service
```
Servicio: SendGrid o AWS SES
Función:
- Confirmación de cita
- Recordatorios automáticos
- Notificaciones al admin
```

### Storage Service
```
Servicio: AWS S3 o Cloudinary
Función:
- Guardar órdenes médicas
- Guardar autorizaciones EPS
- URLs firmadas por 24 horas
```

### WhatsApp Cloud API
```
Ya existe, pero extender:
- Manejo de imágenes entrantes
- Confirmaciones de cita
- Cancelaciones
```

---

## 5️⃣ FRONTEND - NUEVAS VISTAS

### Pacientes
```
- Formulario completo con 8 campos
- Validación de documentos colombianos
- Upload de documentos
- Historial de citas
```

### Médicos
```
- CRUD de médicos
- Configuración de horarios
- Vista de su calendario
- Gestión de disponibilidad
```

### Disponibilidad
```
- Calendario visual
- Ver horarios disponibles
- Selección de fecha/hora
- Vista por médico
```

### Documentos
```
- Upload drag & drop
- Vista de documentos
- Descarga de archivos
- Previsualización
```

---

## 6️⃣ AUTENTICACIÓN Y AUTORIZACIÓN

### Usuarios
```
ROLES:
- ADMIN: Todo el acceso
- RECEPCIONISTA: Citas y pacientes
- MEDICO: Solo sus citas
```

### Seguridad
```
- JWT Tokens
- Login/Logout
- Passwords encriptados (BCrypt)
- CORS configurado
```

---

## 📋 ORDEN DE IMPLEMENTACIÓN

### SEMANA 1: Base de Datos y Backend Core
1. ✅ Extender entidad Paciente (8 campos)
2. ✅ Crear entidad Medico
3. ✅ Crear entidad Disponibilidad
4. ✅ Crear entidad TipoCita
5. ✅ Crear entidad Documento
6. ✅ Crear entidad Usuario
7. ✅ Migraciones de base de datos
8. ✅ Repositorios nuevos
9. ✅ Tests de integración

### SEMANA 2: Lógica de Negocio y Algoritmos
1. ✅ Validación documentos colombianos (util)
2. ✅ Algoritmo disponibilidad (core)
3. ✅ Servicio de Pacientes extendido
4. ✅ Servicio de Médicos
5. ✅ Servicio de Disponibilidad
6. ✅ Servicio de Documentos
7. ✅ API completa de endpoints

### SEMANA 3: Chatbot Completo
1. ✅ Estados complejos del chatbot
2. ✅ Manejo de "atrás/volver"
3. ✅ Validaciones en cada paso
4. ✅ Upload de imágenes
5. ✅ Integración con disponibilidad
6. ✅ Algoritmo de selección de horarios
7. ✅ Confirmación y correo

### SEMANA 4: Frontend y Panel Admin
1. ✅ Formularios extendidos pacientes
2. ✅ CRUD de médicos
3. ✅ Configuración de horarios
4. ✅ Vista de disponibilidad
5. ✅ Upload de documentos
6. ✅ Autenticación (login)
7. ✅ Panel de administración mejorado

### SEMANA 5: Integraciones, Testing y Deploy
1. ✅ Servicio de Email (SendGrid)
2. ✅ Servicio de Storage (S3/Cloudinary)
3. ✅ Recordatorios automáticos
4. ✅ Testing completo (E2E)
5. ✅ Capacitación
6. ✅ Deploy producción
7. ✅ Documentación final

---

## 📦 ARCHIVOS A CREAR/MODIFICAR

### Backend (~30 archivos)
```
model/
  ├── Paciente.java (MODIFICAR - 8 campos)
  ├── Medico.java (NUEVO)
  ├── Disponibilidad.java (NUEVO)
  ├── TipoCita.java (NUEVO)
  ├── DocumentoPaciente.java (NUEVO)
  └── Usuario.java (NUEVO)

repository/
  ├── MedicoRepository.java (NUEVO)
  ├── DisponibilidadRepository.java (NUEVO)
  ├── TipoCitaRepository.java (NUEVO)
  ├── DocumentoPacienteRepository.java (NUEVO)
  └── UsuarioRepository.java (NUEVO)

service/
  ├── MedicoService.java (NUEVO)
  ├── DisponibilidadService.java (NUEVO)
  ├── DocumentoService.java (NUEVO)
  ├── EmailService.java (NUEVO)
  ├── ValidacionDocumentoService.java (NUEVO)
  └── AuthService.java (NUEVO)

controller/
  ├── MedicoController.java (NUEVO)
  ├── DisponibilidadController.java (NUEVO)
  ├── DocumentoController.java (NUEVO)
  ├── UsuarioController.java (NUEVO)
  └── AuthController.java (NUEVO)

dto/
  ├── PacienteRequest.java (MODIFICAR - 8 campos)
  ├── MedicoRequest.java (NUEVO)
  ├── DisponibilidadRequest.java (NUEVO)
  ├── TipoCitaRequest.java (NUEVO)
  └── LoginRequest.java (NUEVO)
```

### Frontend (~15 archivos)
```
components/
  ├── PacienteForm.jsx (NUEVO - extendido)
  ├── MedicoForm.jsx (NUEVO)
  ├── HorarioForm.jsx (NUEVO)
  ├── DisponibilidadView.jsx (NUEVO)
  ├── DocumentoUpload.jsx (NUEVO)
  ├── LoginForm.jsx (NUEVO)
  └── ProtectedRoute.jsx (NUEVO)

services/
  ├── medicoService.js (NUEVO)
  ├── disponibilidadService.js (NUEVO)
  ├── documentoService.js (NUEVO)
  ├── authService.js (NUEVO)
  └── storageService.js (NUEVO)
```

---

## 🎯 TAREAS ESPECÍFICAS POR HACER

### FASE 1: BASE DE DATOS (Día 1-2)
- [ ] Agregar campos a Paciente entity
- [ ] Crear Medico entity
- [ ] Crear Disponibilidad entity
- [ ] Crear TipoCita entity
- [ ] Crear DocumentoPaciente entity
- [ ] Crear Usuario entity
- [ ] Configurar relaciones JPA
- [ ] Crear repositorios
- [ ] Script de migración SQL

### FASE 2: VALIDACIONES (Día 3-4)
- [ ] ValidadorCedulaColombiana
- [ ] ValidadorTI
- [ ] ValidadorRC
- [ ] ValidadorTelefonoColombiano
- [ ] Tests de validaciones

### FASE 3: SERVICIOS CORE (Día 5-7)
- [ ] PacienteService extendido
- [ ] MedicoService completo
- [ ] DisponibilidadService con algoritmo
- [ ] TipoCitaService
- [ ] DocumentoService básico

### FASE 4: ENDPOINTS (Día 8-10)
- [ ] API Pacientes extendida
- [ ] API Médicos completa
- [ ] API Disponibilidad
- [ ] API Documentos upload
- [ ] API Tipos de Cita

### FASE 5: CHATBOT EXTENDIDO (Día 11-15)
- [ ] Enum extendido de estados
- [ ] Lógica de navegación (atrás/volver)
- [ ] Validaciones en cada paso
- [ ] Manejo de imágenes entrantes
- [ ] Integración con disponibilidad
- [ ] Mostrar opciones de horarios
- [ ] Confirmación con datos completos

### FASE 6: INTEGRACIONES (Día 16-18)
- [ ] Configurar SendGrid
- [ ] EmailService con templates HTML
- [ ] Configurar S3 o Cloudinary
- [ ] UploadService con URLs firmadas
- [ ] Tests de integraciones

### FASE 7: FRONTEND (Día 19-23)
- [ ] PacienteForm con 8 campos
- [ ] Validaciones en frontend
- [ ] Upload documentos con drag & drop
- [ ] CRUD Médicos
- [ ] Configuración de horarios (interfaz visual)
- [ ] Vista de disponibilidad
- [ ] Sistema de Login

### FASE 8: TESTING (Día 24-25)
- [ ] Tests unitarios servicios
- [ ] Tests integración endpoints
- [ ] Tests E2E chatbot
- [ ] Pruebas manuales completas

### FASE 9: DEPLOY (Día 26-28)
- [ ] Configurar variables entorno
- [ ] Deploy Render
- [ ] Configurar dominio
- [ ] Pruebas en producción
- [ ] Documentación

### FASE 10: ENTREGA (Día 29-30)
- [ ] Videollamada demo con cliente
- [ ] Ajustes finales solicitados
- [ ] Capacitación equipo
- [ ] Documentación completa
- [ ] Entrega formal

---

## 🚀 PRÓXIMOS PASOS INMEDIATOS

### HOY - Podemos empezar con:

1. **Extender la entidad Paciente**
   - Agregar los 8 campos faltantes
   - Commit y push

2. **Crear la entidad Medico**
   - Estructura básica
   - Repository inicial

3. **Crear la entidad Disponibilidad**
   - Para gestionar horarios
   - Relación con médico

¿Por cuál quieres empezar? 🎯
