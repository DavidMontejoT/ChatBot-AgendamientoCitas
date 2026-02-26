import { useState, useEffect } from 'react'
import axios from 'axios'
import { format, startOfWeek, endOfWeek, startOfDay, endOfDay, subDays } from 'date-fns'
import { es } from 'date-fns/locale'
import { Calendar, Users, Clock, CheckCircle, XCircle, TrendingUp } from 'lucide-react'
import EstadisticasCard from './EstadisticasCard'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function Dashboard() {
  const [citas, setCitas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [periodo, setPeriodo] = useState('hoy')

  useEffect(() => {
    fetchTodasLasCitas()
  }, [])

  const fetchTodasLasCitas = async () => {
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

  const filtrarCitasPorPeriodo = (citas, periodo) => {
    const ahora = new Date()
    let inicio, fin

    switch (periodo) {
      case 'hoy':
        inicio = startOfDay(ahora)
        fin = endOfDay(ahora)
        break
      case 'semana':
        inicio = startOfWeek(ahora, { locale: es })
        fin = endOfWeek(ahora, { locale: es })
        break
      case 'mes':
        inicio = new Date(ahora.getFullYear(), ahora.getMonth(), 1)
        fin = new Date(ahora.getFullYear(), ahora.getMonth() + 1, 0, 23, 59, 59)
        break
      default:
        return citas
    }

    return citas.filter(cita => {
      const fechaCita = new Date(cita.fechaHora)
      return fechaCita >= inicio && fechaCita <= fin
    })
  }

  const calcularEstadisticas = (citasFiltradas) => {
    const total = citasFiltradas.length
    const programadas = citasFiltradas.filter(c => c.estado === 'PROGRAMADA').length
    const confirmadas = citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length
    const canceladas = citasFiltradas.filter(c => c.estado === 'CANCELADA').length
    const completadas = citasFiltradas.filter(c => c.estado === 'COMPLETADA').length

    return { total, programadas, confirmadas, canceladas, completadas }
  }

  const citasFiltradas = filtrarCitasPorPeriodo(citas, periodo)
  const stats = calcularEstadisticas(citasFiltradas)
  const citasTodas = filtrarCitasPorPeriodo(citas, 'todos')

  const tasaCancelacion = stats.total > 0 ? ((stats.canceladas / stats.total) * 100).toFixed(1) : 0
  const tasaCompletacion = stats.total > 0 ? ((stats.completadas / stats.total) * 100).toFixed(1) : 0

  if (cargando) {
    return (
      <div className="card text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        <p className="mt-4 text-gray-600">Cargando estadísticas...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Encabezado con selector de período */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Dashboard de Citas</h2>
          <p className="text-gray-600 mt-1">Estadísticas y métricas en tiempo real</p>
        </div>
        <div className="flex space-x-2">
          {['hoy', 'semana', 'mes'].map((p) => (
            <button
              key={p}
              onClick={() => setPeriodo(p)}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                periodo === p
                  ? 'bg-primary-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              {p.charAt(0).toUpperCase() + p.slice(1)}
            </button>
          ))}
        </div>
      </div>

      {/* Tarjetas de estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <EstadisticasCard
          title="Total Citas"
          value={stats.total}
          icon={<Calendar className="h-6 w-6" />}
          color="blue"
          trend={citasTodas.length > 0 ? `Total: ${citasTodas.length}` : ''}
        />
        <EstadisticasCard
          title="Programadas"
          value={stats.programadas}
          icon={<Clock className="h-6 w-6" />}
          color="yellow"
          percentage={stats.total > 0 ? ((stats.programadas / stats.total) * 100).toFixed(1) : 0}
        />
        <EstadisticasCard
          title="Completadas"
          value={stats.completadas}
          icon={<CheckCircle className="h-6 w-6" />}
          color="green"
          percentage={tasaCompletacion}
        />
        <EstadisticasCard
          title="Canceladas"
          value={stats.canceladas}
          icon={<XCircle className="h-6 w-6" />}
          color="red"
          percentage={tasaCancelacion}
        />
      </div>

      {/* Gráficas adicionales */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Distribución por estado */}
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Distribución por Estado</h3>
          <div className="space-y-3">
            {stats.total > 0 ? (
              <>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Programadas</span>
                  <div className="flex items-center space-x-2">
                    <div className="w-32 bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-yellow-500 h-2 rounded-full"
                        style={{ width: `${(stats.programadas / stats.total) * 100}%` }}
                      ></div>
                    </div>
                    <span className="text-sm font-medium">{stats.programadas}</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Confirmadas</span>
                  <div className="flex items-center space-x-2">
                    <div className="w-32 bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-500 h-2 rounded-full"
                        style={{ width: `${(stats.confirmadas / stats.total) * 100}%` }}
                      ></div>
                    </div>
                    <span className="text-sm font-medium">{stats.confirmadas}</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Completadas</span>
                  <div className="flex items-center space-x-2">
                    <div className="w-32 bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-600 h-2 rounded-full"
                        style={{ width: `${(stats.completadas / stats.total) * 100}%` }}
                      ></div>
                    </div>
                    <span className="text-sm font-medium">{stats.completadas}</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Canceladas</span>
                  <div className="flex items-center space-x-2">
                    <div className="w-32 bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-red-500 h-2 rounded-full"
                        style={{ width: `${(stats.canceladas / stats.total) * 100}%` }}
                      ></div>
                    </div>
                    <span className="text-sm font-medium">{stats.canceladas}</span>
                  </div>
                </div>
              </>
            ) : (
              <p className="text-sm text-gray-500 text-center py-4">No hay datos para este período</p>
            )}
          </div>
        </div>

        {/* Métricas clave */}
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Métricas Clave</h3>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
              <div className="flex items-center space-x-3">
                <Users className="h-5 w-5 text-blue-600" />
                <span className="text-sm font-medium text-gray-700">Pacientes Únicos</span>
              </div>
              <span className="text-lg font-bold text-blue-600">
                {new Set(citasFiltradas.map(c => c.telefono)).size}
              </span>
            </div>
            <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
              <div className="flex items-center space-x-3">
                <TrendingUp className="h-5 w-5 text-green-600" />
                <span className="text-sm font-medium text-gray-700">Tasa Completación</span>
              </div>
              <span className="text-lg font-bold text-green-600">{tasaCompletacion}%</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-red-50 rounded-lg">
              <div className="flex items-center space-x-3">
                <XCircle className="h-5 w-5 text-red-600" />
                <span className="text-sm font-medium text-gray-700">Tasa Cancelación</span>
              </div>
              <span className="text-lg font-bold text-red-600">{tasaCancelacion}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
