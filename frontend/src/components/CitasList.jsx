import { useState, useEffect } from 'react'
import axios from 'axios'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { Calendar, Clock, User, Stethoscope, X } from 'lucide-react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function CitasList({ telefono }) {
  const [citas, setCitas] = useState([])
  const [cargando, setCargando] = useState(false)
  const [mensaje, setMensaje] = useState(null)

  useEffect(() => {
    if (telefono) {
      fetchCitas()
    }
  }, [telefono])

  const fetchCitas = async () => {
    setCargando(true)
    try {
      const response = await axios.get(`${API_URL}/api/citas/paciente/${telefono}`)
      setCitas(response.data)
    } catch (error) {
      setMensaje({
        tipo: 'error',
        texto: 'Error al obtener las citas',
      })
    } finally {
      setCargando(false)
    }
  }

  const cancelarCita = async (id) => {
    if (!confirm('¿Estás seguro de cancelar esta cita?')) return

    try {
      await axios.put(`${API_URL}/api/citas/${id}/cancelar`)
      setMensaje({
        tipo: 'success',
        texto: 'Cita cancelada correctamente',
      })
      fetchCitas()
    } catch (error) {
      setMensaje({
        tipo: 'error',
        texto: 'Error al cancelar la cita',
      })
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

  if (!telefono) {
    return (
      <div className="card text-center py-12">
        <Calendar className="h-16 w-16 mx-auto text-gray-400 mb-4" />
        <h3 className="text-lg font-medium text-gray-900 mb-2">
          Buscar Citas
        </h3>
        <p className="text-gray-600">
          Ingresa un número de teléfono para ver las citas del paciente
        </p>
      </div>
    )
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
    <div className="card">
      <h2 className="text-xl font-bold mb-6 flex items-center">
        <Calendar className="h-6 w-6 mr-2 text-primary-600" />
        Citas del Paciente
      </h2>

      {mensaje && (
        <div
          className={`p-4 rounded-lg mb-6 ${
            mensaje.tipo === 'success'
              ? 'bg-green-50 text-green-800 border border-green-200'
              : 'bg-red-50 text-red-800 border border-red-200'
          }`}
        >
          {mensaje.texto}
        </div>
      )}

      {citas.length === 0 ? (
        <div className="text-center py-8">
          <Calendar className="h-12 w-12 mx-auto text-gray-400 mb-3" />
          <p className="text-gray-600">No hay citas para este paciente</p>
        </div>
      ) : (
        <div className="space-y-4">
          {citas.map((cita) => (
            <div
              key={cita.id}
              className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <h3 className="font-semibold text-gray-900 mb-2">
                    {cita.doctor}
                  </h3>
                  <div className="space-y-1">
                    <div className="flex items-center text-sm text-gray-600">
                      <Calendar className="h-4 w-4 mr-2" />
                      {format(new Date(cita.fechaHora), "dd 'de' MMMM 'de' yyyy", { locale: es })}
                    </div>
                    <div className="flex items-center text-sm text-gray-600">
                      <Clock className="h-4 w-4 mr-2" />
                      {format(new Date(cita.fechaHora), 'HH:mm')}
                    </div>
                  </div>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-xs font-medium ${getEstadoColor(
                    cita.estado
                  )}`}
                >
                  {cita.estado}
                </span>
              </div>

              <div className="flex items-center justify-between pt-3 border-t">
                <div className="flex items-center text-sm text-gray-600">
                  <User className="h-4 w-4 mr-2" />
                  {cita.nombrePaciente}
                </div>
                {cita.estado === 'PROGRAMADA' && (
                  <button
                    onClick={() => cancelarCita(cita.id)}
                    className="text-red-600 hover:text-red-800 text-sm font-medium flex items-center"
                  >
                    <X className="h-4 w-4 mr-1" />
                    Cancelar
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
