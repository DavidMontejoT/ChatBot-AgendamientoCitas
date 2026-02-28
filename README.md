# 🏥 ChatBot - Sistema de Citas Médicas con WhatsApp

Sistema completo para agendamiento de citas médicas con chatbot de WhatsApp, panel de administración web y recordatorios automáticos.

## 🎯 Estado Actual del Proyecto

**Versión:** 2.0 (Refactorizado)
**Última actualización:** Febrero 2025
**Estado:** ✅ **PRODUCCIÓN** - Funcionando en Render

### 📊 Progreso de Refactorización SOLID

| Módulo | Estado | Progreso | Notas |
|--------|--------|----------|-------|
| **Backend** | ✅ Completado | 100% | Refactorización SOLID terminada |
| - WhatsAppService | ✅ Completado | 100% | Dividido en 5 servicios especializados |
| - EmailService | ✅ Completado | 100% | API HTTP de Brevo implementada |
| - Controladores | ✅ Completado | 100% | Sin lógica de negocio |
| - Seguridad | ✅ Completado | 100% | Config validation, CORS configurado |
| - Constants | ✅ Completado | 100% | 3 clases de constantes creadas |
| - Error Handling | ✅ Completado | 100% | Logging mejorado, excepciones específicas |
| **Frontend** | 🔄 Parcial | 60% | Servicios y utils creados, pendiente split componentes |
| - Servicios API | ✅ Completado | 100% | api.js, citaService.js, doctorService.js |
| - Utilidades | ✅ Completado | 100% | colors.js, formatting.js, validation.js |
| - Componentes | 🔄 Pendiente | 30% | Componentes grandes sin dividir |
| - Custom Hooks | ❌ Pendiente | 0% | Sin implementar |
| - Context Providers | ❌ Pendiente | 0% | Props drilling presente |
| - Toast/Modals | ❌ Pendiente | 0% | Todavía usa alert() |
| **Testing** | ❌ Pendiente | 0% | Sin tests unitarios ni de integración |
| **Performance** | ❌ Pendiente | 0% | Sin optimizaciones |

---

## ✨ Características Implementadas

### ✅ Funcionalidades Core
- [x] Agendamiento de citas vía WhatsApp (chatbot conversacional de 14 pasos)
- [x] Agendamiento de citas vía web (panel de administración)
- [x] Confirmación inmediata por WhatsApp
- [x] Confirmación por Email (API Brevo)
- [x] Recordatorios automáticos (24h y 1h antes)
- [x] Cancelación de citas
- [x] CRUD de Doctores (con validación)
- [x] CRUD de Pacientes
- [x] Dashboard con estadísticas
- [x] Gestión de disponibilidad por doctor/fecha/hora

### ✅ Integraciones
- [x] WhatsApp Cloud API (Meta)
- [x] Brevo Email API (HTTP - funciona en plataformas con restricciones de puertos)
- [x] PostgreSQL con JPA/Hibernate
- [x] Programación de tareas (Spring @Scheduled)

### ✅ Arquitectura Backend
- [x] **SOLID Principles** aplicados
  - [x] Single Responsibility (SRP)
  - [x] Open/Closed (OCP)
  - [x] Liskov Substitution (LSP)
  - [x] Interface Segregation (ISP)
  - [x] Dependency Inversion (DIP)
- [x] **Servicios Modularizados**
  - [x] ConversationStateService (gestión de estado)
  - [x] WhatsAppMessageService (API communication)
  - [x] WhatsAppTemplateService (formateo de mensajes)
  - [x] WhatsAppFlowService (lógica del chatbot)
  - [x] WhatsAppOrchestratorService (coordinador)
  - [x] EmailTemplateService (templates HTML)
  - [x] BrevoEmailApiService (API HTTP de emails)
- [x] **Constantes Centralizadas** (WhatsAppConstants, EmailConstants, AppointmentConstants)
- [x] **Mappers** (DoctorMapper para conversión Entity-DTO)
- [x] **Validación de Configuración** (@PostConstruct en WhatsAppConfig)
- [x] **Error Handling** (logging detallado, excepciones específicas)

---

## 🛠 Stack Tecnológico

### Backend
```
Java 17
├── Spring Boot 3.2.0
├── Spring Data JPA (Hibernate)
├── Spring WebFlux (WebClient para WhatsApp API)
├── Spring Scheduling (Quartz)
├── PostgreSQL 14+
├── Lombok
├── Maven
└── Jackson (JSON)
```

