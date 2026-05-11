<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="top-navbar">
      <div class="navbar-content">
        <!-- Logo -->
        <div class="navbar-logo" @click="router.push('/pets')">
          <img src="@/assets/top-icon.png" alt="宠物平台图标" class="logo-icon" />
          <span class="logo-text">宠物领养与互助服务平台</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="navbar-menu">
          <router-link to="/pets" class="nav-item" :class="{ active: route.path === '/pets' }">
            <span class="nav-icon"></span>
            <span>宠物列表</span>
          </router-link>
          <router-link to="/publish" class="nav-item" :class="{ active: route.path === '/publish' }">
            <span class="nav-icon"></span>
            <span>宠物发布</span>
          </router-link>
          <router-link to="/help" class="nav-item" :class="{ active: route.path === '/help' }">
            <span class="nav-icon"></span>
            <span>社区互助</span>
          </router-link>
          <router-link to="/center" class="nav-item" :class="{ active: route.path === '/center' }">
            <span class="nav-icon"></span>
            <span>个人中心</span>
          </router-link>

          <!-- 管理员菜单 -->
          <template v-if="userRole === 'ADMIN'">
            <!-- 审核下拉菜单 -->
            <el-dropdown @command="handleAdminCommand">
              <div class="nav-item" :class="{ active: isReviewPageActive }">
                <span class="nav-icon"></span>
                <span>审核管理</span>
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/admin/applications">
                    <span class="nav-icon"></span>
                    申请审核
                  </el-dropdown-item>
                  <el-dropdown-item command="/admin/pets">
                    <span class="nav-icon"></span>
                    宠物审核
                  </el-dropdown-item>
                  <el-dropdown-item command="/admin/posts">
                    <span class="nav-icon"></span>
                    帖子审核
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 用户管理独立菜单 -->
            <router-link to="/admin/users" class="nav-item" :class="{ active: route.path === '/admin/users' }">
              <span class="nav-icon"></span>
              <span>用户管理</span>
            </router-link>
          </template>
        </nav>

        <!-- 用户操作区 -->
        <div class="navbar-actions">
          <!-- 通知铃铛图标（仅登录用户可见） -->
          <div v-if="isLoggedIn" class="notification-wrapper">
            <button class="notification-btn" @click="toggleNotificationPanel">
              <svg class="bell-icon" viewBox="0 0 448 512">
                <path d="M224 0c-17.7 0-32 14.3-32 32V49.9C119.5 61.4 64 124.2 64 200v33.4c0 45.4-15.5 89.5-43.8 124.9L5.3 377c-5.8 7.2-6.9 17.1-2.9 25.4S14.8 416 24 416H424c9.2 0 17.6-5.3 21.6-13.6s2.9-18.2-2.9-25.4l-14.9-18.6C399.5 322.9 384 278.8 384 233.4V200c0-75.8-55.5-138.6-128-150.1V32c0-17.7-14.3-32-32-32zm0 96h8c57.4 0 104 46.6 104 104v33.4c0 47.9 13.9 94.6 39.7 134.6H72.3C98.1 328 112 281.3 112 233.4V200c0-57.4 46.6-104 104-104h8zm64 352H224 160c0 17 6.7 33.3 18.7 45.3s28.3 18.7 45.3 18.7s33.3-6.7 45.3-18.7s18.7-28.3 18.7-45.3z"></path>
              </svg>
              <!-- 未读数量徽章 -->
              <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
            </button>

            <!-- 通知面板（点击铃铛后显示） -->
            <div v-if="showNotificationPanel" class="notification-panel">
              <div class="panel-header">
                <h3>通知中心</h3>
                <div class="panel-actions">
                  <button class="mark-all-read-btn" @click="markAllAsRead" v-if="unreadCount > 0">
                    全部已读
                  </button>
                  <button class="close-panel-btn" @click="showNotificationPanel = false">✕</button>
                </div>
              </div>

              <!-- 面板主体（通知列表） -->
              <div class="panel-body" v-loading="notificationsLoading">
                <!-- 空状态 -->
                <div v-if="notifications.length === 0" class="empty-notifications">
                  暂无通知
                </div>

                <!-- 通知列表 -->
                <div
                    v-for="notif in notifications"
                    :key="notif.id"
                    class="notification-item"
                    :class="{ 'unread': !notif.read }"
                    @click="handleNotificationClick(notif)"
                >
                  <!-- 通知类型图标 -->
                  <div class="notif-icon">
                    {{ notif.type === 'MESSAGE' ? '✉️' : '💬' }}
                  </div>

                  <!-- 通知内容 -->
                  <div class="notif-content">
                    <div class="notif-title">{{ notif.title }}</div>
                    <div class="notif-text">{{ notif.content }}</div>
                    <div class="notif-time">{{ formatNotificationTime(notif.createTime) }}</div>
                  </div>

                  <!-- 未读标记（小红点） -->
                  <div v-if="!notif.read" class="unread-dot"></div>
                </div>
              </div>

              <!-- 面板底部（分页） -->
              <div v-if="notifications.length > 0" class="panel-footer">
                <el-pagination
                    v-model:current-page="notifCurrentPage"
                    :page-size="10"
                    :total="notifTotal"
                    layout="prev, pager, next"
                    @current-change="loadNotifications"
                    small
                />
              </div>
            </div>
          </div>

          <!-- 登录/注册按钮（未登录时显示） -->
          <el-button v-if="!isLoggedIn" type="primary" @click="showLoginDialog" class="login-btn">
            登录 / 注册
          </el-button>

          <!-- 用户下拉菜单（已登录时显示） -->
          <el-dropdown v-else>
            <span class="user-dropdown">{{ username }} <el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <keep-alive :include="['PetList']">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>
  </div>

  <!-- 登录弹窗 -->
  <Login
      v-model="showLogin"
      @loginSuccess="handleLoginSuccess"
  />
