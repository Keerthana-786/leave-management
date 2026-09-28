import { LeaveStatus } from '../api'

export function getStatusColor(status: LeaveStatus): string {
  switch (status) {
    case 'APPROVED': return 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
    case 'PENDING_MANAGER': return 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
    case 'PENDING_HR': return 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
    case 'ESCALATED': return 'bg-orange-500/20 text-orange-400 border border-orange-500/30'
    case 'REJECTED': return 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
    case 'CANCELLED': return 'bg-slate-500/20 text-slate-400 border border-slate-500/30'
    default: return 'bg-slate-500/20 text-slate-400'
  }
}

export function getStatusLabel(status: LeaveStatus): string {
  switch (status) {
    case 'PENDING_MANAGER': return 'Pending Manager'
    case 'PENDING_HR': return 'Pending HR'
    case 'ESCALATED': return 'Escalated'
    case 'APPROVED': return 'Approved'
    case 'REJECTED': return 'Rejected'
    case 'CANCELLED': return 'Cancelled'
    default: return status
  }
}

export function getRoleColor(role: string): string {
  switch (role) {
    case 'HR': return 'bg-purple-500/20 text-purple-400 border border-purple-500/30'
    case 'MANAGER': return 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30'
    case 'EMPLOYEE': return 'bg-teal-500/20 text-teal-400 border border-teal-500/30'
    default: return 'bg-slate-500/20 text-slate-400'
  }
}

export function formatDate(date: string): string {
  return new Date(date).toLocaleDateString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric'
  })
}

export function formatDateTime(dt: string): string {
  return new Date(dt).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

export function getSeverityColor(severity?: string): string {
  switch (severity) {
    case 'HIGH': return 'text-rose-400'
    case 'MEDIUM': return 'text-orange-400'
    case 'LOW': return 'text-amber-400'
    default: return 'text-slate-500'
  }
}