### Frontend
```
React 18
├── Vite 5.0
├── Tailwind CSS 3.x
├── Axios (HTTP client)
├── Lucide React Icons
├── React Router DOM
└── JavaScript ES6+
```

### Deploy & Infraestructura
```
Producción:
├── Render (Web Service + PostgreSQL)
├── GitHub (Repositorio + CI/CD)
└── Brevo (Email API)

Desarrollo:
├── Docker Compose
├── Maven (Spring Boot)
└── NPM (React)
```

---

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+ (OpenJDK o Oracle)
- Maven 3.6+
- Node.js 18+ y NPM
- PostgreSQL 14+ o Docker
- Git

### 1. Clonar el Repositorio

```bash
git clone https://github.com/DavidMontejoT/ChatBot-AgendamientoCitas.git
cd ChatBot-AgendamientoCitas
```

### 2. Configurar Variables de Entorno

#### Backend (`backend/src/main/resources/application.properties`)

```properties
# Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/citas_medicas
spring.datasource.username=postgres
spring.datasource.password=tu_password

# WhatsApp Cloud API
whatsapp.api.token=TU_WHATSAPP_ACCESS_TOKEN
whatsapp.api.phone-number-id=TU_PHONE_NUMBER_ID
whatsapp.api.verify-token=chatbox_verify_token_2024

# Brevo Email API
brevo.api.key=xkeysib-TU_API_KEY_AQUI

# Email remitente
app.email.from=tu_email@gmail.com
app.email.from-name=Tu Clínica
```

#### Frontend (`frontend/.env`)

```bash
VITE_API_URL=http://localhost:8080
```

### 3. Ejecutar con Docker Compose (Recomendado)

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Backend: http://localhost:8080
# Frontend: http://localhost:5173
# PostgreSQL: localhost:5432
```

### 4. Ejecutar Localmente (Sin Docker)

#### Backend

```bash
cd backend

# Asegúrate de tener PostgreSQL corriendo
mvn clean install
mvn spring-boot:run
```

#### Frontend

```bash
cd frontend

npm install
npm run dev
```

---

## 📱 Configuración de WhatsApp

### 1. Crear App en Meta for Developers

1. Ve a [Meta for Developers](https://developers.facebook.com/)
2. Crea una nueva app → Selecciona "Business"
3. Agrega el producto "WhatsApp"

### 2. Configurar WhatsApp Cloud API

1. En tu dashboard de WhatsApp, obtén:
   - **WhatsApp Access Token** (permanente, sin expiración)
   - **Phone Number ID** (del número de WhatsApp Business)

2. Configura el webhook:
   - **URL:** `https://tu-backend.onrender.com/api/whatsapp/webhook`
   - **Verify Token:** `chatbox_verify_token_2024`

3. Suscríbete a los campos:
   - `messages`

### 3. Suscribir a Webhooks

En Meta for Developers → WhatsApp → Configuration:
- Suscribir a: `messages`
- Configurar URL del webhook

---

## 📧 Configuración de Email (Brevo)

### Obtener API Key para HTTP

