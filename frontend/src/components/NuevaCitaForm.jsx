import { useState } from 'react'
import axios from 'axios'
import { Calendar, Clock, User, Phone, Mail, Stethoscope } from 'lucide-react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function NuevaCitaForm() {
  const [formData, setFormData] = useState({
    nombrePaciente: '',
    telefono: '',
    email: '',
    fechaHora: '',
    doctor: '',
  })
  const [enviando, setEnviando] = useState(false)
  const [mensaje, setMensaje] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setEnviando(true)
    setMensaje(null)

    try {
      const response = await axios.post(`${API_URL}/api/citas`, {
        ...formData,
        fechaHora: new Date(formData.fechaHora).toISOString(),
      })

      setMensaje({
        tipo: 'success',
        texto: '¡Cita agendada exitosamente! Se ha enviado una confirmación por WhatsApp.',
      })

      setFormData({
        nombrePaciente: '',
        telefono: '',
        email: '',
        fechaHora: '',
        doctor: '',
      })
    } catch (error) {
      setMensaje({
        tipo: 'error',
        texto: error.response?.data?.message || 'Error al agendar la cita',
      })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="card">
      <h2 className="text-xl font-bold mb-6 flex items-center">
        <Calendar className="h-6 w-6 mr-2 text-primary-600" />
        Agendar Nueva Cita
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

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Nombre del Paciente *
          </label>
          <div className="relative">
            <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              required
              value={formData.nombrePaciente}
              onChange={(e) => setFormData({ ...formData, nombrePaciente: e.target.value })}
              className="input-field pl-10"
              placeholder="Juan Pérez"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Teléfono (WhatsApp) *
          </label>
          <div className="relative">
            <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="tel"
              required
              value={formData.telefono}
              onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
              className="input-field pl-10"
              placeholder="+521234567890"
            />
          </div>
          <p className="text-xs text-gray-500 mt-1">Formato: +código + número</p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Email (opcional)
          </label>
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              className="input-field pl-10"
              placeholder="juan@ejemplo.com"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Fecha y Hora *
          </label>
          <div className="relative">
            <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="datetime-local"
              required
              value={formData.fechaHora}
              onChange={(e) => setFormData({ ...formData, fechaHora: e.target.value })}
              className="input-field pl-10"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Doctor *
          </label>
          <div className="relative">
            <Stethoscope className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              required
              value={formData.doctor}
              onChange={(e) => setFormData({ ...formData, doctor: e.target.value })}
              className="input-field pl-10"
              placeholder="Dr. Pérez"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={enviando}
          className="w-full btn-primary disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {enviando ? 'Agendando...' : 'Agendar Cita'}
        </button>
      </form>
    </div>
  )
}
