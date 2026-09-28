import React from 'react'
import { getStatusColor, getStatusLabel } from '../utils/helpers'
import { LeaveStatus } from '../api'

export function StatusBadge({ status }: { status: LeaveStatus }) {
  return (
    <span className={`badge ${getStatusColor(status)}`}>
      {getStatusLabel(status)}
    </span>
  )
}
