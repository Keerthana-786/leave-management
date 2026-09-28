import api from './client'

export interface User {
  id: number
  name: string
  email: string
  role: 'EMPLOYEE' | 'MANAGER' | 'HR'
  teamId?: number
  teamName?: string
  managerId?: number
  managerName?: string
  joinDate?: string
  active: boolean
}

export interface LoginResponse {
  token: string
  tokenType: string
  user: User
}

export interface LeaveBalance {
  id: number
  leaveTypeId: number
  leaveTypeCode: string
  leaveTypeName: string
  year: number
  entitled: number
  used: number
  pending: number
  remaining: number
  available: number
  proRataExplanation?: string
}

export interface LeaveRequest {
  id: number
  employeeId: number
  employeeName: string
  employeeEmail: string
  leaveTypeId: number
  leaveTypeCode: string
  leaveTypeName: string
  fromDate: string
  toDate: string
  days: number
  reason: string
  status: LeaveStatus
  flagged: boolean
  flagReason?: string
  createdAt: string
  lastActionAt: string
  conflictSeverity?: 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH'
  employeeAvailableBalance?: number
  overlappingTeammates?: string[]
  dueAt?: string
}

export type LeaveStatus =
  | 'PENDING_MANAGER'
  | 'PENDING_HR'
  | 'ESCALATED'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED'

export interface ApprovalStep {
  id: number
  requestId: number
  stage: 'MANAGER' | 'HR'
  assigneeId?: number
  assigneeName?: string
  delegatedFromId?: number
  delegatedFromName?: string
  dueAt?: string
  decision: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ESCALATED'
  decidedAt?: string
  comment?: string
}

export interface AuditEvent {
  id: number
  requestId: number
  actorId?: number
  actorName: string
  action: string
  fromStatus?: LeaveStatus
  toStatus: LeaveStatus
  comment?: string
  at: string
}

export interface LeaveTimeline {
  leave: LeaveRequest
  steps: ApprovalStep[]
  auditEvents: AuditEvent[]
}

export interface LeavePreviewResponse {
  workingDays: number
  currentBalance: number
  balanceAfter: number
  conflictFlagged: boolean
  flagReason?: string
  holidayDates: string[]
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface Notification {
  id: number
  message: string
  read: boolean
  createdAt: string
  relatedRequestId?: number
}

export interface Delegation {
  id: number
  delegatorId: number
  delegatorName: string
  delegateId: number
  delegateName: string
  fromDate: string
  toDate: string
  active: boolean
}

export interface AnalyticsSummary {
  totalRequests: number
  approvedRequests: number
  pendingManager: number
  pendingHr: number
  escalated: number
  rejected: number
  cancelled: number
  averageDaysPerRequest: number
  requestsByType: Record<string, number>
}

// Auth
export const login = (email: string, password: string) =>
  api.post<LoginResponse>('/auth/login', { email, password })

export const getMe = () => api.get<User>('/auth/me')

export const getDemoUsers = () => api.get<User[]>('/demo/users')

// Balances
export const getMyBalances = () => api.get<LeaveBalance[]>('/balances/me')

// Leave Requests
export const previewLeave = (leaveTypeId: number, fromDate: string, toDate: string) =>
  api.post<LeavePreviewResponse>('/leaves/preview', { leaveTypeId, fromDate, toDate })

export const applyLeave = (leaveTypeId: number, fromDate: string, toDate: string, reason: string) =>
  api.post<LeaveRequest>('/leaves', { leaveTypeId, fromDate, toDate, reason })

export const getMyLeaves = () => api.get<LeaveRequest[]>('/leaves/mine')

export const getLeaveById = (id: number) => api.get<LeaveRequest>(`/leaves/${id}`)

export const getLeaveTimeline = (id: number) => api.get<LeaveTimeline>(`/leaves/${id}/timeline`)

export const cancelLeave = (id: number) => api.post<LeaveRequest>(`/leaves/${id}/cancel`)

export const approveLeave = (id: number, comment?: string) =>
  api.post<LeaveRequest>(`/leaves/${id}/approve`, { comment })

export const rejectLeave = (id: number, comment: string) =>
  api.post<LeaveRequest>(`/leaves/${id}/reject`, { comment })

// Manager / HR queues
export const getManagerRequests = (params?: Record<string, string | number>) =>
  api.get<PageResponse<LeaveRequest>>('/manager/requests', { params })

export const getHrRequests = (params?: Record<string, string | number>) =>
  api.get<PageResponse<LeaveRequest>>('/hr/requests', { params })

// Notifications
export const getNotifications = () => api.get<Notification[]>('/notifications')

export const markNotificationRead = (id: number) => api.post(`/notifications/${id}/read`)

// Delegations
export const getDelegations = () => api.get<Delegation[]>('/delegations')

export const createDelegation = (delegateId: number, fromDate: string, toDate: string) =>
  api.post<Delegation>('/delegations', { delegateId, fromDate, toDate })

export const revokeDelegation = (id: number) => api.delete(`/delegations/${id}`)

// Demo controls
export const simulateTimeout = (id: number) =>
  api.post<{ success: boolean; message: string }>(`/demo/simulate-timeout/${id}`)

export const resetSeed = () =>
  api.post<{ success: boolean; message: string }>('/demo/reset-seed')

// Analytics
export const getAnalyticsSummary = () => api.get<AnalyticsSummary>('/analytics/summary')

// Holidays
export const getHolidays = () => api.get('/holidays')

// Calendar
export const getTeamCalendar = (month?: string) =>
  api.get('/calendar/team', { params: month ? { month } : undefined })
