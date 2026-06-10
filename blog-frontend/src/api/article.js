import request from './request'

export const articleApi = {
  getDetail(id) {
    return request.get(`/articles/public/${id}`)
  },

  getForEdit(id) {
    return request.get(`/articles/edit/${id}`)
  },

  getList(params) {
    return request.get('/articles/public/list', { params })
  },

  getMyArticles(params) {
    return request.get('/articles/my', { params })
  },

  getDrafts(params) {
    return request.get('/articles/drafts', { params })
  },

  create(data) {
    return request.post('/articles', data)
  },

  update(data) {
    return request.put('/articles', data)
  },

  delete(id) {
    return request.delete(`/articles/${id}`)
  },

  like(id) {
    return request.post(`/articles/${id}/like`)
  },

  getDates(year, month) {
    return request.get('/articles/public/dates', { params: { year, month } })
  }
}
