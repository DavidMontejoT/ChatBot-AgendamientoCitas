import { useState, useEffect } from 'react'
import axios from 'axios'
import { format, parseISO, startOfDay, endOfDay } from 'date-fns'
import { es } from 'date-fns/locale'
import { Calendar, Clock, User, Stethoscope, X, Search, Filter, ChevronDown, ChevronUp } from 'lucide-react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function TodasLasCitas() {
  const [citas, setCitas] = useState([])
  const [citasFiltradas, setCitasFiltradas] = useState([])
  const [cargando, setCargando] = useState(false)
  const [filtros, setFiltros] = useState({
    estado: 'todas',
    busqueda: '',
    fechaInicio: '',
    fechaFin: '',
    doctor: '',
    ordenarPor: 'fechaDesc', // fechaDesc, fechaAsc, nombreAsc, nombreDesc
  })
  const [mostrarFiltrosAvanzados, setMostrarFiltrosAvanzados] = useState(false)

  useEffect(() => {
    fetchCitas()
  }, [])

  useEffect(() => {
    filtrarYOrdenarCitas()
  }, [citas, filtros])

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

  const filtrarYOrdenarCitas = () => {
    let filtradas = [...citas]

    // Filtrar por estado
    if (filtros.estado !== 'todas') {
      filtradas = filtradas.filter(c => c.estado === filtros.estado)
    }

    // Filtrar por búsqueda (nombre, teléfono, doctor)
    if (filtros.busqueda) {
      const busquedaLower = filtros.busqueda.toLowerCase()
      filtradas = filtradas.filter(c =>
        c.nombrePaciente?.toLowerCase().includes(busquedaLower) ||
        c.telefono?.includes(busqueda) ||
        c.doctor?.toLowerCase().includes(busquedaLower)
      )
    }

    // Filtrar por rango de fechas
    if (filtros.fechaInicio) {
      const fechaInicio = startOfDay(parseISO(filtros.fechaInicio))
      filtradas = filtradas.filter(c => {
        const fechaCita = new Date(c.fechaHora)
        return fechaCita >= fechaInicio
      })
    }

    if (filtros.fechaFin) {
      const fechaFin = endOfDay(parseISO(filtros.fechaFin))
      filtradas = filtradas.filter(c => {
        const fechaCita = new Date(c.fechaHora)
        return fechaCita <= fechaFin
      })
    }

    // Filtrar por doctor
    if (filtros.doctor) {
      filtradas = filtradas.filter(c =>
        c.doctor?.toLowerCase().includes(filtros.doctor.toLowerCase())
      )
    }

    // Ordenar
    filtradas.sort((a, b) => {
      const fechaA = new Date(a.fechaHora)
      const fechaB = new Date(b.fechaHora)

      switch (filtros.ordenarPor) {
        case 'fechaDesc':
          return fechaB - fechaA // Última a más vieja (por defecto)
        case 'fechaAsc':
          return fechaA - fechaB // Más vieja a última
        case 'nombreAsc':
          return a.nombrePaciente?.localeCompare(b.nombrePaciente)
        case 'nombreDesc':
          return b.nombrePaciente?.localeCompare(a.nombrePaciente)
        default:
          return fechaB - fechaA
      }
    })

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

  const limpiarFiltros = () => {
    setFiltros({
      estado: 'todas',
      busqueda: '',
      fechaInicio: '',
      fechaFin: '',
      doctor: '',
      ordenarPor: 'fechaDesc',
    })
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

  // Obtener lista única de doctores para el filtro
  const doctoresUnicos = [...new Set(citas.map(c => c.doctor).filter(Boolean))].sort()

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
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Todas las Citas</h2>
          <p className="text-gray-600 mt-1">
            {citasFiltradas.length} de {citas.length} citas encontradas
          </p>
        </div>
        <button
          onClick={() => setMostrarFiltrosAvanzados(!mostrarFiltrosAvanzados)}
          className="btn-secondary flex items-center space-x-2"
        >
          <Filter className="h-4 w-4" />
          <span>Filtros Avanzados</span>
          {mostrarFiltrosAvanzados ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
        </button>
      </div>

      {/* Filtros */}
      <div className="card space-y-4">
        {/* Filtros básicos */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Búsqueda general */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por nombre, teléfono..."
              value={filtros.busqueda}
              onChange={(e) => setFiltros({ ...filtros, busqueda: e.target.value })}
              className="input-field pl-10"
            />
          </div>

          {/* Filtro por estado */}
          <div className="relative">
            <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <select
              value={filtros.estado}
              onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}
              className="input-field pl-10"
            >
              <option value="todas">Todos los estados</option>
              <option value="PROGRAMADA">Programadas</option>
              <option value="CONFIRMADA">Confirmadas</option>
              <option value="COMPLETADA">Completadas</option>
              <option value="CANCELADA">Canceladas</option>
            </select>
          </div>

          {/* Ordenamiento */}
          <div className="relative">
            <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-4 text-gray-400" />
            <select
              value={filtros.ordenarPor}
              onChange={(e) => setFiltros({ ...filtros, ordenarPor: e.target.value })}
              className="input-field pl-10"
            >
              <option value="fechaDesc">📅 Fecha: ↓ Recientes primero</option>
              <option value="fechaAsc">📅 Fecha: ↑ Antiguas primero</option>
              <option value="nombreAsc">👤 Nombre: A → Z</option>
              <option value="nombreDesc">👤 Nombre: Z → A</option>
            </select>
          </div>
        </div>

        {/* Filtros avanzados */}
        {mostrarFiltrosAvanzados && (
          <div className="border-t pt-4 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Filtro por fecha inicio */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Desde:
                </label>
                <input
                  type="date"
                  value={filtros.fechaInicio}
                  onChange={(e) => setFiltros({ ...filtros, fechaInicio: e.target.value })}
                  className="input-field"
                />
              </div>

              {/* Filtro por fecha fin */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Hasta:
                </label>
                <input
                  type="date"
                  value={filtros.fechaFin}
                  onChange={(e) => setFiltros({ ...filtros, fechaFin: e.target.value })}
                  className="input-field"
                />
              </div>

              {/* Filtro por doctor */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Doctor:
                </label>
                <select
                  value={filtros.doctor}
                  onChange={(e) => setFiltros({ ...filtros, doctor: e.target.value })}
                  className="input-field"
                >
                  <option value="">Todos los doctores</option>
                  {doctoresUnicos.map(doctor => (
                    <option key={doctor} value={doctor}>{doctor}</option>
                  ))}
                </select>
              </div>
            </div>

            {/* Botón limpiar filtros */}
            <div className="flex justify-end">
              <button
                onClick={limpiarFiltros}
                className="text-sm text-gray-600 hover:text-gray-800 underline"
              >
                Limpiar todos los filtros
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Lista de citas */}
      {citasFiltradas.length === 0 ? (
        <div className="card text-center py-12">
          <Calendar className="h-16 w-16 mx-auto text-gray-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No hay citas</h3>
          <p className="text-gray-600">
            {citas.length === 0 ? 'No hay citas registradas' : 'No se encontraron citas con los filtros actuales'}
          </p>
          {(filtros.busqueda || filtros.estado !== 'todas' || filtros.fechaInicio || filtros.fechaFin || filtros.doctor) && (
            <button
              onClick={limpiarFiltros}
              className="mt-4 text-primary-600 hover:text-primary-800 font-medium"
            >
              Limpiar filtros
            </button>
          )}
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
                  <div className="flex items-center space-x-3 mb-2 flex-wrap gap-2">
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
                      <Calendar className="h-4 w-4 flex-shrink-0" />
                      <span>{format(new Date(cita.fechaHora), "dd 'de' MMMM 'de' yyyy", { locale: es })}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Clock className="h-4 w-4 flex-shrink-0" />
                      <span>{format(new Date(cita.fechaHora), 'HH:mm')}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Stethoscope className="h-4 w-4 flex-shrink-0" />
                      <span>{cita.doctor}</span>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2 mt-2 text-sm text-gray-600">
                    <User className="h-4 w-4 flex-shrink-0" />
                    <span>{cita.telefono}</span>
                  </div>
                </div>
                {cita.estado === 'PROGRAMADA' && (
                  <button
                    onClick={() => cancelarCita(cita.id)}
                    className="text-red-600 hover:text-red-800 text-sm font-medium flex items-center space-x-1 ml-4 flex-shrink-0"
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