</template>

<script setup>
import { ArrowDown } from '@element-plus/icons-vue'
import {ref, onMounted, provide, onUnmounted, computed} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Login from './components/Login.vue'
import { get, post } from './utils/request.js'

const route = useRoute()
const router = useRouter()

// 用户登录状态
const isLoggedIn = ref(false)
const username = ref('')
const userRole = ref('')
const showLogin = ref(false)

// 判断当前是否在审核页面
const isReviewPageActive = computed(() => {
  return route.path.startsWith('/admin/applications') ||
      route.path.startsWith('/admin/pets') ||
      route.path.startsWith('/admin/posts')
})

// 处理审核下拉菜单点击
const handleAdminCommand = (command) => {
  router.push(command)
}

// 通知相关状态
const showNotificationPanel = ref(false)        // 是否显示通知面板
const notifications = ref([])                   // 通知列表
const notificationsLoading = ref(false)         // 是否正在加载通知
const unreadCount = ref(0)                      // 未读通知数量
const notifCurrentPage = ref(1)                 // 通知列表当前页码
const notifTotal = ref(0)                       // 通知总数

// 定时刷新通知的计时器
let notificationTimer = null

// 提供触发登录弹窗的方法给子组件
const triggerLogin = () => {
  showLogin.value = true
}
provide('triggerLogin', triggerLogin)

/**
 * 格式化通知时间
 * 将时间转换为"刚刚"、"5分钟前"、"2小时前"等友好格式
 */
const formatNotificationTime = (timeStr) => {
  if (!timeStr) return ''
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time

  const minutes = Math.floor(diff / 60000)          // 转换为分钟
  const hours = Math.floor(diff / 3600000)          // 转换为小时
  const days = Math.floor(diff / 86400000)          // 转换为天

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  // 超过7天显示具体日期
  return `${time.getFullYear()}-${String(time.getMonth() + 1).padStart(2, '0')}-${String(time.getDate()).padStart(2, '0')}`
}

/**
 * 加载未读通知数量
 * 每30秒自动调用一次
 */
const loadUnreadCount = async () => {
  if (!isLoggedIn.value || !username.value) return

  try {
    const data = await get('/api/notification/unread-count', { username: username.value })
    unreadCount.value = data.count || 0
  } catch (err) {
    console.error('加载未读通知数量失败:', err)
  }
}

/**
 * 加载通知列表
 * 点击铃铛图标时调用
 */
const loadNotifications = async (page = 1) => {
  if (!isLoggedIn.value || !username.value) return

  notificationsLoading.value = true
  try {
    const data = await get('/api/notification/list', {
      username: username.value,
      page: page - 1,
      size: 10
    })
    notifications.value = data.content || []
    notifTotal.value = data.totalElements || 0
    notifCurrentPage.value = page
  } catch (err) {
    console.error('加载通知列表失败:', err)
  } finally {
    notificationsLoading.value = false
  }
}

/**
 * 切换通知面板显示/隐藏
 */
const toggleNotificationPanel = () => {
  showNotificationPanel.value = !showNotificationPanel.value
  // 打开面板时加载通知列表
  if (showNotificationPanel.value) {
    loadNotifications()
  }
}

/**
 * 标记所有通知为已读
 */
const markAllAsRead = async () => {
  try {
    await post('/api/notification/read-all', null, { username: username.value })
    unreadCount.value = 0
    // 更新本地通知列表的已读状态
    notifications.value.forEach(n => n.read = true)
  } catch (err) {
    console.error('标记已读失败:', err)
  }
}

