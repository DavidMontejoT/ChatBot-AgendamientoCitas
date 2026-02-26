import { useState } from 'react'
import CitasList from './components/CitasList'
import NuevaCitaForm from './components/NuevaCitaForm'
import TodasLasCitas from './components/TodasLasCitas'
import Dashboard from './components/Dashboard'
import WhatsappStatus from './components/WhatsappStatus'
import { Calendar, Phone, MessageSquare, BarChart3, List } from 'lucide-react'

function App() {
  const [vistaActual, setVistaActual] = useState('dashboard')
  const [telefonoBusqueda, setTelefonoBusqueda] = useState('')

  const vistas = [
    { id: 'dashboard', label: 'Dashboard', icon: <BarChart3 className="h-4 w-4" /> },
    { id: 'todas', label: 'Todas las Citas', icon: <List className="h-4 w-4" /> },
    { id: 'nueva', label: 'Nueva Cita', icon: <Calendar className="h-4 w-4" /> },
    { id: 'citas', label: 'Buscar por Teléfono', icon: <Phone className="h-4 w-4" /> },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-blue-100">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <Calendar className="h-8 w-8 text-primary-600" />
              <h1 className="text-2xl font-bold text-gray-900">
                Sistema de Citas Médicas
              </h1>
            </div>
            <div className="flex items-center space-x-2">
              <MessageSquare className="h-5 w-5 text-green-600" />
              <span className="text-sm text-gray-600">WhatsApp Integrado</span>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Navegación */}
        <div className="mb-8">
          <div className="flex flex-wrap gap-3">
            {vistas.map((vista) => (
              <button
                key={vista.id}
                onClick={() => setVistaActual(vista.id)}
                className={`px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2 ${
                  vistaActual === vista.id
                    ? 'bg-primary-600 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                }`}
              >
                {vista.icon}
                <span>{vista.label}</span>
              </button>
            ))}
          </div>

          {/* Campo de búsqueda solo para la vista de citas por teléfono */}
          {vistaActual === 'citas' && (
            <div className="flex items-center space-x-4 mt-4">
              <div className="flex-1 max-w-md">
                <input
                  type="tel"
                  placeholder="Buscar por teléfono (+52...) "
                  value={telefonoBusqueda}
                  onChange={(e) => setTelefonoBusqueda(e.target.value)}
                  className="input-field"
                />
              </div>
              <button
                onClick={() => setVistaActual('citas')}
                className="btn-primary flex items-center space-x-2"
              >
                <Phone className="h-4 w-4" />
                <span>Buscar</span>
              </button>
            </div>
          )}
        </div>

        {/* Contenido principal */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2">
            {vistaActual === 'dashboard' && <Dashboard />}
            {vistaActual === 'todas' && <TodasLasCitas />}
            {vistaActual === 'nueva' && <NuevaCitaForm />}
            {vistaActual === 'citas' && <CitasList telefono={telefonoBusqueda} />}
          </div>

          <div className="lg:col-span-1">
            <WhatsappStatus />
          </div>
        </div>
      </main>

      <footer className="bg-white border-t mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <p className="text-center text-gray-600 text-sm">
            Sistema de Citas Médicas con WhatsApp Cloud API - Dashboard Profesional
          </p>
        </div>
      </footer>
    </div>
  )
}

export default App
