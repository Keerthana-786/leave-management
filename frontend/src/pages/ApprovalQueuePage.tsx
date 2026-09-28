import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getManagerRequests, getHrRequests, approveLeave, rejectLeave,
  LeaveRequest, PageResponse
} from '../api'
import { useAuth } from '../context/AuthContext'
import { StatusBadge } from '../components/StatusBadge'
import { formatDate, formatDateTime, getSeverityColor } from '../utils/helpers'
import {
  CheckCircle, XCircle, AlertTriangle, ChevronLeft,
  ChevronRight, Loader2, Search, Filter, Clock
} from 'lucide-react'

function ApprovalModal({
  leave,
  onClose,
}: {
  leave: LeaveRequest
  onClose: () => void
}) {
  const qc = useQueryClient()
  const [comment, setComment] = useState('')
  const [mode, setMode] = useState<'APPROVE' | 'REJECT' | null>(null)

  const approveMut = useMutation({
    mutationFn: () => approveLeave(leave.id, comment || undefined),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['manager-requests'] })
      qc.invalidateQueries({ queryKey: ['hr-requests'] })
      onClose()
    },
  })

  const rejectMut = useMutation({
    mutationFn: () => rejectLeave(leave.id, comment),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['manager-requests'] })
      qc.invalidateQueries({ queryKey: ['hr-requests'] })
      onClose()
    },
  })

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl w-full max-w-lg">
        <div className="p-5 border-b border-slate-800">
          <div className="flex items-center justify-between mb-1">
            <h2 className="text-lg font-semibold text-white">Review Leave Request</h2>
            <StatusBadge status={leave.status} />
          </div>
          <p className="text-slate-400 text-sm">{leave.employeeName} · {leave.leaveTypeName}</p>
        </div>

        <div className="p-5 space-y-4">
          {/* Summary */}
          <div className="bg-slate-800/60 rounded-xl p-4 space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-slate-400">Dates</span>
              <span className="text-white">{formatDate(leave.fromDate)} → {formatDate(leave.toDate)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-400">Duration</span>
              <span className="text-white font-semibold">{leave.days} working days</span>
            </div>
            {leave.employeeAvailableBalance !== undefined && (
              <div className="flex justify-between">
                <span className="text-slate-400">Available balance</span>
                <span className={leave.employeeAvailableBalance >= leave.days ? 'text-emerald-400' : 'text-rose-400'}>
                  {leave.employeeAvailableBalance} days
                </span>
              </div>
            )}
            {leave.conflictSeverity && leave.conflictSeverity !== 'NONE' && (
              <div className="flex items-start gap-2 mt-2 pt-2 border-t border-slate-700">
                <AlertTriangle className={`w-4 h-4 flex-shrink-0 mt-0.5 ${getSeverityColor(leave.conflictSeverity)}`} />
                <span className={getSeverityColor(leave.conflictSeverity)}>
                  {leave.conflictSeverity} conflict: {leave.flagReason}
                  {leave.overlappingTeammates?.length ? ` (${leave.overlappingTeammates.join(', ')} also on leave)` : ''}
                </span>
              </div>
            )}
            <div className="pt-2 border-t border-slate-700">
              <p className="text-slate-400">Reason:</p>
              <p className="text-slate-200 mt-0.5">{leave.reason}</p>
            </div>
          </div>

          {/* Comment */}
          <div>
            <label className="label">Comment {mode === 'REJECT' ? '(required)' : '(optional)'}</label>
            <textarea
              id="approval-comment"
              className="input min-h-[70px] resize-none"
              placeholder="Add a comment for the employee..."
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            />
          </div>

          {/* SLA warning */}
          {leave.dueAt && (
            <div className="flex items-center gap-2 bg-amber-900/30 border border-amber-500/30 rounded-lg px-3 py-2 text-sm text-amber-400">
              <Clock className="w-4 h-4 flex-shrink-0" />
              Due by: {formatDateTime(leave.dueAt)}
            </div>
          )}
        </div>

        <div className="flex gap-3 p-5 border-t border-slate-800">
          <button onClick={onClose} className="btn-secondary">Cancel</button>
          <button
            id={`reject-btn-${leave.id}`}
            onClick={() => { setMode('REJECT'); rejectMut.mutate() }}
            disabled={!comment.trim() || rejectMut.isPending || approveMut.isPending}
            className="btn-danger flex-1 flex items-center justify-center gap-2"
          >
            {rejectMut.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <XCircle className="w-4 h-4" />}
            Reject
          </button>
          <button
            id={`approve-btn-${leave.id}`}
            onClick={() => { setMode('APPROVE'); approveMut.mutate() }}
            disabled={rejectMut.isPending || approveMut.isPending}
            className="btn-success flex-1 flex items-center justify-center gap-2"
          >
            {approveMut.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4" />}
            Approve
          </button>
        </div>
      </div>
    </div>
  )
}

