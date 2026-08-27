/** 后端统一响应结构。 */
export interface ApiEnvelope<T> {
  code: string
  message: string
  data: T
  traceId: string
}

export interface HealthData {
  service: string
  status: 'UP'
}

export interface UserData {
  id: number
  email: string
  nickname: string
}

export interface AuthSessionData {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: UserData
}

export interface ValidationErrorData {
  fields: Record<string, string>
}
