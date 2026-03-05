const BASE_URL = 'http://localhost:9090'

async function request(method, url, data, params) {
  const token = localStorage.getItem('token')
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  let fullUrl = BASE_URL + url
  if (params) {
    const qs = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v !== null && v !== undefined && v !== ''))
    ).toString()
    if (qs) fullUrl += '?' + qs
  }

  const options = { method, headers }
  if (data !== undefined && method !== 'GET') {
    options.body = JSON.stringify(data)
  }

  const res = await fetch(fullUrl, options)
  const json = await res.json()
  if (json.code !== 200) throw new Error(json.message || '请求失败')
  return json.data
}

export const http = {
  get: (url, params) => request('GET', url, undefined, params),
  post: (url, data) => request('POST', url, data),
  put: (url, data) => request('PUT', url, data),
  delete: (url) => request('DELETE', url),
}
