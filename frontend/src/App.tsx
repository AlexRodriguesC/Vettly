import { useEffect, useState, type FormEvent } from 'react'
import { ApiError, fetchCurrentUser, login, register, type User } from './api'

const TOKEN_KEY = 'vettly.token'

type Mode = 'login' | 'register'

function readToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

function writeToken(token: string | null) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
    // Storage unavailable (e.g. private mode); the session just won't survive a reload.
  }
}

export default function App() {
  const [token, setToken] = useState<string | null>(readToken)
  const [user, setUser] = useState<User | null>(null)
  const [mode, setMode] = useState<Mode>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    fetchCurrentUser(token)
      .then((me) => {
        if (!cancelled) setUser(me)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        if (err instanceof ApiError && err.status === 401) signOut()
        else setError(err instanceof Error ? err.message : 'Could not load your account')
      })
    return () => {
      cancelled = true
    }
  }, [token])

  function signOut() {
    writeToken(null)
    setToken(null)
    setUser(null)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setBusy(true)
    try {
      const auth = mode === 'login' ? await login(email, password) : await register(email, password)
      writeToken(auth.accessToken)
      setToken(auth.accessToken)
      setPassword('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong')
    } finally {
      setBusy(false)
    }
  }

  function switchMode(next: Mode) {
    setMode(next)
    setError(null)
  }

  if (token) {
    return (
      <main>
        <h1>Vettly</h1>
        {user ? <p>Signed in as {user.email}</p> : <p>Loading your account…</p>}
        {error && <p role="alert" className="error">{error}</p>}
        <button type="button" onClick={signOut}>
          Log out
        </button>
      </main>
    )
  }

  return (
    <main>
      <h1>Vettly</h1>
      <nav>
        <button type="button" aria-pressed={mode === 'login'} onClick={() => switchMode('login')}>
          Log in
        </button>
        <button type="button" aria-pressed={mode === 'register'} onClick={() => switchMode('register')}>
          Register
        </button>
      </nav>

      <form onSubmit={handleSubmit}>
        <label>
          Email
          <input
            type="email"
            autoComplete="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label>
          Password
          <input
            type="password"
            autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            required
            minLength={mode === 'register' ? 8 : undefined}
            maxLength={72}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        {error && <p role="alert" className="error">{error}</p>}
        <button type="submit" disabled={busy}>
          {mode === 'login' ? 'Log in' : 'Create account'}
        </button>
      </form>
    </main>
  )
}
