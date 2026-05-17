import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const service = axios.create({ baseURL: '/api', timeout: 10000 })

service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) router.push('/login')
      return Promise.reject(res)
    }
    return res.data
  },
  error => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
