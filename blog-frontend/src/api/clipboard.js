import request from './request'

export const clipboardApi = {
  getAll() {
    return request.get('/clipboard/list')
  },

  create(title, content) {
    return request.post('/clipboard', null, { params: { title, content } })
  },

  update(id, title, content) {
    return request.put(`/clipboard/${id}`, null, { params: { title, content } })
  },

  delete(id) {
    return request.delete(`/clipboard/${id}`)
  }
}
