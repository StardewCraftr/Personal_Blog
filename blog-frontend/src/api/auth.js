import request from './request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },

  getCurrentUser() {
    return request.get('/auth/current')
  },

  updateProfile(data) {
    return request.put('/auth/profile', data)
  },

  sendCode(data) {
    return request.post('/auth/code', data)
  },

  resetPassword(data) {
    return request.post('/auth/reset-password', data)
  }
}
