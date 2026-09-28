import React, { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getNotifications } from '../api'
import {
  LayoutDashboard, Clock, CheckSquare, Bell, BarChart3,
  LogOut, Calendar, ChevronDown, Users
} from 'lucide-react'
import { getRoleColor } from '../utils/helpers'

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [mobileOpen, setMobileOpen] = useState(false)

  const { data: notifications = [] } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => getNotifications().then((r) => r.data),
    refetchInterval: 15000,
    enabled: !!user,
  })

  const unreadCount = notifications.filter((n: any) => !n.read).length

  const handleLogout = () => {
    logout()
    qc.clear()
    navigate('/')
  }

  const navLinks = [
    { to: '/app/dashboard', icon: LayoutDashboard, label: 'Dashboard', roles: ['EMPLOYEE', 'MANAGER', 'HR'] },
    { to: '/app/my-leaves', icon: Clock, label: 'My Leaves', roles: ['EMPLOYEE', 'MANAGER', 'HR'] },
    { to: '/app/manager', icon: CheckSquare, label: 'Manager Queue', roles: ['MANAGER', 'HR'] },
    { to: '/app/hr', icon: Users, label: 'HR Queue', roles: ['HR'] },
    { to: '/app/calendar', icon: Calendar, label: 'Team Calendar', roles: ['EMPLOYEE', 'MANAGER', 'HR'] },
    { to: '/app/analytics', icon: BarChart3, label: 'Analytics', roles: ['MANAGER', 'HR'] },
  ].filter((l) => user && l.roles.includes(user.role))

  return (
    <div className="min-h-screen bg-slate-950 flex">
      {/* Sidebar */}
      <aside className="hidden md:flex flex-col w-60 bg-slate-900 border-r border-slate-800 min-h-screen">
        <div className="p-5 border-b border-slate-800">
          <h1 className="text-lg font-bold text-white">LeaveFlow</h1>
          <p className="text-xs text-slate-500 mt-0.5">Leave Management</p>
        </div>

        <nav className="flex-1 p-3 space-y-1">
          {navLinks.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              id={`nav-${link.label.toLowerCase().replace(/\s+/g, '-')}`}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-indigo-600/20 text-indigo-400 border border-indigo-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
                }`
              }
            >
              <link.icon className="w-4 h-4 flex-shrink-0" />
              {link.label}
            </NavLink>
          ))}

          <NavLink
            to="/app/notifications"
            id="nav-notifications"
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive
                  ? 'bg-indigo-600/20 text-indigo-400 border border-indigo-500/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`
            }
          >
            <Bell className="w-4 h-4 flex-shrink-0" />
            Notifications
            {unreadCount > 0 && (
              <span className="ml-auto bg-rose-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </NavLink>
        </nav>

        {/* User footer */}
        <div className="p-3 border-t border-slate-800">
          <div className="flex items-center gap-3 px-3 py-2.5 rounded-lg">
            <div className="w-8 h-8 rounded-full bg-indigo-600/30 border border-indigo-500/30 flex items-center justify-center text-indigo-400 font-semibold text-sm flex-shrink-0">
              {user?.name?.[0]}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{user?.name}</p>
              <span className={`badge text-xs mt-0.5 ${getRoleColor(user?.role ?? '')}`}>{user?.role}</span>
            </div>
          </div>
          <button
            id="logout-btn"
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 mt-1 text-sm text-slate-500 hover:text-rose-400 hover:bg-rose-900/20 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Mobile top bar */}
        <header className="md:hidden flex items-center justify-between px-4 py-3 bg-slate-900 border-b border-slate-800">
          <h1 className="font-bold text-white">LeaveFlow</h1>
          <div className="flex items-center gap-2">
            {unreadCount > 0 && (
              <span className="bg-rose-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                {unreadCount}
              </span>
            )}
            <button onClick={handleLogout} className="text-slate-400 hover:text-rose-400">
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </header>

        {/* Mobile nav */}
        <nav className="md:hidden flex overflow-x-auto bg-slate-900 border-b border-slate-800 px-2 py-1 gap-1">
          {navLinks.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `flex flex-col items-center gap-0.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex-shrink-0 ${
                  isActive ? 'bg-indigo-600/20 text-indigo-400' : 'text-slate-500 hover:text-slate-200'
                }`
              }
            >
              <link.icon className="w-4 h-4" />
              {link.label.split(' ')[0]}
            </NavLink>
          ))}
        </nav>

        {/* Page content */}
        <main className="flex-1 p-4 md:p-6 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