function ApprovalQueue({
  title,
  queryKey,
  fetcher,
}: {
  title: string
  queryKey: string
  fetcher: (params: any) => Promise<any>
}) {
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState<LeaveRequest | null>(null)
  const [search, setSearch] = useState('')

  const { data, isLoading, isFetching } = useQuery({
    queryKey: [queryKey, page],
    queryFn: () => fetcher({ page, size: 10, sort: 'createdAt,desc' }).then((r: any) => r.data),
    refetchInterval: 20000,
  })

  const leaves: LeaveRequest[] = data?.content ?? []
  const totalPages = data?.totalPages ?? 1
  const total = data?.totalElements ?? 0

  const filtered = search
    ? leaves.filter((l) =>
        l.employeeName.toLowerCase().includes(search.toLowerCase()) ||
        l.leaveTypeName.toLowerCase().includes(search.toLowerCase())
      )
    : leaves

  return (
    <div className="space-y-4">
      {/* Title and search */}
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white">{title}</h2>
          <p className="text-slate-400 text-sm">{total} requests pending</p>
        </div>
        <div className="relative">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            className="input pl-9 w-56"
            placeholder="Search..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><Loader2 className="w-6 h-6 animate-spin text-indigo-400" /></div>
      ) : filtered.length === 0 ? (
        <div className="card text-center text-slate-500 py-12">
          {search ? 'No matching requests.' : '🎉 Queue is empty!'}
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map((leave) => (
            <button
              key={leave.id}
              id={`queue-item-${leave.id}`}
              onClick={() => setSelected(leave)}
              className="w-full card hover:border-indigo-500/40 transition-all flex items-center justify-between gap-4 text-left"
            >
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-semibold text-white">{leave.employeeName}</span>
                  <span className="text-slate-400 text-sm">·</span>
                  <span className="text-slate-300 text-sm">{leave.leaveTypeName}</span>
                  {leave.flagged && (
                    <AlertTriangle className={`w-3.5 h-3.5 flex-shrink-0 ${getSeverityColor(leave.conflictSeverity)}`} />
                  )}
                </div>
                <p className="text-sm text-slate-400">
                  {formatDate(leave.fromDate)} → {formatDate(leave.toDate)} · {leave.days} days
                </p>
                <p className="text-xs text-slate-500 mt-0.5 truncate">{leave.reason}</p>
              </div>
              <div className="flex items-center gap-3 flex-shrink-0">
                <div className="text-right">
                  <StatusBadge status={leave.status} />
                  {leave.dueAt && (
                    <p className="text-xs text-amber-500 mt-1">Due {formatDate(leave.dueAt)}</p>
                  )}
                </div>
                <ChevronRight className="w-4 h-4 text-slate-600" />
              </div>
            </button>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-2">
          <button
            onClick={() => setPage((p) => p - 1)}
            disabled={page === 0}
            className="btn-secondary px-3 py-1.5"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <span className="text-sm text-slate-400">Page {page + 1} of {totalPages}</span>
          <button
            onClick={() => setPage((p) => p + 1)}
            disabled={page >= totalPages - 1}
            className="btn-secondary px-3 py-1.5"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      )}

      {selected && (
        <ApprovalModal leave={selected} onClose={() => setSelected(null)} />
      )}
    </div>
  )
}

export function ManagerQueuePage() {
  return (
    <ApprovalQueue
      title="Manager Review Queue"
      queryKey="manager-requests"
      fetcher={getManagerRequests}
    />
  )
}

export function HrQueuePage() {
  return (
    <ApprovalQueue
      title="HR Approval Queue"
      queryKey="hr-requests"
      fetcher={getHrRequests}
    />
  )
}
