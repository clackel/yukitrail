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

