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
            <span class="nav-icon">🐶</span>
            <span>宠物列表</span>
          </router-link>
          <router-link to="/publish" class="nav-item" :class="{ active: route.path === '/publish' }">
            <span class="nav-icon">📤</span>
            <span>宠物发布</span>
          </router-link>
          <router-link to="/help" class="nav-item" :class="{ active: route.path === '/help' }">
            <span class="nav-icon">💬</span>
            <span>社区互助</span>
          </router-link>
          <router-link to="/center" class="nav-item" :class="{ active: route.path === '/center' }">
            <span class="nav-icon">👤</span>
            <span>个人中心</span>
          </router-link>

          <!-- 管理员菜单 -->
          <template v-if="userRole === 'ADMIN'">
            <router-link to="/admin/applications" class="nav-item" :class="{ active: route.path === '/admin/applications' }">
              <span class="nav-icon">📋</span>
              <span>申请审核</span>
            </router-link>
            <router-link to="/admin/pets" class="nav-item" :class="{ active: route.path === '/admin/pets' }">
              <span class="nav-icon">🐾</span>
              <span>宠物审核</span>
            </router-link>
            <router-link to="/admin/posts" class="nav-item" :class="{ active: route.path === '/admin/posts' }">
              <span class="nav-icon">📝</span>
              <span>帖子审核</span>
            </router-link>
          </template>
        </nav>

        <!-- 用户操作区 -->
        <div class="navbar-actions">
          <el-button v-if="!isLoggedIn" type="primary" @click="showLoginDialog" class="login-btn">
            登录 / 注册
          </el-button>
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
import { ref, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Login from './components/Login.vue'

const route = useRoute()
const router = useRouter()

const isLoggedIn = ref(false)
const username = ref('')
const userRole = ref('')
const showLogin = ref(false)

// 提供触发登录弹窗的方法给子组件
const triggerLogin = () => {
  showLogin.value = true
}
provide('triggerLogin', triggerLogin)

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const userData = JSON.parse(userStr)
      isLoggedIn.value = true
      username.value = userData.username || userData.realName
      userRole.value = userData.role
    } catch (e) {
      localStorage.removeItem('user')
    }
  }
})

const handleLoginSuccess = (user) => {
  isLoggedIn.value = true
  username.value = user.username || user.realName
  userRole.value = user.role
  localStorage.setItem('user', JSON.stringify(user))

  // 手动触发 storage 事件，通知其他组件登录状态已改变
  window.dispatchEvent(new StorageEvent('storage', {
    key: 'user',
    newValue: JSON.stringify(user)
  }))
}

const logout = () => {
  isLoggedIn.value = false
  username.value = ''
  userRole.value = ''
  localStorage.removeItem('user')
  router.push('/pets')
}

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

</style>
