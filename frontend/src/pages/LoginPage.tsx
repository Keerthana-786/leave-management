import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getDemoUsers,
  login as apiLogin,
  User,
} from '../api'
import { useAuth } from '../context/AuthContext'
import { getRoleColor } from '../utils/helpers'
import { Loader2, LogIn, Users, Zap } from 'lucide-react'

const PERSONA_EMAILS = [
  { email: 'riya@co.com', label: 'Riya Sen', hint: 'Employee', color: 'from-teal-900 to-teal-800' },
  { email: 'manager1@co.com', label: 'Manoj Kumar', hint: 'Manager', color: 'from-indigo-900 to-indigo-800' },
  { email: 'hr@co.com', label: 'Hema HR', hint: 'HR Admin', color: 'from-purple-900 to-purple-800' },
]

export default function LoginPage() {
  const { login } = useAuth()
  const qc = useQueryClient()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const { data: demoUsers = [] } = useQuery({
    queryKey: ['demo-users'],
    queryFn: () => getDemoUsers().then((r) => r.data),
    staleTime: 5 * 60 * 1000,
  })

  const loginMut = useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      apiLogin(email, password),
    onSuccess: (res) => {
      login(res.data.token, res.data.user)
      qc.clear()
    },
    onError: () => {
      setError('Invalid email or password')
    },
  })

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    loginMut.mutate({ email, password })
  }

  const quickLogin = (userEmail: string) => {
    setError('')
    loginMut.mutate({ email: userEmail, password: 'Demo@123' })
  }

  const demoPersonas = PERSONA_EMAILS.map((p) => {
    const user = demoUsers.find((u: User) => u.email === p.email)
    return { ...p, user }
  })

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col items-center justify-center p-4">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-br from-indigo-950/30 via-slate-950 to-purple-950/20 pointer-events-none" />

      <div className="relative w-full max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 bg-indigo-600/20 border border-indigo-500/30 rounded-full px-4 py-1.5 mb-4">
            <Zap className="w-3.5 h-3.5 text-indigo-400" />
            <span className="text-xs font-semibold text-indigo-400 uppercase tracking-widest">Hackathon Demo</span>
          </div>
          <h1 className="text-4xl font-bold text-white mb-2">Leave Management</h1>
          <p className="text-slate-400 text-lg">Approval chains, real-time updates, smart conflict detection</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Quick Persona Login */}
          <div className="card">
            <div className="flex items-center gap-2 mb-4">
              <Users className="w-4 h-4 text-indigo-400" />
              <h2 className="font-semibold text-white">One-Click Demo Login</h2>
            </div>
            <p className="text-slate-500 text-sm mb-4">Switch between personas to see the full approval workflow</p>
            <div className="space-y-3">
              {demoPersonas.map((p) => (
                <button
                  key={p.email}
                  id={`persona-${p.email.split('@')[0]}`}
                  onClick={() => quickLogin(p.email)}
                  disabled={loginMut.isPending}
                  className={`w-full bg-gradient-to-r ${p.color} hover:brightness-110 border border-white/10 rounded-xl p-4 flex items-center justify-between transition-all group`}
                >
                  <div className="text-left">
                    <div className="font-semibold text-white">{p.label}</div>
                    <div className="text-sm text-white/60">{p.email}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`badge ${p.hint === 'HR Admin' ? 'bg-purple-500/30 text-purple-300' : p.hint === 'Manager' ? 'bg-indigo-500/30 text-indigo-300' : 'bg-teal-500/30 text-teal-300'}`}>
                      {p.hint}
                    </span>
                    {loginMut.isPending ? (
                      <Loader2 className="w-4 h-4 text-white/50 animate-spin" />
                    ) : (
                      <LogIn className="w-4 h-4 text-white/50 group-hover:text-white transition-colors" />
                    )}
                  </div>
                </button>
              ))}
            </div>
            <p className="text-center text-slate-600 text-xs mt-4">Password: Demo@123</p>
          </div>

          {/* Manual Login */}
          <div className="card">
            <div className="flex items-center gap-2 mb-4">
              <LogIn className="w-4 h-4 text-indigo-400" />
              <h2 className="font-semibold text-white">Manual Login</h2>
            </div>
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="label">Email address</label>
                <input
                  id="email-input"
                  type="email"
                  className="input"
                  placeholder="user@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div>
                <label className="label">Password</label>
                <input
                  id="password-input"
                  type="password"
                  className="input"
                  placeholder="Demo@123"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              {error && (
                <div className="bg-rose-900/40 border border-rose-500/30 rounded-lg px-3 py-2 text-sm text-rose-400">
                  {error}
                </div>
              )}

              <button
                id="login-submit"
                type="submit"
                disabled={loginMut.isPending}
                className="btn-primary w-full flex items-center justify-center gap-2"
              >
                {loginMut.isPending ? (
                  <><Loader2 className="w-4 h-4 animate-spin" /> Logging in...</>
                ) : (
                  <><LogIn className="w-4 h-4" /> Sign In</>
                )}
              </button>
            </form>

            <div className="mt-6 border-t border-slate-800 pt-4">
              <p className="text-slate-500 text-xs font-medium mb-2 uppercase tracking-wider">All Demo Users</p>
              <div className="space-y-1 max-h-48 overflow-y-auto">
                {demoUsers.map((u: User) => (
                  <button
                    key={u.email}
                    onClick={() => quickLogin(u.email)}
                    className="w-full text-left flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-slate-800 transition-colors"
                  >
                    <span className={`badge text-xs ${getRoleColor(u.role)}`}>{u.role}</span>
                    <span className="text-slate-300 text-sm truncate">{u.name}</span>
                    <span className="text-slate-500 text-xs ml-auto">{u.email}</span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
