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

<<<<<<< HEAD
// 认证接口使用独立实例，避免刷新请求再次触发业务请求的 401 拦截器。
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
export const apiClient = axios.create(clientOptions)
export const authClient = axios.create(clientOptions)
