import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getMyBalances, getMyLeaves, previewLeave, applyLeave, LeaveBalance, LeaveRequest } from '../api'
import { useAuth } from '../context/AuthContext'
import { StatusBadge } from '../components/StatusBadge'
import { formatDate, formatDateTime } from '../utils/helpers'
import {
  CalendarDays, TrendingUp, Clock, CheckCircle,
  PlusCircle, Loader2, AlertTriangle, X, Info
} from 'lucide-react'

function ApplyLeaveModal({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient()
  const { data: balances = [] } = useQuery({
    queryKey: ['my-balances'],
    queryFn: () => getMyBalances().then((r) => r.data),
  })

  const [leaveTypeId, setLeaveTypeId] = useState<number | ''>('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [reason, setReason] = useState('')
  const [preview, setPreview] = useState<any>(null)
  const [previewError, setPreviewError] = useState('')

  const previewMut = useMutation({
    mutationFn: () => previewLeave(Number(leaveTypeId), fromDate, toDate).then((r) => r.data),
    onSuccess: (data) => { setPreview(data); setPreviewError('') },
    onError: (e: any) => { setPreviewError(e.response?.data?.message || 'Preview failed'); setPreview(null) },
  })

  const applyMut = useMutation({
    mutationFn: () => applyLeave(Number(leaveTypeId), fromDate, toDate, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['my-leaves'] })
      qc.invalidateQueries({ queryKey: ['my-balances'] })
      onClose()
    },
  })

  const canPreview = leaveTypeId && fromDate && toDate

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl w-full max-w-lg">
        <div className="flex items-center justify-between p-5 border-b border-slate-800">
          <h2 className="text-lg font-semibold text-white">Apply for Leave</h2>
          <button onClick={onClose} className="text-slate-500 hover:text-white"><X className="w-5 h-5" /></button>
        </div>

        <div className="p-5 space-y-4">
          <div>
            <label className="label">Leave Type</label>
            <select
              id="leave-type-select"
              className="input"
              value={leaveTypeId}
              onChange={(e) => { setLeaveTypeId(Number(e.target.value)); setPreview(null) }}
            >
              <option value="">Select type...</option>
              {balances.map((b: LeaveBalance) => (
                <option key={b.leaveTypeId} value={b.leaveTypeId}>
                  {b.leaveTypeName} ({b.available} days available)
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">From Date</label>
              <input id="from-date" type="date" className="input" value={fromDate}
                onChange={(e) => { setFromDate(e.target.value); setPreview(null) }} />
            </div>
            <div>
              <label className="label">To Date</label>
              <input id="to-date" type="date" className="input" value={toDate}
                onChange={(e) => { setToDate(e.target.value); setPreview(null) }} />
            </div>
          </div>

          <div>
            <label className="label">Reason</label>
            <textarea
              id="leave-reason"
              className="input min-h-[80px] resize-none"
              placeholder="Briefly explain why you need this leave..."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
          </div>

          {/* Preview button */}
          <button
            id="preview-btn"
            type="button"
            disabled={!canPreview || previewMut.isPending}
            onClick={() => previewMut.mutate()}
            className="btn-secondary w-full flex items-center justify-center gap-2"
          >
            {previewMut.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <Info className="w-4 h-4" />}
            Preview Leave
          </button>

          {/* Preview result */}
          {previewError && (
            <div className="bg-rose-900/30 border border-rose-500/30 rounded-lg p-3 text-sm text-rose-400">
              {previewError}
            </div>
          )}

          {preview && (
            <div className="bg-slate-800/60 border border-slate-700 rounded-xl p-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">Working days</span>
                <span className="text-white font-semibold">{preview.workingDays} days</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">Current balance</span>
                <span className="text-white">{preview.currentBalance} days</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">Balance after</span>
                <span className={`font-semibold ${preview.balanceAfter < 0 ? 'text-rose-400' : 'text-emerald-400'}`}>
                  {preview.balanceAfter} days
                </span>
              </div>
              {preview.holidayDates?.length > 0 && (
                <div className="text-xs text-amber-400 mt-1">
                  ⚠️ Holidays excluded: {preview.holidayDates.join(', ')}
                </div>
              )}
              {preview.conflictFlagged && (
                <div className="mt-2 bg-orange-900/30 border border-orange-500/30 rounded-lg p-2 text-xs text-orange-400 flex items-start gap-2">
                  <AlertTriangle className="w-3.5 h-3.5 flex-shrink-0 mt-0.5" />
                  <span>{preview.flagReason}</span>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="flex gap-3 p-5 border-t border-slate-800">
          <button onClick={onClose} className="btn-secondary flex-1">Cancel</button>
          <button
            id="submit-leave-btn"
            disabled={!preview || !reason.trim() || applyMut.isPending}
            onClick={() => applyMut.mutate()}
            className="btn-primary flex-1 flex items-center justify-center gap-2"
          >
            {applyMut.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : <PlusCircle className="w-4 h-4" />}
            Submit Leave
          </button>
        </div>
      </div>
    </div>
  )
}

export default function DashboardPage() {
  const { user } = useAuth()
  const [showApply, setShowApply] = useState(false)

  const { data: balances = [], isLoading: balLoading } = useQuery({
    queryKey: ['my-balances'],
    queryFn: () => getMyBalances().then((r) => r.data),
  })

  const { data: leaves = [] } = useQuery({
    queryKey: ['my-leaves'],
    queryFn: () => getMyLeaves().then((r) => r.data),
  })

  const recent = [...leaves].slice(0, 5)
  const pending = leaves.filter((l: LeaveRequest) => ['PENDING_MANAGER', 'PENDING_HR', 'ESCALATED'].includes(l.status)).length
  const approved = leaves.filter((l: LeaveRequest) => l.status === 'APPROVED').length

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Welcome back, {user?.name?.split(' ')[0]} 👋</h1>
          <p className="text-slate-400 mt-1">Here's your leave overview for 2026</p>
        </div>
        <button id="apply-leave-btn" onClick={() => setShowApply(true)} className="btn-primary flex items-center gap-2">
          <PlusCircle className="w-4 h-4" />
          Apply Leave
        </button>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/30 flex items-center justify-center">
            <Clock className="w-5 h-5 text-amber-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-white">{pending}</p>
            <p className="text-xs text-slate-400">Pending</p>
          </div>
        </div>
        <div className="card flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center">
            <CheckCircle className="w-5 h-5 text-emerald-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-white">{approved}</p>
            <p className="text-xs text-slate-400">Approved</p>
          </div>
        </div>
        <div className="card flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-indigo-500/20 border border-indigo-500/30 flex items-center justify-center">
            <CalendarDays className="w-5 h-5 text-indigo-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-white">{leaves.length}</p>
            <p className="text-xs text-slate-400">Total Requests</p>
          </div>
        </div>
        <div className="card flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-teal-500/20 border border-teal-500/30 flex items-center justify-center">
            <TrendingUp className="w-5 h-5 text-teal-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-white">
              {balances.reduce((s: number, b: LeaveBalance) => s + b.available, 0)}
            </p>
            <p className="text-xs text-slate-400">Days Available</p>
          </div>
        </div>
      </div>

      {/* Balances */}
      <div>
        <h2 className="text-lg font-semibold text-white mb-3">Leave Balances</h2>
        {balLoading ? (
          <div className="flex items-center gap-2 text-slate-500"><Loader2 className="w-4 h-4 animate-spin" /> Loading...</div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {balances.map((b: LeaveBalance) => {
              const usedPct = b.entitled > 0 ? ((b.used + b.pending) / b.entitled) * 100 : 0
              return (
                <div key={b.id} className="card">
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <p className="font-semibold text-white">{b.leaveTypeName}</p>
                      <p className="text-xs text-slate-500 mt-0.5">{b.leaveTypeCode}</p>
                    </div>
                    <span className="text-2xl font-bold text-white">{b.available}</span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-1.5 mb-3">
                    <div
                      className="bg-indigo-500 h-1.5 rounded-full transition-all"
                      style={{ width: `${Math.min(usedPct, 100)}%` }}
                    />
                  </div>
                  <div className="flex justify-between text-xs text-slate-500">
                    <span>Used: {b.used}</span>
                    {b.pending > 0 && <span className="text-amber-500">Pending: {b.pending}</span>}
                    <span>Total: {b.entitled}</span>
                  </div>
                  {b.proRataExplanation && (
                    <p className="text-xs text-slate-600 mt-2">Pro-rata: {b.proRataExplanation}</p>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* Recent leaves */}
      <div>
        <h2 className="text-lg font-semibold text-white mb-3">Recent Requests</h2>
        {recent.length === 0 ? (
          <div className="card text-center text-slate-500 py-8">No leave requests yet. Apply for your first leave!</div>
        ) : (
          <div className="space-y-2">
            {recent.map((l: LeaveRequest) => (
              <div key={l.id} className="card flex items-center justify-between gap-4 hover:border-slate-700 transition-colors">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-white">{l.leaveTypeName}</span>
                    {l.flagged && <AlertTriangle className="w-3.5 h-3.5 text-orange-400 flex-shrink-0" title={l.flagReason || ''} />}
                  </div>
                  <p className="text-sm text-slate-400 mt-0.5">
                    {formatDate(l.fromDate)} → {formatDate(l.toDate)} · {l.days} days
                  </p>
                  <p className="text-xs text-slate-600 mt-0.5 truncate">{l.reason}</p>
                </div>
                <div className="flex flex-col items-end gap-1.5">
                  <StatusBadge status={l.status} />
                  <span className="text-xs text-slate-600">{formatDateTime(l.createdAt)}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showApply && <ApplyLeaveModal onClose={() => setShowApply(false)} />}
    </div>
  )
}
