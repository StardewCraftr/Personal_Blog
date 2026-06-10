import request from './request'

export const commentApi = {
  getByArticleId(articleId) {
    return request.get(`/comments/public/article/${articleId}`)
  },

  add(data) {
    return request.post('/comments', data)
  },

  delete(id) {
    return request.delete(`/comments/${id}`)
  }
}
