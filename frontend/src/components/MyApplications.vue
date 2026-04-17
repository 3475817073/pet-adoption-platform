<template>
  <div>
    <el-page-header title="我的领养申请" content="查看申请记录与审核状态" />

    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="我提交的申请" name="my">
        <el-table :data="myApplications" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物" width="180">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 10px">
                <img v-if="row.pet.photoUrl" :src="row.pet.photoUrl" style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px" />
                <div v-else style="width: 50px; height: 50px; background: #f5f7fa; border-radius: 8px; display: flex; align-items: center; justify-content: center">🐾</div>
                <div>
                  <div style="font-weight: 500">{{ row.pet.name }}</div>
                  <div style="font-size: 12px; color: #999">{{ row.pet.type }} · {{ row.pet.gender }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="领养理由" min-width="200" show-overflow-tooltip />
          <el-table-column prop="contact" label="联系方式" width="150" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" effect="dark">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="reviewTime" label="审核时间" width="180">
            <template #default="{ row }">
              {{ row.reviewTime ? formatTime(row.reviewTime) : '-' }}
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="myApplications.length === 0" description="还没有提交过申请" :image-size="150">
          <el-button type="primary" @click="goToPetList">去浏览宠物</el-button>
        </el-empty>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
/**
 * 我的领养申请管理组件
 * 展示用户提交的领养申请记录，支持状态查看与基础交互
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('my')
const myApplications = ref([])
const receivedApplications = ref([])
const currentUser = ref(null)
const loading = ref(false)

/**
 * 获取本地存储中的当前登录用户信息
 * @returns {Object|null} 用户信息对象，未登录则返回 null
 */
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

/**
 * 格式化时间为本地系统默认格式
 * @param {string|Date} time - 原始时间数据
 * @returns {string} 格式化后的时间字符串
 */
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

/**
 * 获取申请状态对应的 Element Plus 标签颜色类型
 * @param {string} status - 申请状态码
 * @returns {string} 标签颜色类型
 */
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || ''
}

/**
 * 将申请状态码转换为中文显示
 * @param {string} status - 申请状态码
 * @returns {string} 中文状态文本
 */
const getStatusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

/**
 * 加载当前用户提交的领养申请列表
 */
const loadMyApplications = async () => {
  if (!currentUser.value) return
  loading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/adoption/my-applications?username=${currentUser.value.username}`)
    if (res.ok) {
      myApplications.value = await res.json()
    } else {
      ElMessage.error(await res.text())
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载救助者收到的领养申请列表
 */
const loadReceivedApplications = async () => {
  if (!currentUser.value || currentUser.value.role !== 'RESCUER') return
  loading.value = true
  try {
    // 预留接口
    receivedApplications.value = []
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 根据当前激活的标签页加载对应数据
 */
const loadData = () => {
  if (activeTab.value === 'my') {
    loadMyApplications()
  } else if (activeTab.value === 'received') {
    loadReceivedApplications()
  }
}

/**
 * 触发全局导航事件，跳转到宠物列表页面
 */
const goToPetList = () => {
  window.dispatchEvent(new CustomEvent('navigate', { detail: '1' }))
}

/**
 * 组件挂载时执行：获取用户信息并初始化加载申请数据
 */
onMounted(() => {
  currentUser.value = getCurrentUser()
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    return
  }
  loadData()
})
</script>
