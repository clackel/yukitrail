import axios from 'axios'

import type { ApiEnvelope, ValidationErrorData } from '@/types/api'

export interface PresentedApiError {
  message: string
  fields: Record<string, string>
}

export const presentApiError = (error: unknown): PresentedApiError => {
  if (axios.isAxiosError<ApiEnvelope<ValidationErrorData | null>>(error) && error.response?.data) {
    return {
      message: error.response.data.message,
      fields: error.response.data.data?.fields ?? {},
    }
  }

  return {
    message: '暂时无法连接服务，请稍后重试。',
    fields: {},
  }
}
