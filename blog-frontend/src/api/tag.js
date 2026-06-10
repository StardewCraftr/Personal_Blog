import request from './request'

export const tagApi = {
  getAll() {
    return request.get('/tags/public/list')
  },

  create(name, link) {
    return request.post('/tags', null, { params: { name, link } })
  },

  update(id, name, link) {
    return request.put(`/tags/${id}`, null, { params: { name, link } })
  },

  delete(id) {
    return request.delete(`/tags/${id}`)
  }
}
