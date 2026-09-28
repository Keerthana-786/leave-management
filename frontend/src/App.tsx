import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import MyLeavesPage from './pages/MyLeavesPage'
import { ManagerQueuePage, HrQueuePage } from './pages/ApprovalQueuePage'
import NotificationsPage from './pages/NotificationsPage'
import AnalyticsPage from './pages/AnalyticsPage'
import CalendarPage from './pages/CalendarPage'

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <>{children}</> : <Navigate to="/" replace />
}

function RoleRoute({ children, roles }: { children: React.ReactNode; roles: string[] }) {
  const { user } = useAuth()
  if (!user || !roles.includes(user.role)) return <Navigate to="/app/dashboard" replace />
  return <>{children}</>
}

function AppRoutes() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      <Route
        path="/"
        element={isAuthenticated ? <Navigate to="/app/dashboard" replace /> : <LoginPage />}
      />
      <Route
        path="/app"
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="my-leaves" element={<MyLeavesPage />} />
        <Route
          path="manager"
          element={
            <RoleRoute roles={['MANAGER', 'HR']}>
              <ManagerQueuePage />
            </RoleRoute>
          }
        />
        <Route
          path="hr"
          element={
            <RoleRoute roles={['HR']}>
              <HrQueuePage />
            </RoleRoute>
          }
        />
        <Route path="calendar" element={<CalendarPage />} />
        <Route
          path="analytics"
          element={
            <RoleRoute roles={['MANAGER', 'HR']}>
              <AnalyticsPage />
            </RoleRoute>
          }
        />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="*" element={<Navigate to="dashboard" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}
