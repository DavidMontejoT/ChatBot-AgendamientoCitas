import { useState, useEffect } from 'react'
import axios from 'axios'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { Calendar, Clock, User, Stethoscope, X, Search, Filter } from 'lucide-react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function TodasLasCitas() {
  const [citas, setCitas] = useState([])
  const [citasFiltradas, setCitasFiltradas] = useState([])
  const [cargando, setCargando] = useState(false)
  const [filtroEstado, setFiltroEstado] = useState('todas')
  const [busqueda, setBusqueda] = useState('')

  useEffect(() => {
    fetchCitas()
  }, [])

  useEffect(() => {
    filtrarCitas()
  }, [citas, filtroEstado, busqueda])

  const fetchCitas = async () => {
    setCargando(true)
    try {
      const response = await axios.get(`${API_URL}/api/citas/todas`)
      setCitas(response.data)
    } catch (error) {
      console.error('Error al obtener las citas:', error)
    } finally {
      setCargando(false)
    }
  }

  const filtrarCitas = () => {
    let filtradas = [...citas]

    // Filtrar por estado
    if (filtroEstado !== 'todas') {
      filtradas = filtradas.filter(c => c.estado === filtroEstado)
    }

    // Filtrar por búsqueda
    if (busqueda) {
      const busquedaLower = busqueda.toLowerCase()
      filtradas = filtradas.filter(c =>
        c.nombrePaciente.toLowerCase().includes(busquedaLower) ||
        c.telefono.includes(busqueda) ||
        c.doctor.toLowerCase().includes(busquedaLower)
      )
    }

    setCitasFiltradas(filtradas)
  }

  const cancelarCita = async (id) => {
    if (!confirm('¿Estás seguro de cancelar esta cita?')) return

    try {
      await axios.put(`${API_URL}/api/citas/${id}/cancelar`)
      await fetchCitas()
    } catch (error) {
      alert('Error al cancelar la cita')
    }
  }

  const getEstadoColor = (estado) => {
    switch (estado) {
      case 'PROGRAMADA':
        return 'bg-blue-100 text-blue-800'
      case 'CONFIRMADA':
        return 'bg-green-100 text-green-800'
      case 'CANCELADA':
        return 'bg-red-100 text-red-800'
      case 'COMPLETADA':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (cargando) {
    return (
      <div className="card text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        <p className="mt-4 text-gray-600">Cargando citas...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Encabezado */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Todas las Citas</h2>
          <p className="text-gray-600 mt-1">{citasFiltradas.length} citas encontradas</p>
        </div>
      </div>

      {/* Filtros */}
      <div className="card space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Búsqueda */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por nombre, teléfono o doctor..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="input-field pl-10"
            />
          </div>

          {/* Filtro por estado */}
          <div className="relative">
            <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <select
              value={filtroEstado}
              onChange={(e) => setFiltroEstado(e.target.value)}
              className="input-field pl-10"
            >
              <option value="todas">Todos los estados</option>
              <option value="PROGRAMADA">Programadas</option>
              <option value="CONFIRMADA">Confirmadas</option>
              <option value="COMPLETADA">Completadas</option>
              <option value="CANCELADA">Canceladas</option>
            </select>
          </div>
        </div>
      </div>

      {/* Lista de citas */}
      {citasFiltradas.length === 0 ? (
        <div className="card text-center py-12">
          <Calendar className="h-16 w-16 mx-auto text-gray-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No hay citas</h3>
          <p className="text-gray-600">No se encontraron citas con los filtros actuales</p>
        </div>
      ) : (
        <div className="space-y-4">
          {citasFiltradas.map((cita) => (
            <div
              key={cita.id}
              className="card hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <div className="flex items-center space-x-3 mb-2">
                    <h3 className="font-semibold text-gray-900">{cita.nombrePaciente}</h3>
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-medium ${getEstadoColor(
                        cita.estado
                      )}`}
                    >
                      {cita.estado}
                    </span>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm text-gray-600">
                    <div className="flex items-center space-x-2">
                      <Calendar className="h-4 w-4" />
                      <span>{format(new Date(cita.fechaHora), "dd 'de' MMMM 'de' yyyy", { locale: es })}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Clock className="h-4 w-4" />
                      <span>{format(new Date(cita.fechaHora), 'HH:mm')}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Stethoscope className="h-4 w-4" />
                      <span>{cita.doctor}</span>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2 mt-2 text-sm text-gray-600">
                    <User className="h-4 w-4" />
                    <span>{cita.telefono}</span>
                  </div>
                </div>
                {cita.estado === 'PROGRAMADA' && (
                  <button
                    onClick={() => cancelarCita(cita.id)}
                    className="text-red-600 hover:text-red-800 text-sm font-medium flex items-center space-x-1 ml-4"
                  >
                    <X className="h-4 w-4" />
                    <span>Cancelar</span>
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
