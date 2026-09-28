import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { getAnalyticsSummary, AnalyticsSummary } from '../api'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'
import { Loader2 } from 'lucide-react'

const STATUS_COLORS: Record<string, string> = {
  approvedRequests: '#10b981',
  pendingManager: '#f59e0b',
  pendingHr: '#3b82f6',
  escalated: '#f97316',
  rejected: '#f43f5e',
  cancelled: '#64748b',
}

const PIE_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#f43f5e', '#3b82f6', '#8b5cf6', '#ec4899']

function StatCard({ label, value, sub, color }: { label: string; value: number; sub?: string; color: string }) {
  return (
    <div className="card">
      <p className="text-sm text-slate-400">{label}</p>
      <p className={`text-3xl font-bold mt-1 ${color}`}>{value}</p>
      {sub && <p className="text-xs text-slate-500 mt-1">{sub}</p>}
    </div>
  )
}

export default function AnalyticsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['analytics'],
    queryFn: () => getAnalyticsSummary().then((r) => r.data),
    refetchInterval: 60000,
  })

  if (isLoading) {
    return <div className="flex justify-center py-16"><Loader2 className="w-8 h-8 animate-spin text-indigo-400" /></div>
  }

  if (!data) return null

  const statusData = [
    { name: 'Approved', value: data.approvedRequests, fill: '#10b981' },
    { name: 'Pending Manager', value: data.pendingManager, fill: '#f59e0b' },
    { name: 'Pending HR', value: data.pendingHr, fill: '#3b82f6' },
    { name: 'Escalated', value: data.escalated, fill: '#f97316' },
    { name: 'Rejected', value: data.rejected, fill: '#f43f5e' },
    { name: 'Cancelled', value: data.cancelled, fill: '#64748b' },
  ]

  const typeData = Object.entries(data.requestsByType || {}).map(([name, value], i) => ({
    name, value, fill: PIE_COLORS[i % PIE_COLORS.length],
  }))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Analytics</h1>
        <p className="text-slate-400 text-sm mt-1">Leave usage across the organization</p>
      </div>

      {/* Summary stats */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <StatCard label="Total" value={data.totalRequests} color="text-white" />
        <StatCard label="Approved" value={data.approvedRequests} color="text-emerald-400" />
        <StatCard label="Pending Manager" value={data.pendingManager} color="text-amber-400" />
        <StatCard label="Pending HR" value={data.pendingHr} color="text-blue-400" />
        <StatCard label="Escalated" value={data.escalated} color="text-orange-400" />
        <StatCard label="Avg Days" value={Math.round(data.averageDaysPerRequest)} sub="per request" color="text-indigo-400" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Status breakdown bar chart */}
        <div className="card">
          <h2 className="text-lg font-semibold text-white mb-4">Status Breakdown</h2>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={statusData} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
              <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 11 }} />
              <YAxis tick={{ fill: '#64748b', fontSize: 11 }} />
              <Tooltip
                contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: '8px' }}
                labelStyle={{ color: '#f1f5f9' }}
                itemStyle={{ color: '#94a3b8' }}
              />
              <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                {statusData.map((entry, i) => (
                  <Cell key={i} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Leave type pie chart */}
        <div className="card">
          <h2 className="text-lg font-semibold text-white mb-4">Requests by Leave Type</h2>
          {typeData.length === 0 ? (
            <div className="flex items-center justify-center h-[250px] text-slate-500">No data available</div>
          ) : (
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={typeData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  labelLine={{ stroke: '#475569' }}
                >
                  {typeData.map((entry, i) => (
                    <Cell key={i} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: '8px' }}
                  itemStyle={{ color: '#94a3b8' }}
                />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  )
}
