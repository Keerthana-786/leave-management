import React from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getNotifications, markNotificationRead, Notification } from '../api'
import { formatDateTime } from '../utils/helpers'
import { Bell, Check, Loader2 } from 'lucide-react'

export default function NotificationsPage() {
  const qc = useQueryClient()
  const { data: notifications = [], isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => getNotifications().then((r) => r.data),
    refetchInterval: 15000,
  })

  const markMut = useMutation({
    mutationFn: (id: number) => markNotificationRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  })

  const unread = notifications.filter((n: Notification) => !n.read)
  const read = notifications.filter((n: Notification) => n.read)

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-white">Notifications</h1>
        <p className="text-slate-400 text-sm mt-1">{unread.length} unread</p>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><Loader2 className="w-6 h-6 animate-spin text-indigo-400" /></div>
      ) : notifications.length === 0 ? (
        <div className="card text-center py-12">
          <Bell className="w-8 h-8 text-slate-600 mx-auto mb-2" />
          <p className="text-slate-500">No notifications yet</p>
        </div>
      ) : (
        <div className="space-y-4">
          {unread.length > 0 && (
            <div>
              <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-2">Unread</h2>
              <div className="space-y-2">
                {unread.map((n: Notification) => (
                  <div
                    key={n.id}
                    className="card border-indigo-500/30 bg-indigo-600/5 flex items-start justify-between gap-4"
                  >
                    <div className="flex-1">
                      <p className="text-slate-200 text-sm">{n.message}</p>
                      <p className="text-xs text-slate-500 mt-1">{formatDateTime(n.createdAt)}</p>
                    </div>
                    <button
                      id={`mark-read-${n.id}`}
                      onClick={() => markMut.mutate(n.id)}
                      disabled={markMut.isPending}
                      className="text-indigo-400 hover:text-indigo-300 transition-colors flex-shrink-0"
                      title="Mark as read"
                    >
                      <Check className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {read.length > 0 && (
            <div>
              <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-2">Earlier</h2>
              <div className="space-y-2">
                {read.map((n: Notification) => (
                  <div key={n.id} className="card opacity-50">
                    <p className="text-slate-300 text-sm">{n.message}</p>
                    <p className="text-xs text-slate-600 mt-1">{formatDateTime(n.createdAt)}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