1. Ve a [Brevo Dashboard](https://app.brevo.com/)
2. navegá a **SMTP & API** → **API Keys**
3. Clic en **"Generate a new API key"**
4. Configura permisos:
   - ✅ Campaigns data
   - ✅ Transactional emails
5. La clave debe empezar con `xkeysib-...` (NO `xsmtpsib-...`)

⚠️ **Importante:** Las claves SMTP (`xsmtpsib-`) NO funcionan con la API HTTP. Necesitas una clave que empiece con `xkeysib-`.

### Variables de Entorno en Producción (Render)

```
BREVO_API_KEY=xkeysib-xxxxxxxxxxxxxxxxxxxxxxxxxxx
BREVO_SMTP_USERNAME=tu_username@smtp-brevo.com
```

---

## 🏗 Arquitectura Backend (Post-Refactorización)

### Estructura de Paquetes

```
src/main/java/com/chatbox/citas/
├── config/
│   ├── HttpClientConfig.java          # RestTemplate Bean
│   ├── CorsConfig.java                # Configuración CORS global
│   └── WhatsAppConfig.java            # Configuración WhatsApp + validación
├── constants/
│   ├── WhatsAppConstants.java         # Constants WhatsApp
│   ├── EmailConstants.java            # Constants Email
│   └── AppointmentConstants.java      # Constants Citas
├── controller/
│   ├── CitaController.java            # Endpoints de citas
│   ├── DoctorController.java          # Endpoints de doctores
│   └── WhatsAppController.java       # Webhook de WhatsApp
├── dto/
│   ├── CitaRequest.java               # DTO creación de cita
│   ├── CitaRequestCompleto.java       # DTO completo (con paciente)
│   ├── CitaResponse.java              # Respuesta de cita
│   ├── DoctorRequest.java             # DTO doctor
│   └── DoctorResponse.java            # Respuesta doctor
├── mapper/
│   └── DoctorMapper.java              # Entity-DTO mapper
├── model/
│   ├── Cita.java                      # Entidad Cita
│   ├── Doctor.java                    # Entidad Doctor
│   └── Paciente.java                  # Entidad Paciente
├── repository/
│   ├── CitaRepository.java            # Repository citas
│   ├── DoctorRepository.java          # Repository doctores
│   └── PacienteRepository.java        # Repository pacientes
└── service/
    ├── email/
    │   ├── BrevoEmailApiService.java # API HTTP de Brevo
    │   └── EmailTemplateService.java  # Templates HTML
    ├── whatsapp/
    │   ├── ConversationStateService.java       # Gestión estado conversaciones
    │   ├── WhatsAppMessageService.java         # Comunicación API WhatsApp
    │   ├── WhatsAppTemplateService.java        # Formateo mensajes
    │   ├── WhatsAppFlowService.java             # Lógica chatbot (14 pasos)
    │   └── WhatsAppOrchestratorService.java     # Coordinador
    ├── CitaService.java               # Lógica de negocio de citas
    ├── DoctorService.java             # Lógica de negocio de doctores
    ├── PacienteService.java           # Lógica de negocio de pacientes
    ├── RecordatorioService.java       # Recordatorios automáticos
    ├── ValidacionDatosService.java    # Validaciones de datos
    ├── ValidacionDocumentoService.java # Validación documentos
    └── EmailService.java              # Orquestador de emails
```

### Servicios WhatsApp - Detalle

| Servicio | Responsabilidad | LOC (aprox) |
|----------|----------------|-------------|
| `ConversationStateService` | Estado conversaciones, timeouts, cleanup | 242 |
| `WhatsAppMessageService` | API communication, envío de mensajes | 103 |
| `WhatsAppTemplateService` | Formateo y templates de mensajes | 319 |
| `WhatsAppFlowService` | Lógica del chatbot, máquina de estados | 534 |
| `WhatsAppOrchestratorService` | Coordinador, webhook processing | 131 |

**Total antes:** 972 líneas (1 archivo monolítico)
**Total después:** ~1,329 líneas (5 archivos modularizados)
**Beneficio:** +357 líneas pero 5x más mantenible, testeable y escalable

---

## 📋 Roadmap - Próximos Pasos

### 🚀 Prioridad ALTA (Sprint 3 - Frontend Refactoring)

#### 1. Split Componentes Grandes
**Archivos a refactorizar:**
- [ ] `DoctoresCRUD.jsx` (347 líneas) → dividir en:
  - `DoctorList.jsx`
  - `DoctorForm.jsx`
  - `DoctorFilterBar.jsx`
- [ ] `TodasLasCitas.jsx` (356 líneas) → dividir en:
  - `CitaList.jsx`
  - `CitaFilters.jsx`
  - `CitaCard.jsx`
  - `CitaStats.jsx`
- [ ] `Dashboard.jsx` (236 líneas) → dividir en:
  - `StatsCards.jsx`
  - `ChartsSection.jsx`
  - `RecentActivity.jsx`

#### 2. Custom Hooks
- [ ] `hooks/useApi.js` - Hook genérico para API calls
- [ ] `hooks/useCitas.js` - Hook específico para citas
- [ ] `hooks/useDoctores.js` - Hook específico para doctores
- [ ] `hooks/useLocalStorage.js` - Hook para localStorage

#### 3. Context Providers
- [ ] `contexts/AppContext.jsx` - Estado global de la app
- [ ] `contexts/NotificationContext.jsx` - Sistema de toasts
- [ ] Eliminar props drilling (especialmente prop `telefono`)

#### 4. Reemplazar alert() con Toast/Modals
- [ ] Instalar `react-hot-toast` o implementar toast manual
- [ ] Reemplazar todos los `alert()` del código
- [ ] Agregar ErrorBoundary para manejo de errores

### 🔧 Prioridad MEDIA (Sprint 4 - Performance & Quality)

#### 5. Backend - Scheduler de Cleanup
- [ ] `scheduler/CleanupScheduler.java`
  - Limpiar conversaciones expiradas cada hora
  - Limpiar mensajesProcesados antiguos
  - Evitar memory leaks

#### 6. Backend - Testing
- [ ] Tests unitarios de servicios
  - `ConversationStateServiceTest`
  - `WhatsAppMessageServiceTest`
  - `EmailServiceTest`
- [ ] Tests de integración
  - `WhatsAppFlowTest`
  - `CitaServiceTest`
- [ ] Tests de controladores
  - `CitaControllerTest`
  - `DoctorControllerTest`

#### 7. Frontend - Performance
- [ ] Agregar `useMemo` y `useCallback`
- [ ] Code splitting con `React.lazy()`
- [ ] Virtual scrolling para listas largas
- [ ] Optimización de re-renders

### 📚 Prioridad BAJA (Sprint 5 - Mejoras Opcionales)

#### 8. Documentación & DevEx
- [ ] Swagger/OpenAPI documentation
- [ ] Javadoc en todos los métodos públicos
- [ ] Diagramas de arquitectura (Mermaid)
- [ ] Guía de contribución detallada

#### 9. Features Adicionales
- [ ] Autenticación y autorización (Spring Security)
- [ ] Roles de usuario (admin, doctor, paciente)
- [ ] Exportar citas a PDF/Excel
- [ ] Gráficos de estadísticas avanzadas
- [ ] Sistema de calificaciones de doctores
- [ ] Historial de cambios de citas (auditoría)

#### 10. Infraestructura
- [ ] GitHub Actions para CI/CD
- [ ] Automated testing en PRs
- [ ] Code quality checks (SonarQube)
- [ ] Monitoring y alertas (Sentry, New Relic)

---

## 🧪 Testing

### Ejecutar Tests (Cuando se implementen)

```bash
# Backend Tests
cd backend
mvn test

# Frontend Tests
cd frontend
npm test
```

---

## 📦 Deploy en Producción

### Render (Automático)

1. **Push a main branch:**
   ```bash
   git push origin main
   ```

2. **Render detecta el cambio y hace deploy automático**

3. **Configurar variables de entorno en Render:**
   ```
   DATABASE_URL
   JDBC_DATABASE_URL
   WHATSAPP_TOKEN
   WHATSAPP_PHONE_NUMBER_ID
   BREVO_API_KEY
   CORS_ALLOWED_ORIGINS
   ```

### Manual Deploy

1. Ve a [Render Dashboard](https://dashboard.render.com/)
2. Selecciona tu servicio
3. Clic en **"Manual Deploy"**
4. Selecciona la rama **main**
5. Clic en **"Deploy"**

---

## 📊 API Endpoints

### Citas

```
POST   /api/citas                      # Crear cita simple
POST   /api/citas/completa             # Crear cita completa (con paciente)
GET    /api/citas/{id}                 # Obtener cita por ID
GET    /api/citas/paciente/{telefono}  # Listar citas por paciente
GET    /api/citas/todas                 # Listar todas las citas
PUT    /api/citas/{id}/cancelar         # Cancelar cita
GET    /api/citas/disponibilidad?fecha  # Consultar disponibilidad
```

### Doctores

```
GET    /api/doctores                   # Listar todos
GET    /api/doctores/activos           # Listar activos
GET    /api/doctores/especialidad/{esp} # Filtrar por especialidad
GET    /api/doctores/{id}              # Obtener por ID
POST   /api/doctores                   # Crear doctor
PUT    /api/doctores/{id}              # Actualizar doctor
DELETE /api/doctores/{id}              # Eliminar doctor
```

### WhatsApp

```
POST   /api/whatsapp/enviar             # Enviar mensaje manual
GET    /api/whatsapp/webhook            # Verificar webhook (Meta)
POST   /api/whatsapp/webhook            # Recibir mensajes de WhatsApp
POST   /api/whatsapp/test-email         # Probar email (debug)
```

---

## 🐛 Troubleshooting

### WhatsApp no envía mensajes

- [ ] Verifica que `WHATSAPP_TOKEN` sea válido y no haya expirado
- [ ] Confirma que `WHATSAPP_PHONE_NUMBER_ID` es correcto
- [ ] Revisa que el webhook esté configurado correctamente en Meta
- [ ] Verifica los logs del backend: `/api/citas/health`
- [ ] Prueba el endpoint: `POST /api/whatsapp/enviar`

### Emails no llegan

- [ ] Verifica que `BREVO_API_KEY` empiece con `xkeysib-` (no `xsmtpsib-`)
- [ ] Confirma que la API key tenga permisos de "Transactional emails"
- [ ] Revisa los logs para ver errores 401 Unauthorized
- [ ] Prueba el endpoint: `POST /api/whatsapp/test-email`

### Recordatorios no se envían

- [ ] Verifica que `reminder.enabled=true` en application.properties
- [ ] Confirma que el @Scheduled se está ejecutando (revisa logs)
- [ ] Chequea la zona horaria del servidor (Render usa UTC)
- [ ] Asegúrate de que las citas tengan fecha futura

### Error: "Conversation timeout"

- [ ] Las conversaciones expiran después de 30 minutos de inactividad
- [ ] El paciente debe enviar cualquier mensaje para reiniciar
- [ ] Ajusta `whatsapp.conversation.timeout-minutes` si es necesario

---

## 📈 Métricas del Proyecto

### Código

- **Backend:** ~3,500 líneas de Java
- **Frontend:** ~2,500 líneas de JavaScript/JSX
- **Total:** ~6,000 líneas de código

### Cobertura

- **Backend Refactorizado:** 100% (todos los servicios SOLID)
- **Frontend Refactorizado:** 60% (servicios y utils completos)
- **Tests:** 0% (pendiente implementar)

### Dependencias

- **Backend:** 15 dependencias Maven
- **Frontend:** 8 dependencias NPM

---

## 🤝 Contribuir

### Guidelines

1. **Code Style:**
   - Backend: Seguir convenciones de Java (Google Java Style)
   - Frontend: Seguir convenciones de Airbnb React/JS

2. **Commit Messages:**
   - Usar formato Conventional Commits:
     ```
     feat: agregar nueva funcionalidad
     fix: corregir error
     refactor: mejorar código sin cambiar funcionalidad
     docs: actualizar documentación
     ```

3. **Pull Requests:**
   - Crear branch desde `main`: `feature/tu-feature`
   - Tests requeridos para nuevas funcionalidades
   - Code review obligatorio antes de merge

4. **SOLID Principles:**
   - Todas las nuevas clases deben seguir SOLID
   - Máximo 200 líneas por clase/método
   - Una responsabilidad por clase

---

## 📝 Changelog

### [2.0.0] - Febrero 2025 - Refactorización SOLID

**Added:**
- ✅ Servicios modularizados de WhatsApp (5 servicios)
- ✅ API HTTP de Brevo para emails
- ✅ Constantes centralizadas (3 clases)
- ✅ Validación de configuración al inicio
- ✅ Error handling mejorado
- ✅ Mapper Entity-DTO
- ✅ Servicios API en frontend
- ✅ Utilidades en frontend (colors, formatting, validation)

**Changed:**
- ✅ Refactorizado WhatsAppService (972 líneas → 5 servicios)
- ✅ EmailService ahora usa API HTTP (no SMTP)
- ✅ Controladores limpios (sin lógica de negocio)
- ✅ Aplicados principios SOLID en todo el backend

**Fixed:**
- ✅ Memory leaks en conversaciones
- ✅ Bloqueo de puertos SMTP en Render
- ✅ Security issues (CORS, input validation)
- ✅ Silent exceptions sin logging

### [1.0.0] - Enero 2025 - MVP Inicial

**Features:**
- Chatbot de WhatsApp conversacional
- Panel de administración web
- Recordatorios automáticos
- CRUD de doctores y citas

---

## 📄 Licencia

MIT License - Ver archivo [LICENSE](LICENSE) para detalles

---

## 👥 Soporte

- **Issues:** [GitHub Issues](https://github.com/DavidMontejoT/ChatBot-AgendamientoCitas/issues)
- **Email:** davidmontejotorres5@gmail.com
- **WhatsApp:** +57 301 318 8696 (Sociedad Urológica del Cauca)

---

## 🙏 Agradecimientos

- **Meta** - WhatsApp Cloud API
- **Brevo** - Servicios de email API
- **Render** - Plataforma de hosting
- **Spring Boot** - Framework backend
- **React** - Framework frontend

---

**Desarrollado con ❤️ para la Sociedad Urológica del Cauca**

*Este proyecto es el resultado de una refactorización completa aplicando principios SOLID y mejores prácticas de desarrollo de software.*
