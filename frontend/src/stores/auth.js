import { defineStore } from 'pinia'
import { authApi } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('ledger_token') || '',
    user: JSON.parse(localStorage.getItem('ledger_user') || 'null'),
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
  },
  actions: {
    setSession(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem('ledger_token', token)
      localStorage.setItem('ledger_user', JSON.stringify(user))
    },
    async login(payload) {
      const data = await authApi.login(payload)
      this.setSession(data.token, data.user)
    },
    async register(payload) {
      const data = await authApi.register(payload)
      this.setSession(data.token, data.user)
    },
    async fetchMe() {
      const user = await authApi.me()
      this.user = user
      localStorage.setItem('ledger_user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('ledger_token')
      localStorage.removeItem('ledger_user')
    },
  },
})