/**
 * 处理通知点击事件
 * 点击通知后：标记为已读 + 跳转到对应页面
 */
const handleNotificationClick = async (notif) => {
  // 如果未读，先标记为已读
  if (!notif.read) {
    try {
      await post(`/api/notification/read/${notif.id}`, null, { username: username.value })
      notif.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (err) {
      console.error('标记通知已读失败:', err)
    }
  }

  // 根据通知类型跳转到不同页面
  if (notif.type === 'MESSAGE') {
    // 私信通知：跳转到发送者的主页
    router.push(`/user/${notif.senderUsername}`)
  } else if (notif.type === 'COMMENT') {
    // 评论通知：跳转到对应的帖子
    router.push(`/post/${notif.relatedId}`)
  }

  // 关闭通知面板
  showNotificationPanel.value = false
}

/**
 * 启动定时刷新通知
 * 每30秒检查一次新通知
 */
const startNotificationPolling = () => {
  notificationTimer = setInterval(() => {
    loadUnreadCount()
  }, 30000) // 30秒 = 30000毫秒
}

onMounted(() => {
  // 检查登录状态
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const userData = JSON.parse(userStr)
      isLoggedIn.value = true
      username.value = userData.username || userData.realName
      userRole.value = userData.role
      // 加载未读通知数量
      loadUnreadCount()
      // 启动定时刷新
      startNotificationPolling()
    } catch (e) {
      localStorage.removeItem('user')
    }
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (notificationTimer) {
    clearInterval(notificationTimer)
  }
})

/**
 * 登录成功处理
 */
const handleLoginSuccess = (user) => {
  isLoggedIn.value = true
  username.value = user.username || user.realName
  userRole.value = user.role
  localStorage.setItem('user', JSON.stringify(user))

  // 登录成功后加载通知
  loadUnreadCount()
  startNotificationPolling()

  // 通知其他组件登录状态已改变
  window.dispatchEvent(new StorageEvent('storage', {
    key: 'user',
    newValue: JSON.stringify(user)
  }))
}

/**
 * 退出登录
 */
const logout = () => {
  isLoggedIn.value = false
  username.value = ''
  userRole.value = ''
  unreadCount.value = 0
  notifications.value = []
  localStorage.removeItem('user')

  // 清除定时器
  if (notificationTimer) {
    clearInterval(notificationTimer)
  }

  router.push('/pets')
}

/**
 * 显示登录对话框
 */
const showLoginDialog = () => {
  showLogin.value = true
}
</script>



<style>
/* ===== 全局重置 ===== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  min-height: 100%;
  height: auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  overflow-x: hidden;
  overflow-y: auto;
}

#app {
  height: 100%;
  width: 100%;
}

.app-container {
  min-height: 100vh;
  background: #f9fafb;
  width: 100%;
}

/* ===== 自定义消息提示框样式 ===== */
.custom-message {
  border-radius: 16px !important;
  padding: 16px 20px !important;
  border: 2px solid !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  min-width: 320px !important;
}

.success-message {
  background: rgba(16, 185, 129, 0.08) !important;
  border-color: rgba(16, 185, 129, 0.3) !important;
  color: #065f46 !important;
}

.warning-message {
  background: rgba(245, 158, 11, 0.08) !important;
  border-color: rgba(245, 158, 11, 0.3) !important;
  color: #92400e !important;
}

.error-message {
  background: rgba(239, 68, 68, 0.08) !important;
  border-color: rgba(239, 68, 68, 0.3) !important;
  color: #991b1b !important;
}

.info-message {
  background: rgba(59, 130, 246, 0.08) !important;
  border-color: rgba(59, 130, 246, 0.3) !important;
  color: #1e40af !important;
}

.custom-message .el-message__content {
  line-height: 1.6 !important;
}

.custom-message .el-message__closeBtn {
  font-size: 18px !important;
  opacity: 0.6 !important;
  transition: opacity 0.2s !important;
}

.custom-message .el-message__closeBtn:hover {
  opacity: 1 !important;
}

/* ===== 顶部导航栏 ===== */
.top-navbar {
  background: #ffffff;
  border-bottom: 2px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

/* Logo */
.navbar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.navbar-logo:hover {
  transform: scale(1.02);
}

.logo-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* 导航菜单 */
.navbar-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  text-decoration: none;
  color: #4b5563;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.2s ease;
  position: relative;
  white-space: nowrap;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #cc785c;
  transform: translateY(-2px);
}

.nav-item.active {
  background: rgba(204, 120, 92, 0.1);
  color: #cc785c;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #cc785c;
  border-radius: 2px;
}

.nav-icon {
  font-size: 18px;
}

