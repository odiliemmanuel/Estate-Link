import type { AxiosError } from 'axios'

// The microservices return errors in a few shapes:
//   { "error": "message" }                     -> business error
//   { "message": "..." }                       -> some services use this key
//   { "field": "message", ... }                -> bean validation errors
//   "plain string body"                        -> register catch-all
//   empty/null body (401 bad password, 403 inactive)
//   HTML error page (gateway/service down)     -> never shown to the user
export class ApiError extends Error {
  readonly status?: number
  readonly fieldErrors: Record<string, string>

  constructor(message: string, status?: number, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

// Friendly, user-facing messages for HTTP statuses. Internal details such as
// stack traces, service names or HTML error pages must never reach the user.
const HTTP_STATUS_MESSAGES: Record<number, string> = {
  400: 'The request could not be processed. Please check your details and try again.',
  401: 'Invalid email or password.',
  403: 'Your account is not active. Verify your email or contact an admin.',
  404: 'The requested resource was not found.',
  408: 'The request timed out. Please try again.',
  409: 'This action could not be completed because of a conflict with existing data.',
  422: 'The request could not be processed. Please check your details and try again.',
  429: 'Too many requests. Please wait a moment and try again.',
  500: 'Something went wrong on our end. Please try again later.',
  502: 'Our service is still starting up. Give it a few seconds and try again.',
  503: 'Our service is temporarily unavailable. Please try again in a moment.',
  504: 'The server took too long to respond. Please try again.',
}

const NETWORK_ERROR_MESSAGE = 'Cannot reach the server. Check your internet connection and try again.'
const GENERIC_ERROR_MESSAGE = 'Something went wrong. Please try again.'

// Only short, plain-text bodies are safe to surface to the user. HTML error
// pages and anything that looks like a stack trace or markup is discarded.
function isSafeMessage(value: unknown): value is string {
  if (typeof value !== 'string') return false
  const trimmed = value.trim()
  if (trimmed.length === 0 || trimmed.length > 300) return false
  if (/<[a-z][\s\S]*>/i.test(trimmed)) return false
  if (/(Exception|Error| at [\w.]+\(|\bCaused by:|\bat\s+com\.)/.test(trimmed)) return false
  return true
}

export function extractErrorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return isSafeMessage(error.message) ? error.message : GENERIC_ERROR_MESSAGE
  return GENERIC_ERROR_MESSAGE
}

export function toApiError(error: AxiosError<unknown>): ApiError {
  const status = error.response?.status
  const body = error.response?.data

  if (typeof body === 'string' && isSafeMessage(body)) {
    return new ApiError(body.trim(), status)
  }

  if (body && typeof body === 'object') {
    const record = body as Record<string, unknown>
    for (const key of ['error', 'message'] as const) {
      if (isSafeMessage(record[key])) {
        return new ApiError(record[key] as string, status)
      }
    }
    // Bean-validation style: a map of field -> message.
    const fieldErrors: Record<string, string> = {}
    for (const [field, message] of Object.entries(record)) {
      if (isSafeMessage(message)) fieldErrors[field] = message as string
    }
    if (Object.keys(fieldErrors).length > 0) {
      const first = Object.values(fieldErrors)[0]
      return new ApiError(first ?? GENERIC_ERROR_MESSAGE, status, fieldErrors)
    }
  }

  if (status !== undefined) {
    return new ApiError(HTTP_STATUS_MESSAGES[status] ?? GENERIC_ERROR_MESSAGE, status)
  }

  return new ApiError(NETWORK_ERROR_MESSAGE)
}
