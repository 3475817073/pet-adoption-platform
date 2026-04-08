<template>
  <div>
    <el-page-header title="领养申请管理" content="审核领养者的申请" />

    <el-tabs v-model="activeTab">
      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pendingList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物" width="150">
            <template #default="{ row }">
              {{ row.pet.name }}
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="150">
            <template #default="{ row }">
              {{ row.adopter.username }}
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="领养理由" />
          <el-table-column prop="contact" label="联系方式" width="150" />
          <el-table-column prop="applyTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="review(row.id, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="review(row.id, 'reject')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="所有申请" name="all">
        <el-table :data="allList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物" width="150">
            <template #default="{ row }">
              {{ row.pet.name }}
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="150">
            <template #default="{ row }">
              {{ row.adopter.username }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
                {{ statusText(row.status) }}
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('pending')
const pendingList = ref([])
const allList = ref([])
const currentUser = ref(null)

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const statusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

const loadPending = async () => {
  if (!currentUser.value) return
  try {
    const res = await fetch(`http://localhost:8080/api/adoption/pending?username=${currentUser.value.username}`)
    if (res.ok) {
      pendingList.value = await res.json()
    } else {
      ElMessage.error(await res.text())
    }
  } catch {
    ElMessage.error('加载失败')
  }
}

const loadAll = async () => {
  if (!currentUser.value) return
  try {
    const res = await fetch(`http://localhost:8080/api/adoption/all?username=${currentUser.value.username}`)
    if (res.ok) {
      allList.value = await res.json()
    } else {
      ElMessage.error(await res.text())
    }
  } catch {
    ElMessage.error('加载失败')
  }
}

const review = (applicationId, action) => {
  const text = action === 'approve' ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${text}这条申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await fetch(
          `http://localhost:8080/api/adoption/review/${applicationId}?username=${currentUser.value.username}&action=${action}`,
          { method: 'POST' }
      )
      if (res.ok) {
        ElMessage.success(`${text}成功`)
        loadPending()
        loadAll()
      } else {
        ElMessage.error(await res.text())
      }
    } catch {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  currentUser.value = getCurrentUser()
  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    ElMessage.error('无权限访问')
    return
  }
  loadPending()
  loadAll()
})

watch(activeTab, (newTab) => {
  if (newTab === 'pending') loadPending()
  else loadAll()
})
</script>