.dropdown-arrow {
  margin-left: 4px;
  font-size: 12px;
  transition: transform 0.3s;
}

.nav-item:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 用户操作区 */
.navbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 登录按钮 */
.login-btn {
  background: #cc785c !important;
  border: none !important;
  border-radius: 12px !important;
  padding: 10px 24px !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  transition: all 0.2s ease !important;
  box-shadow: 0 4px 12px rgba(204, 120, 92, 0.25) !important;
}

.login-btn:hover {
  background: #a9583e !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(204, 120, 92, 0.35) !important;
}

/* 用户下拉菜单 */
.user-dropdown {
  cursor: pointer;
  color: #cc785c;
  font-weight: 600;
  padding: 8px 16px;
  border: 2px solid #e8d5c4;
  border-radius: 12px;
  background: rgba(204, 120, 92, 0.05);
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.user-dropdown:hover {
  background: rgba(204, 120, 92, 0.1);
  border-color: #cc785c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(204, 120, 92, 0.15);
}

/* ===== 主内容区 - 全宽布局 ===== */
.main-content {
  width: 100%;
  min-height: calc(100vh - 70px);
  background: #f9fafb;
}

/* ===== 响应式设计 ===== */
@media (max-width: 1200px) {
  .navbar-content {
    padding: 0 20px;
    gap: 20px;
  }

  .nav-item {
    padding: 8px 14px;
    font-size: 14px;
  }

  .nav-icon {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 0 15px;
    gap: 10px;
  }

  .logo-text {
    font-size: 16px;
  }

  .navbar-menu {
    gap: 4px;
  }

  .nav-item {
    padding: 6px 10px;
    font-size: 13px;
  }

  .nav-item span:not(.nav-icon) {
    display: none;
  }
}

/* ===== 响应式设计 ===== */
@media (max-width: 1200px) {
  .navbar-content {
    padding: 0 20px;
    gap: 20px;
  }

  .nav-item {
    padding: 8px 14px;
    font-size: 14px;
  }

  .nav-icon {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 0 15px;
    gap: 10px;
  }

  .logo-text {
    font-size: 16px;
  }

  .navbar-menu {
    gap: 4px;
  }

  .nav-item {
    padding: 6px 10px;
    font-size: 13px;
  }

  .nav-item span:not(.nav-icon) {
    display: none;
  }
}

/* ===== 通知铃铛按钮样式（参考 notification_better.html 设计） ===== */
.notification-wrapper {
  position: relative;
}

.notification-btn {
  width: 50px;
  height: 50px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #cc785c;
  border-radius: 50%;
  cursor: pointer;
  transition-duration: .3s;
  box-shadow: 2px 2px 10px rgba(204, 120, 92, 0.25);
  border: none;
  padding: 0;
}

.notification-btn:hover {
  background-color: #a9583e;
  box-shadow: 2px 2px 15px rgba(204, 120, 92, 0.4);
}

.notification-btn:hover .bell-icon {
  animation: bellRing 0.9s both;
}

/* 铃铛摇晃动画 */
@keyframes bellRing {
  0%,
  100% {
    transform-origin: top;
  }

  15% {
    transform: rotateZ(10deg);
  }

  30% {
    transform: rotateZ(-10deg);
  }

  45% {
    transform: rotateZ(5deg);
  }

  60% {
    transform: rotateZ(-5deg);
  }

  75% {
    transform: rotateZ(2deg);
  }
}

.notification-btn:active {
  transform: scale(0.85);
}

/* 铃铛图标样式 */
.bell-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.bell-icon path {
  fill: white;
}

/* 未读数量徽章（圆形按钮上的小红点） */
.notification-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 700;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #cc785c;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.4);
}

/* ===== 通知面板样式 ===== */
.notification-panel {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 420px;
  max-height: 500px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 面板头部 */
.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}

.panel-actions {
  display: flex;
  gap: 12px;
}

.mark-all-read-btn {
  background: none;
  border: none;
  color: #cc785c;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.mark-all-read-btn:hover {
  background: rgba(204, 120, 92, 0.1);
}

.close-panel-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-panel-btn:hover {
  background: #f3f4f6;
  color: #4b5563;
}

/* 面板主体 */
.panel-body {
  flex: 1;
  overflow-y: auto;
  max-height: 380px;
}

.empty-notifications {
  padding: 40px 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

/* 通知列表项 */
.notification-item {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  position: relative;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item.unread {
  background: rgba(204, 120, 92, 0.05);
}

.notif-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.notif-content {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
  margin-bottom: 4px;
}

.notif-text {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notif-time {
  color: #9ca3af;
  font-size: 12px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #cc785c;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 8px;
}

/* 面板底部 */
.panel-footer {
  padding: 12px 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
}

</style>