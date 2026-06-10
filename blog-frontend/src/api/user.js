import request from './request'

export const userApi = {
  getProfile() {
    return request.get('/user/profile')
  },

  updateProfile(data) {
    return request.put('/user/profile', data)
  },

  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
