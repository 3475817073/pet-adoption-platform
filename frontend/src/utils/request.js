// 基础配置
const BASE_URL = 'http://localhost:8080'

/**
 * 统一请求封装
 */
export async function request(url, options = {}) {
    // 获取用户信息
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    // 构建完整 URL（支持查询参数）
    let fullUrl = BASE_URL + url
    if (options.params) {
        const queryString = new URLSearchParams(options.params).toString()
        if (queryString) {
            fullUrl += '?' + queryString
        }
    }

    // 构建请求配置
    const fetchOptions = {
        method: options.method || 'GET',
        headers: {
            'Content-Type': 'application/json',
            ...(user?.token && { 'Authorization': `Bearer ${user.token}` })
        }
    }

    // 如果有请求体，序列化
    if (options.body) {
        fetchOptions.body = JSON.stringify(options.body)
    }

    try {
        const response = await fetch(fullUrl, fetchOptions)

        // 处理 401 未授权（登录过期）
        if (response.status === 401) {
            localStorage.removeItem('user')
            window.location.href = '/login'
            throw new Error('请先登录')
        }

        // 处理 403 无权限
        if (response.status === 403) {
            throw new Error('无权限执行此操作')
        }

        // 处理其他错误
        if (!response.ok) {
            const errorText = await response.text()
            throw new Error(errorText || '请求失败')
        }

        // 兼容 JSON 和 纯文本响应
        const contentType = response.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
            return await response.json()
        } else {
            return await response.text()
        }

    } catch (error) {
        // 网络错误
        if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
            throw new Error('网络连接失败，请检查网络或后端服务')
        }
        throw error
    }
}

/**
 * GET 请求快捷方法
 */
export function get(url, params) {
    return request(url, { method: 'GET', params })
}

/**
 * POST 请求快捷方法
 */
export function post(url, body) {
    return request(url, { method: 'POST', body })
}

/**
 * DELETE 请求快捷方法
 */
export function del(url,params) {
    return request(url, { method: 'DELETE', params })
}

/**
 * PUT 请求快捷方法
 */
export function put(url, body) {
    return request(url, { method: 'PUT', body })
}