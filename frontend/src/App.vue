<template>
  <el-container style="height: 100vh">
    <!-- 左侧菜单 -->
    <el-aside width="220px" style="position: relative; overflow: hidden">
      <svg class="sidebar-bg" viewBox="0 0 220 800" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <pattern id="sidebar-paw" x="0" y="0" width="80" height="80" patternUnits="userSpaceOnUse">
            <text x="10" y="40" font-size="24" opacity="0.08">🐾</text>
          </pattern>
        </defs>

        <rect width="220" height="800" fill="#3D405B"/>
        <rect width="220" height="800" fill="url(#sidebar-paw)"/>

        <circle cx="110" cy="80" r="150" fill="#E07A5F" opacity="0.12"/>
        <circle cx="50" cy="300" r="100" fill="#81B29A" opacity="0.08"/>
        <circle cx="180" cy="500" r="120" fill="#F2CC8F" opacity="0.1"/>
        <circle cx="100" cy="700" r="130" fill="#81B29A" opacity="0.06"/>

        <ellipse cx="60" cy="200" rx="80" ry="50" fill="#E07A5F" opacity="0.08" transform="rotate(-20 60 200)"/>
        <ellipse cx="160" cy="600" rx="70" ry="40" fill="#F2CC8F" opacity="0.08" transform="rotate(15 160 600)"/>
      </svg>

      <div style="position: relative; z-index: 1; padding: 35px 20px; text-align: center">
        <div style="font-size: 52px; margin-bottom: 12px; filter: drop-shadow(0 2px 8px rgba(0,0,0,0.3))">🐱</div>
        <div style="font-size: 17px; font-weight: 600; color: #FDF8F3; line-height: 1.6; letter-spacing: 0.5px; text-shadow: 0 2px 4px rgba(0,0,0,0.2)">
          宠物领养平台<br/>
          <span style="font-size: 11px; opacity: 0.9; font-weight: 400"></span>
        </div>
      </div>

      <el-menu :default-active="activeIndex" @select="handleMenuSelect"
               style="border-right: none; background: transparent; position: relative; z-index: 1"
               text-color="#E8EAED"
               active-text-color="#FDF8F3"
               class="sidebar-menu">
        <el-menu-item index="1">
          <span class="menu-icon">🐶</span> 宠物列表
        </el-menu-item>
        <el-menu-item index="2">
          <span class="menu-icon">📤</span> 发布宠物
        </el-menu-item>
        <el-menu-item index="3">
          <span class="menu-icon">💬</span> 互助交流
        </el-menu-item>
        <el-menu-item index="4">
          <span class="menu-icon">👤</span> 个人中心
        </el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN'" index="5">
          <span class="menu-icon">📋</span> 申请审核
        </el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN'" index="6">
          <span class="menu-icon">🐾</span> 宠物审核
        </el-menu-item>
        <el-menu-item v-if="userRole === 'ADMIN'" index="7">
          <span class="menu-icon">📝</span> 帖子审核
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header height="65px" style="background: #FDF8F3; border-bottom: 1px solid #E5E7EB; display: flex; align-items: center; justify-content: space-between; padding: 0 35px">
        <div>
          <span style="font-size: 20px; font-weight: 600; color: #3D405B; letter-spacing: 0.3px">
            宠物领养与互助服务平台
          </span>
        </div>
        <div style="display: flex; align-items: center; gap: 12px">

          <el-button v-if="!isLoggedIn" type="primary" @click="showLoginDialog"
                     class="login-btn">
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
      </el-header>

      <el-main style="background: #FDF8F3">
        <component :is="currentComponent" @needLogin="showLoginDialog" />
      </el-main>
    </el-container>
  </el-container>

  <!-- 登录弹窗 -->
  <Login
      v-model="showLogin"
      @loginSuccess="handleLoginSuccess"

  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PetList from './components/PetList.vue'
import PublishPet from './components/PublishPet.vue'
import HelpPost from './components/HelpPost.vue'
import AdminApplyManagement from './components/AdminApplyManagement.vue'
import AdminPetReview from './components/AdminPetReview.vue'
import AdminPostReview from './components/AdminPostReview.vue'
import MyCenter from './components/MyCenter.vue'
import Login from './components/Login.vue'


const activeIndex = ref('1')
const currentComponent = ref(PetList)
const isLoggedIn = ref(false)
const username = ref('')
const userRole = ref('')
const showLogin = ref(false)

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

const handleMenuSelect = (index) => {
  activeIndex.value = index
  if (index === '1') currentComponent.value = PetList
  else if (index === '2') currentComponent.value = PublishPet
  else if (index === '3') currentComponent.value = HelpPost
  else if (index === '4') currentComponent.value = MyCenter
  else if (index === '5') {
    if (userRole.value === 'ADMIN') {
      currentComponent.value = AdminApplyManagement
    } else {
      import('element-plus').then(({ ElMessage }) => {
        ElMessage.warning('无权限访问')
      })
    }
  }
  else if (index === '6') {
    if (userRole.value === 'ADMIN') {
      currentComponent.value = AdminPetReview
    } else {
      import('element-plus').then(({ ElMessage }) => {
        ElMessage.warning('无权限访问')
      })
    }
  }
  else if (index === '7') {
    if (userRole.value === 'ADMIN') {
      currentComponent.value = AdminPostReview
    } else {
      import('element-plus').then(({ ElMessage }) => {
        ElMessage.warning('无权限访问')
      })
    }
  }
}



const handleLoginSuccess = (user) => {
  isLoggedIn.value = true
  username.value = user.username || user.realName
  userRole.value = user.role
  localStorage.setItem('user', JSON.stringify(user))
}

const logout = () => {
  isLoggedIn.value = false
  username.value = ''
  userRole.value = ''
  localStorage.removeItem('user')
  currentComponent.value = PetList
  activeIndex.value = '1'
}

const showLoginDialog = () => {
  showLogin.value = true
}
</script>


<style>
.sidebar-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}

.sidebar-menu .el-menu-item {
  border-radius: 10px;
  margin: 6px 10px;
  position: relative;
  overflow: hidden;
}

.sidebar-menu .el-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 3px;
  height: 100%;
  background: transparent;
  transition: all 0.3s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

.sidebar-menu .el-menu-item:hover::before {
  background: #E07A5F;
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(224, 122, 95, 0.2) !important;
  color: #FDF8F3 !important;
}

.sidebar-menu .el-menu-item.is-active::before {
  background: #E07A5F;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
}

.role-badge.admin {
  background: #E07A5F;
  color: white;
}

.role-badge.user {
  background: #81B29A;
  color: white;
}

.login-btn {
  background: #E07A5F !important;
  border-color: #E07A5F !important;
  border-radius: 20px !important;
  padding: 8px 20px !important;
}

.user-dropdown {
  cursor: pointer;
  color: #E07A5F;
  font-weight: 500;
}
</style>
