import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

const clientOptions = {
  baseURL,
  timeout: 10_000,
  withCredentials: true,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
}

export const apiClient = axios.create(clientOptions)
export const authClient = axios.create(clientOptions)
