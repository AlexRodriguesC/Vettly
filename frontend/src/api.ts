export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export type User = {
  id: string
  email: string
  createdAt: string
}

type Problem = {
  detail?: string
  errors?: Record<string, string>
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init.headers },
  })
  if (!response.ok) {
    const problem: Problem = await response.json().catch(() => ({}))
    const fieldErrors = problem.errors
      ? Object.entries(problem.errors).map(([field, message]) => `${field}: ${message}`)
      : []
    const message =
      fieldErrors.length > 0
        ? fieldErrors.join('. ')
        : (problem.detail ?? `Request failed (${response.status})`)
    throw new ApiError(response.status, message)
  }
  return response.json() as Promise<T>
}

export function register(email: string, password: string) {
  return request<AuthResponse>('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function login(email: string, password: string) {
  return request<AuthResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function fetchCurrentUser(token: string) {
  return request<User>('/api/v1/users/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
}
