import { useState, useEffect } from 'react'
import axios from 'axios'
import { MessageCircle, CheckCircle, XCircle, Clock } from 'lucide-react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function WhatsappStatus() {
  const [estado, setEstado] = useState('checking')

  useEffect(() => {
    checkStatus()
  }, [])

  const checkStatus = async () => {
    try {
      await axios.get(`${API_URL}/api/citas/health`)
      setEstado('connected')
    } catch (error) {
      setEstado('error')
    }
  }

  return (
    <div className="card">
      <h3 className="text-lg font-bold mb-4 flex items-center">
        <MessageCircle className="h-5 w-5 mr-2 text-green-600" />
        Estado WhatsApp
      </h3>

      <div className="space-y-4">
        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
          <span className="text-sm text-gray-700">API Backend</span>
          <div className="flex items-center">
            {estado === 'checking' && (
              <Clock className="h-5 w-5 text-yellow-600 animate-spin" />
            )}
            {estado === 'connected' && (
              <CheckCircle className="h-5 w-5 text-green-600" />
            )}
            {estado === 'error' && (
              <XCircle className="h-5 w-5 text-red-600" />
            )}
          </div>
        </div>

        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
          <span className="text-sm text-gray-700">Recordatorios</span>
          <CheckCircle className="h-5 w-5 text-green-600" />
        </div>

        <div className="border-t pt-4">
          <h4 className="text-sm font-medium text-gray-900 mb-2">
            Funcionalidades Activas
          </h4>
          <ul className="space-y-2 text-sm text-gray-600">
            <li className="flex items-start">
              <CheckCircle className="h-4 w-4 text-green-600 mr-2 mt-0.5 flex-shrink-0" />
              <span>Confirmación inmediata por WhatsApp</span>
            </li>
            <li className="flex items-start">
              <CheckCircle className="h-4 w-4 text-green-600 mr-2 mt-0.5 flex-shrink-0" />
              <span>Recordatorio 24h antes</span>
            </li>
            <li className="flex items-start">
              <CheckCircle className="h-4 w-4 text-green-600 mr-2 mt-0.5 flex-shrink-0" />
              <span>Recordatorio 1h antes</span>
            </li>
            <li className="flex items-start">
              <CheckCircle className="h-4 w-4 text-green-600 mr-2 mt-0.5 flex-shrink-0" />
              <span>Webhook para recibir mensajes</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  )
}
