import { useState, useEffect } from 'react'
import axios from 'axios'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { Plus, Pencil, Trash2, Search, Stethoscope, Mail, Phone, User, CheckCircle, XCircle } from 'lucide-react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function DoctoresCRUD() {
  const [doctores, setDoctores] = useState([])
  const [cargando, setCargando] = useState(false)
  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [modoEdicion, setModoEdicion] = useState(false)
  const [doctorSeleccionado, setDoctorSeleccionado] = useState(null)
  const [busqueda, setBusqueda] = useState('')
  const [mostrarSoloActivos, setMostrarSoloActivos] = useState(true)

  const [formulario, setFormulario] = useState({
    nombre: '',
    especialidad: '',
    telefono: '',
    email: ''
  })

  useEffect(() => {
    fetchDoctores()
  }, [mostrarSoloActivos])

  const fetchDoctores = async () => {
    setCargando(true)
    try {
      const endpoint = mostrarSoloActivos ? '/api/doctores/activos' : '/api/doctores'
      const response = await axios.get(`${API_URL}${endpoint}`)
      setDoctores(response.data)
    } catch (error) {
      console.error('Error al obtener doctores:', error)
      alert('Error al cargar los doctores')
    } finally {
      setCargando(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    try {
      if (modoEdicion && doctorSeleccionado) {
        await axios.put(`${API_URL}/api/doctores/${doctorSeleccionado.id}`, formulario)
        alert('Doctor actualizado correctamente')
      } else {
        await axios.post(`${API_URL}/api/doctores`, formulario)
        alert('Doctor creado correctamente')
      }

      cerrarFormulario()
      fetchDoctores()
    } catch (error) {
      console.error('Error al guardar doctor:', error)
      alert('Error al guardar el doctor')
    }
  }

  const handleEditar = (doctor) => {
    setFormulario({
      nombre: doctor.nombre,
      especialidad: doctor.especialidad,
      telefono: doctor.telefono || '',
      email: doctor.email || ''
    })
    setDoctorSeleccionado(doctor)
    setModoEdicion(true)
    setMostrarFormulario(true)
  }

  const handleEliminar = async (doctor) => {
    if (!confirm(`¿Estás seguro de eliminar al Dr. ${doctor.nombre}?`)) return

    try {
      await axios.delete(`${API_URL}/api/doctores/${doctor.id}`)
      alert('Doctor eliminado correctamente')
      fetchDoctores()
    } catch (error) {
      console.error('Error al eliminar doctor:', error)
      alert('Error al eliminar el doctor')
    }
  }

  const abrirFormularioNuevo = () => {
    setFormulario({ nombre: '', especialidad: '', telefono: '', email: '' })
    setDoctorSeleccionado(null)
    setModoEdicion(false)
    setMostrarFormulario(true)
  }

  const cerrarFormulario = () => {
    setMostrarFormulario(false)
    setFormulario({ nombre: '', especialidad: '', telefono: '', email: '' })
    setDoctorSeleccionado(null)
    setModoEdicion(false)
  }

  const doctoresFiltrados = doctores.filter(doctor =>
    doctor.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
    doctor.especialidad.toLowerCase().includes(busqueda.toLowerCase())
  )

  if (cargando) {
    return (
      <div className="card text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        <p className="mt-4 text-gray-600">Cargando doctores...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Encabezado */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Gestión de Doctores</h2>
          <p className="text-gray-600 mt-1">
            {mostrarSoloActivos
              ? `${doctores.length} doctores activos`
              : `${doctores.length} doctores totales`
            }
          </p>
        </div>
        <button
          onClick={abrirFormularioNuevo}
          className="btn-primary flex items-center space-x-2"
        >
          <Plus className="h-4 w-4" />
          <span>Nuevo Doctor</span>
        </button>
      </div>

      {/* Filtros */}
      <div className="card space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Búsqueda */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por nombre o especialidad..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="input-field pl-10"
            />
          </div>

          {/* Toggle mostrar activos */}
          <div className="flex items-center space-x-4">
            <label className="flex items-center space-x-2 cursor-pointer">
              <input
                type="checkbox"
                checked={mostrarSoloActivos}
                onChange={(e) => setMostrarSoloActivos(e.target.checked)}
                className="w-4 h-4 text-primary-600 rounded focus:ring-primary-500"
              />
              <span className="text-sm font-medium text-gray-700">
                Mostrar solo activos
              </span>
            </label>
          </div>
        </div>
      </div>

      {/* Formulario */}
      {mostrarFormulario && (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">
              {modoEdicion ? 'Editar Doctor' : 'Nuevo Doctor'}
            </h3>
            <button
              onClick={cerrarFormulario}
              className="text-gray-400 hover:text-gray-600"
            >
              <XCircle className="h-5 w-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Nombre */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre completo *
                </label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <input
                    type="text"
                    required
                    value={formulario.nombre}
                    onChange={(e) => setFormulario({ ...formulario, nombre: e.target.value })}
                    className="input-field pl-10"
                    placeholder="Dr. Juan Pérez"
                  />
                </div>
              </div>

              {/* Especialidad */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Especialidad *
                </label>
                <div className="relative">
                  <Stethoscope className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <input
                    type="text"
                    required
                    value={formulario.especialidad}
                    onChange={(e) => setFormulario({ ...formulario, especialidad: e.target.value })}
                    className="input-field pl-10"
                    placeholder="Urología, Medicina General, etc."
                  />
                </div>
              </div>

              {/* Teléfono */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Teléfono
                </label>
                <div className="relative">
                  <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <input
                    type="tel"
                    value={formulario.telefono}
                    onChange={(e) => setFormulario({ ...formulario, telefono: e.target.value })}
                    className="input-field pl-10"
                    placeholder="300 123 4567"
                  />
                </div>
              </div>

              {/* Email */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <input
                    type="email"
                    value={formulario.email}
                    onChange={(e) => setFormulario({ ...formulario, email: e.target.value })}
                    className="input-field pl-10"
                    placeholder="doctor@ejemplo.com"
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={cerrarFormulario}
                className="btn-secondary"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn-primary"
              >
                {modoEdicion ? 'Actualizar' : 'Crear'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Lista de doctores */}
      {doctoresFiltrados.length === 0 ? (
        <div className="card text-center py-12">
          <Stethoscope className="h-16 w-16 mx-auto text-gray-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No hay doctores</h3>
          <p className="text-gray-600">
            {doctores.length === 0
              ? 'No hay doctores registrados. ¡Crea el primero!'
              : 'No se encontraron doctores con la búsqueda actual.'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {doctoresFiltrados.map((doctor) => (
            <div key={doctor.id} className="card hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <div className="flex items-center space-x-2 mb-2">
                    <h3 className="font-semibold text-gray-900">{doctor.nombre}</h3>
                    {doctor.estado === 'ACTIVO' ? (
                      <CheckCircle className="h-4 w-4 text-green-500" title="Activo" />
                    ) : (
                      <XCircle className="h-4 w-4 text-red-500" title="Inactivo" />
                    )}
                  </div>
                  <p className="text-sm text-gray-600 mb-2">{doctor.especialidad}</p>

                  <div className="space-y-1 text-sm text-gray-500">
                    {doctor.telefono && (
                      <div className="flex items-center space-x-2">
                        <Phone className="h-3 w-3" />
                        <span>{doctor.telefono}</span>
                      </div>
                    )}
                    {doctor.email && (
                      <div className="flex items-center space-x-2">
                        <Mail className="h-3 w-3" />
                        <span className="truncate">{doctor.email}</span>
                      </div>
                    )}
                  </div>

                  <p className="text-xs text-gray-400 mt-2">
                    Creado: {format(new Date(doctor.creadoEn), "dd/MM/yyyy", { locale: es })}
                  </p>
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-3 border-t">
                <button
                  onClick={() => handleEditar(doctor)}
                  className="text-blue-600 hover:text-blue-800 text-sm flex items-center space-x-1"
                >
                  <Pencil className="h-3 w-3" />
                  <span>Editar</span>
                </button>
                <button
                  onClick={() => handleEliminar(doctor)}
                  className="text-red-600 hover:text-red-800 text-sm flex items-center space-x-1"
                >
                  <Trash2 className="h-3 w-3" />
                  <span>Eliminar</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
