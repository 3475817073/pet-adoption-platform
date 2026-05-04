import { ElMessage } from 'element-plus'

/**
 * 成功提示
 */
export const success = (message, duration = 3000) => {
    const messages = {
        '登录成功': '🎉 欢迎回来！登录成功',
        '注册成功': '✨ 注册成功！欢迎加入宠物领养大家庭',
        '发布成功': ' 发布成功！你的宠物信息已展示给更多人',
        '申请已提交': '💌 申请已提交！请耐心等待审核',
        '领养申请已提交': '💌 领养申请已提交！请耐心等待管理员审核',
        '操作成功': '✅ 操作成功',
        '修改成功': '✅ 修改成功！信息已更新',
        '更新成功': '✅ 更新成功！',
        '删除成功': ' 删除成功',
        '审核通过': '✅ 审核通过！',
        '审核拒绝': '❌ 已拒绝该申请',
        '回访记录添加成功': '📋 回访记录添加成功'
    }

    let displayMessage = message
    for (const [key, value] of Object.entries(messages)) {
        if (message.includes(key)) {
            displayMessage = value
            break
        }
    }

    const instance = ElMessage({
        message: displayMessage,
        type: 'success',
        duration: 0,
        showClose: true,
        customClass: 'custom-message success-message'
    })

    // 立即修改 DOM 实现点击穿透
    setTimeout(() => {
        const messageEl = document.querySelector('.el-message')
        if (messageEl) {
            messageEl.style.pointerEvents = 'none'
            messageEl.style.userSelect = 'none'

            const closeBtn = messageEl.querySelector('.el-message__closeBtn')
            if (closeBtn) {
                closeBtn.style.pointerEvents = 'auto'
                closeBtn.style.cursor = 'pointer'
            }
        }
    }, 0)

    setTimeout(() => {
        instance.close()
    }, duration)
}

/**
 * 警告提示
 */
export const warning = (message) => {
    const messages = {
        '请先登录': '请先登录才能进行此操作',
        '请填写完整的领养信息': ' 请填写完整的领养信息',
        '请填写必填项': '📝 请填写必填项',
        '这只宠物已经被领养啦': '🏠 这只宠物已经找到温暖的家啦',
        '请先登录才能申请领养': '请先登录才能申请领养',
        '信息不完整': ' 信息不完整，请检查后重新提交'
    }

    let displayMessage = message
    for (const [key, value] of Object.entries(messages)) {
        if (message.includes(key)) {
            displayMessage = value
            break
        }
    }

    const instance = ElMessage({
        message: displayMessage,
        type: 'warning',
        duration: 0,
        showClose: true,
        customClass: 'custom-message warning-message'
    })

    setTimeout(() => {
        const messageEl = document.querySelector('.el-message')
        if (messageEl) {
            messageEl.style.pointerEvents = 'none'
            messageEl.style.userSelect = 'none'

            const closeBtn = messageEl.querySelector('.el-message__closeBtn')
            if (closeBtn) {
                closeBtn.style.pointerEvents = 'auto'
                closeBtn.style.cursor = 'pointer'
            }
        }
    }, 0)

    setTimeout(() => {
        instance.close()
    }, 3000)
}

/**
 * 错误提示
 */
export const error = (message) => {
    const messages = {
        '加载失败': ' 加载失败，请稍后重试',
        '操作失败': '❌ 操作失败，请稍后重试',
        '添加失败': '❌ 添加失败，请稍后重试',
        '删除失败': '❌ 删除失败，请稍后重试',
        '获取宠物列表失败': '😔 获取宠物列表失败',
        '加载宠物详情失败': '😔 加载宠物详情失败',
        '无权限访问': '🔒 无权限访问，请使用管理员账号登录'
    }

    let displayMessage = message
    for (const [key, value] of Object.entries(messages)) {
        if (message.includes(key)) {
            displayMessage = value
            break
        }
    }

    const instance = ElMessage({
        message: displayMessage,
        type: 'error',
        duration: 0,
        showClose: true,
        customClass: 'custom-message error-message'
    })

    setTimeout(() => {
        const messageEl = document.querySelector('.el-message')
        if (messageEl) {
            messageEl.style.pointerEvents = 'none'
            messageEl.style.userSelect = 'none'

            const closeBtn = messageEl.querySelector('.el-message__closeBtn')
            if (closeBtn) {
                closeBtn.style.pointerEvents = 'auto'
                closeBtn.style.cursor = 'pointer'
            }
        }
    }, 0)

    setTimeout(() => {
        instance.close()
    }, 4000)
}

/**
 * 信息提示
 */
export const info = (message) => {
    const messages = {
        '这只宠物已经被领养啦': ' 这只宠物已经找到温暖的家啦~'
    }

    let displayMessage = message
    for (const [key, value] of Object.entries(messages)) {
        if (message.includes(key)) {
            displayMessage = value
            break
        }
    }

    const instance = ElMessage({
        message: displayMessage,
        type: 'info',
        duration: 0,
        showClose: true,
        customClass: 'custom-message info-message'
    })

    setTimeout(() => {
        const messageEl = document.querySelector('.el-message')
        if (messageEl) {
            messageEl.style.pointerEvents = 'none'
            messageEl.style.userSelect = 'none'

            const closeBtn = messageEl.querySelector('.el-message__closeBtn')
            if (closeBtn) {
                closeBtn.style.pointerEvents = 'auto'
                closeBtn.style.cursor = 'pointer'
            }
        }
    }, 0)

    setTimeout(() => {
        instance.close()
    }, 3000)
}
