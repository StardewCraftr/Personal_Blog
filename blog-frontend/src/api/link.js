import request from './request'

export const linkApi = {
  getAll() {
    return request.get('/links/public/list')
  },

  create(name, url, logo, description, sort) {
    return request.post('/links', null, { params: { name, url, logo, description, sort } })
  },

  update(id, name, url, logo, description, sort, status) {
    return request.put(`/links/${id}`, null, { params: { name, url, logo, description, sort, status } })
  },

  delete(id) {
    return request.delete(`/links/${id}`)
  }
}
