import { LucideIcon } from 'lucide-react'

const colorClasses = {
  blue: {
    bg: 'bg-blue-50',
    iconBg: 'bg-blue-100',
    iconText: 'text-blue-600',
    text: 'text-blue-600',
  },
  green: {
    bg: 'bg-green-50',
    iconBg: 'bg-green-100',
    iconText: 'text-green-600',
    text: 'text-green-600',
  },
  yellow: {
    bg: 'bg-yellow-50',
    iconBg: 'bg-yellow-100',
    iconText: 'text-yellow-600',
    text: 'text-yellow-600',
  },
  red: {
    bg: 'bg-red-50',
    iconBg: 'bg-red-100',
    iconText: 'text-red-600',
    text: 'text-red-600',
  },
}

export default function EstadisticasCard({ title, value, icon, color = 'blue', percentage, trend }) {
  const colors = colorClasses[color] || colorClasses.blue

  return (
    <div className={`${colors.bg} rounded-lg p-6`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className={`text-3xl font-bold ${colors.text} mt-2`}>{value}</p>
          {percentage !== undefined && (
            <p className="text-sm text-gray-500 mt-1">{percentage}% del total</p>
          )}
          {trend && (
            <p className="text-xs text-gray-500 mt-1">{trend}</p>
          )}
        </div>
        <div className={`${colors.iconBg} p-3 rounded-full`}>
          <div className={colors.iconText}>
            {icon}
          </div>
        </div>
      </div>
    </div>
  )
}
