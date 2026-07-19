import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const saved = localStorage.getItem('quizsphere-auth')
  if (saved) {
    const { token } = JSON.parse(saved)
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('quizsphere-auth')
    }
    return Promise.reject(error)
  }
)

export const errorMessage = (error) => {
  const data = error.response?.data
  if (data?.validationErrors) return Object.values(data.validationErrors).join(', ')
  return data?.message || error.message || 'Something went wrong'
}

export default api
