import { useState } from 'react'
import CitasList from './components/CitasList'
import NuevaCitaForm from './components/NuevaCitaForm'
import WhatsappStatus from './components/WhatsappStatus'
import { Calendar, Phone, MessageSquare } from 'lucide-react'

function App() {
  const [vistaActual, setVistaActual] = useState('citas')
  const [telefonoBusqueda, setTelefonoBusqueda] = useState('')

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
        <div className="mb-8">
          <div className="flex space-x-4 mb-6">
            <button
              onClick={() => setVistaActual('nueva')}
              className={`px-6 py-3 rounded-lg font-medium transition-colors ${
                vistaActual === 'nueva'
                  ? 'bg-primary-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              Nueva Cita
            </button>
            <button
              onClick={() => setVistaActual('citas')}
              className={`px-6 py-3 rounded-lg font-medium transition-colors ${
                vistaActual === 'citas'
                  ? 'bg-primary-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              Mis Citas
            </button>
          </div>

          {vistaActual === 'citas' && (
            <div className="flex items-center space-x-4">
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

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2">
            {vistaActual === 'nueva' ? <NuevaCitaForm /> : <CitasList telefono={telefonoBusqueda} />}
          </div>

          <div className="lg:col-span-1">
            <WhatsappStatus />
          </div>
        </div>
      </main>

      <footer className="bg-white border-t mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <p className="text-center text-gray-600 text-sm">
            Sistema MVP de Citas Médicas con WhatsApp Cloud API
          </p>
        </div>
      </footer>
    </div>
  )
}

export default App
