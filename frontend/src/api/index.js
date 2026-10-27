import http from './http'

export const authApi = {
  register: (data) => http.post('/auth/register', data),
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/auth/me'),
}

export const categoryApi = {
  list: (type) => http.get('/categories', { params: { type } }),
  create: (data) => http.post('/categories', data),
  update: (id, data) => http.put(`/categories/${id}`, data),
  remove: (id) => http.delete(`/categories/${id}`),
}

export const transactionApi = {
  list: (params) => http.get('/transactions', { params }),
  create: (data) => http.post('/transactions', data),
  update: (id, data) => http.put(`/transactions/${id}`, data),
  remove: (id) => http.delete(`/transactions/${id}`),
}

export const statsApi = {
  summary: (month) => http.get('/stats/summary', { params: { month } }),
  breakdown: (month) => http.get('/stats/breakdown', { params: { month } }),
  trend: (params) => http.get('/stats/trend', { params }),
}
