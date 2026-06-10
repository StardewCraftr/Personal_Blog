import request from './request'

export const deviceApi = {
  getDevices() {
    return request.get('/devices')
  },
  logoutDevice(id) {
    return request.delete(`/devices/${id}`)
  },
  deleteDevice(id) {
    return request.delete(`/devices/record/${id}`)
  }
}
