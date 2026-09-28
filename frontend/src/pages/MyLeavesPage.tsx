import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getLeaveTimeline, cancelLeave, getMyLeaves, LeaveRequest, LeaveTimeline, ApprovalStep, AuditEvent } from '../api'
import { StatusBadge } from '../components/StatusBadge'
import { formatDate, formatDateTime, getStatusColor } from '../utils/helpers'
import {
  ChevronRight, X, CheckCircle, XCircle, Clock,
  AlertTriangle, Loader2, RefreshCw, User
} from 'lucide-react'

function TimelineModal({ leave, onClose }: { leave: LeaveRequest; onClose: () => void }) {
  const { data, isLoading } = useQuery({
    queryKey: ['timeline', leave.id],
    queryFn: () => getLeaveTimeline(leave.id).then((r) => r.data),
  })

  const qc = useQueryClient()
  const cancelMut = useMutation({
    mutationFn: () => cancelLeave(leave.id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['my-leaves'] })
      onClose()
    },
  })

  const canCancel = ['PENDING_MANAGER', 'PENDING_HR'].includes(leave.status)

  const stepIcon = (decision: string) => {
    switch (decision) {
      case 'APPROVED': return <CheckCircle className="w-4 h-4 text-emerald-400" />
      case 'REJECTED': return <XCircle className="w-4 h-4 text-rose-400" />
      case 'ESCALATED': return <AlertTriangle className="w-4 h-4 text-orange-400" />
      default: return <Clock className="w-4 h-4 text-amber-400" />
    }
  }

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-5 border-b border-slate-800 sticky top-0 bg-slate-900">
          <div>
            <h2 className="text-lg font-semibold text-white">{leave.leaveTypeName}</h2>
            <p className="text-sm text-slate-400">{formatDate(leave.fromDate)} → {formatDate(leave.toDate)} · {leave.days} days</p>
          </div>
          <div className="flex items-center gap-2">
            <StatusBadge status={leave.status} />
            <button onClick={onClose} className="text-slate-500 hover:text-white ml-2"><X className="w-5 h-5" /></button>
          </div>
        </div>

        {isLoading ? (
          <div className="p-8 flex justify-center"><Loader2 className="w-6 h-6 animate-spin text-indigo-400" /></div>
        ) : data ? (
          <div className="p-5 space-y-5">
            {/* Leave details */}
            <div className="bg-slate-800/60 rounded-xl p-4 grid grid-cols-2 gap-3 text-sm">
              <div><span className="text-slate-400">Reason: </span><span className="text-slate-200">{leave.reason}</span></div>
              <div><span className="text-slate-400">Applied: </span><span className="text-slate-200">{formatDateTime(leave.createdAt)}</span></div>
              {leave.flagged && (
                <div className="col-span-2 flex items-start gap-2 text-orange-400">
                  <AlertTriangle className="w-4 h-4 flex-shrink-0 mt-0.5" />
                  <span>{leave.flagReason}</span>
                </div>
              )}
            </div>

            {/* Approval steps */}
            {data.steps?.length > 0 && (
              <div>
                <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-3">Approval Steps</h3>
                <div className="space-y-2">
                  {data.steps.map((step: ApprovalStep) => (
                    <div key={step.id} className="flex items-start gap-3 p-3 bg-slate-800/40 rounded-xl">
                      <div className="mt-0.5">{stepIcon(step.decision)}</div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <span className="font-medium text-white text-sm">{step.stage} Review</span>
                          <span className={`badge text-xs ${
                            step.decision === 'APPROVED' ? 'bg-emerald-500/20 text-emerald-400' :
                            step.decision === 'REJECTED' ? 'bg-rose-500/20 text-rose-400' :
                            step.decision === 'ESCALATED' ? 'bg-orange-500/20 text-orange-400' :
                            'bg-amber-500/20 text-amber-400'
                          }`}>{step.decision}</span>
                        </div>
                        {step.assigneeName && (
                          <p className="text-xs text-slate-400 mt-0.5">
                            Assigned to: {step.assigneeName}
                            {step.delegatedFromName && ` (delegated from ${step.delegatedFromName})`}
                          </p>
                        )}
                        {step.comment && <p className="text-sm text-slate-300 mt-1 italic">"{step.comment}"</p>}
                        {step.dueAt && <p className="text-xs text-slate-600 mt-0.5">Due: {formatDateTime(step.dueAt)}</p>}
                        {step.decidedAt && <p className="text-xs text-slate-600 mt-0.5">Decided: {formatDateTime(step.decidedAt)}</p>}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Audit trail */}
            {data.auditEvents?.length > 0 && (
              <div>
                <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-3">Activity Log</h3>
                <div className="relative">
                  <div className="absolute left-4 top-0 bottom-0 w-px bg-slate-700" />
                  <div className="space-y-3">
                    {data.auditEvents.map((evt: AuditEvent) => (
                      <div key={evt.id} className="flex items-start gap-3 pl-8 relative">
                        <div className="absolute left-2.5 w-3 h-3 rounded-full bg-slate-700 border-2 border-slate-600 mt-1" />
                        <div>
                          <div className="flex items-center gap-2">
                            <span className="text-sm font-medium text-slate-200">{evt.action}</span>
                            {evt.toStatus && <StatusBadge status={evt.toStatus} />}
                          </div>
                          <p className="text-xs text-slate-400 mt-0.5">by {evt.actorName} · {formatDateTime(evt.at)}</p>
                          {evt.comment && <p className="text-sm text-slate-400 italic mt-1">"{evt.comment}"</p>}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        ) : null}

        {canCancel && (
          <div className="p-5 border-t border-slate-800">
            <button
              id={`cancel-leave-${leave.id}`}
              onClick={() => cancelMut.mutate()}
              disabled={cancelMut.isPending}
              className="btn-danger w-full flex items-center justify-center gap-2"
            >
              {cancelMut.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <XCircle className="w-4 h-4" />}
              Cancel Leave Request
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default function MyLeavesPage() {
  const [selected, setSelected] = useState<LeaveRequest | null>(null)
  const [statusFilter, setStatusFilter] = useState<string>('ALL')

  const { data: leaves = [], isLoading } = useQuery({
    queryKey: ['my-leaves'],
    queryFn: () => getMyLeaves().then((r) => r.data),
    refetchInterval: 30000,
  })

  const filtered = statusFilter === 'ALL'
    ? leaves
    : leaves.filter((l: LeaveRequest) => l.status === statusFilter)

  const STATUS_OPTIONS = [
    { value: 'ALL', label: 'All' },
    { value: 'PENDING_MANAGER', label: 'Pending Manager' },
    { value: 'PENDING_HR', label: 'Pending HR' },
    { value: 'ESCALATED', label: 'Escalated' },
    { value: 'APPROVED', label: 'Approved' },
    { value: 'REJECTED', label: 'Rejected' },
    { value: 'CANCELLED', label: 'Cancelled' },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">My Leaves</h1>
          <p className="text-slate-400 text-sm mt-1">{leaves.length} total requests</p>
        </div>
        <div className="flex gap-2 flex-wrap">
          {STATUS_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              id={`filter-${opt.value.toLowerCase()}`}
              onClick={() => setStatusFilter(opt.value)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                statusFilter === opt.value
                  ? 'bg-indigo-600 text-white'
                  : 'bg-slate-800 text-slate-400 hover:text-white'
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="flex items-center gap-2 text-slate-500 py-8 justify-center">
          <Loader2 className="w-5 h-5 animate-spin" /> Loading your leaves...
        </div>
      ) : filtered.length === 0 ? (
        <div className="card text-center text-slate-500 py-12">
          No leave requests found for this filter.
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map((leave: LeaveRequest) => (
            <button
              key={leave.id}
              id={`leave-row-${leave.id}`}
              onClick={() => setSelected(leave)}
              className="w-full card hover:border-indigo-500/40 transition-all flex items-center justify-between gap-4 text-left"
            >
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-semibold text-white">{leave.leaveTypeName}</span>
                  {leave.flagged && (
                    <AlertTriangle className="w-3.5 h-3.5 text-orange-400 flex-shrink-0" title={leave.flagReason || ''} />
                  )}
                </div>
                <p className="text-sm text-slate-400">
                  {formatDate(leave.fromDate)} → {formatDate(leave.toDate)} · {leave.days} working days
                </p>
                <p className="text-xs text-slate-500 mt-0.5 truncate">{leave.reason}</p>
              </div>
              <div className="flex items-center gap-3 flex-shrink-0">
                <div className="text-right">
                  <StatusBadge status={leave.status} />
                  <p className="text-xs text-slate-600 mt-1">{formatDate(leave.createdAt)}</p>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-600" />
              </div>
            </button>
          ))}
        </div>
      )}

      {selected && <TimelineModal leave={selected} onClose={() => setSelected(null)} />}
    </div>
  )
}
