import request from './request'

export const categoryApi = {
  getAll() {
    return request.get('/categories/public/list')
  },

  create(name, parentId, sort) {
    return request.post('/categories', null, { params: { name, parentId, sort } })
  },

  update(id, name, parentId, sort) {
    return request.put(`/categories/${id}`, null, { params: { name, parentId, sort } })
  },

  delete(id) {
    return request.delete(`/categories/${id}`)
  }
}
