import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getTeamCalendar, getHolidays } from '../api'
import { formatDate } from '../utils/helpers'
import { ChevronLeft, ChevronRight, Loader2, CalendarDays } from 'lucide-react'
import { StatusBadge } from '../components/StatusBadge'

function CalendarGrid({ month, events, holidays }: {
  month: string
  events: any[]
  holidays: any[]
}) {
  const [year, mon] = month.split('-').map(Number)
  const firstDay = new Date(year, mon - 1, 1)
  const lastDay = new Date(year, mon, 0)
  const startDow = firstDay.getDay() // 0=Sun
  const daysInMonth = lastDay.getDate()

  const cells = []
  for (let i = 0; i < startDow; i++) cells.push(null)
  for (let d = 1; d <= daysInMonth; d++) cells.push(d)

  const holidayDates = new Set(
    holidays
      .filter((h: any) => h.date?.startsWith(month))
      .map((h: any) => new Date(h.date).getDate())
  )

  const eventsByDay: Record<number, any[]> = {}
  events.forEach((e: any) => {
    const from = new Date(e.fromDate)
    const to = new Date(e.toDate)
    for (let dt = new Date(from); dt <= to; dt.setDate(dt.getDate() + 1)) {
      if (dt.getFullYear() === year && dt.getMonth() + 1 === mon) {
        const d = dt.getDate()
        if (!eventsByDay[d]) eventsByDay[d] = []
        eventsByDay[d].push(e)
      }
    }
  })

  const DOW = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

  return (
    <div>
      <div className="grid grid-cols-7 mb-2">
        {DOW.map((d) => (
          <div key={d} className="text-center text-xs font-semibold text-slate-500 py-2">{d}</div>
        ))}
      </div>
      <div className="grid grid-cols-7 gap-1">
        {cells.map((day, i) => {
          if (!day) return <div key={`empty-${i}`} />
          const evts = eventsByDay[day] || []
          const isHoliday = holidayDates.has(day)
          const isToday = new Date().getDate() === day &&
            new Date().getMonth() + 1 === mon &&
            new Date().getFullYear() === year

          return (
            <div
              key={day}
              className={`min-h-[70px] rounded-lg p-1.5 border text-sm transition-all ${
                isToday
                  ? 'border-indigo-500 bg-indigo-600/10'
                  : isHoliday
                  ? 'border-rose-500/30 bg-rose-900/10'
                  : 'border-slate-800 bg-slate-900'
              }`}
            >
              <div className={`text-xs font-semibold mb-1 ${isToday ? 'text-indigo-400' : isHoliday ? 'text-rose-400' : 'text-slate-400'}`}>
                {day}
              </div>
              {evts.slice(0, 2).map((e: any, idx: number) => (
                <div
                  key={idx}
                  className="text-xs truncate rounded px-1 py-0.5 mb-0.5 bg-indigo-600/30 text-indigo-300"
                  title={`${e.employeeName}: ${e.leaveTypeName}`}
                >
                  {e.employeeName?.split(' ')[0]}
                </div>
              ))}
              {evts.length > 2 && (
                <div className="text-xs text-slate-500">+{evts.length - 2} more</div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default function CalendarPage() {
  const today = new Date()
  const [month, setMonth] = useState(`${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`)

  const { data: calData, isLoading: calLoading } = useQuery({
    queryKey: ['calendar', month],
    queryFn: () => getTeamCalendar(month).then((r) => r.data),
  })

  const { data: holidays = [] } = useQuery({
    queryKey: ['holidays'],
    queryFn: () => getHolidays().then((r) => r.data),
  })

  const prevMonth = () => {
    const [y, m] = month.split('-').map(Number)
    const d = new Date(y, m - 2, 1)
    setMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }

  const nextMonth = () => {
    const [y, m] = month.split('-').map(Number)
    const d = new Date(y, m, 1)
    setMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }

  const monthLabel = new Date(month + '-01').toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })

  const events: any[] = Array.isArray(calData) ? calData : calData?.leaves ?? []
  const monthHolidays = holidays.filter((h: any) => h.date?.startsWith(month))

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Team Calendar</h1>
          <p className="text-slate-400 text-sm mt-1">Approved and pending team leaves</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={prevMonth} className="btn-secondary px-3 py-2"><ChevronLeft className="w-4 h-4" /></button>
          <span className="text-white font-semibold min-w-[140px] text-center">{monthLabel}</span>
          <button onClick={nextMonth} className="btn-secondary px-3 py-2"><ChevronRight className="w-4 h-4" /></button>
        </div>
      </div>

      {/* Holidays strip */}
      {monthHolidays.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {monthHolidays.map((h: any) => (
            <span key={h.id} className="badge bg-rose-500/20 text-rose-400 border border-rose-500/30">
              🏖️ {h.name} ({formatDate(h.date)})
            </span>
          ))}
        </div>
      )}

      {calLoading ? (
        <div className="flex justify-center py-16"><Loader2 className="w-6 h-6 animate-spin text-indigo-400" /></div>
      ) : (
        <div className="card">
          <CalendarGrid month={month} events={events} holidays={holidays} />
        </div>
      )}

      {/* Legend */}
      <div className="flex flex-wrap gap-4 text-xs text-slate-400">
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-indigo-600/40" /> Team leave</span>
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-indigo-500 border border-indigo-400" /> Today</span>
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-rose-900/40 border border-rose-500/30" /> Holiday</span>
      </div>
    </div>
  )
}
