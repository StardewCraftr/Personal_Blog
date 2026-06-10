import request from './request'

export const uploadApi = {
  uploadChunk(formData) {
    return request.post('/files/upload/chunk', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  merge(identifier, filename, totalChunks) {
    return request.post('/files/upload/merge', null, {
      params: { identifier, filename, totalChunks }
    })
  },

  getUploadedChunks(identifier) {
    return request.get('/files/upload/exists', { params: { identifier } })
  },

  getAttachments(pageNum, pageSize) {
    return request.get('/files/list', { params: { pageNum, pageSize } })
  },

  delete(id) {
    return request.delete(`/files/${id}`)
  }
}
