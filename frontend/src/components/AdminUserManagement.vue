<template>
  <div class="user-management">
    <el-card class="header-card">
      <div class="header-content">
        <h2>用户管理</h2>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或真实姓名"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </el-card>

    <el-card class="table-card" style="margin-top: 20px">
      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteUser(row)"
              :disabled="row.id === currentUser?.id"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
          class="custom-pagination"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { get, del } from '../utils/request.js'
import { success, error, warning } from '../utils/message.js'

const userList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
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

const getRoleType = (role) => {
  return role === 'ADMIN' ? 'danger' : 'info'
}

const getRoleText = (role) => {
  return role === 'ADMIN' ? '管理员' : '普通用户'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const allUsers = await get('/api/user/list', {
      page: currentPage.value - 1,
      size: pageSize.value
    })
    userList.value = allUsers.content || []
    total.value = allUsers.totalElements || 0
  } catch (err) {
    error(err.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadUsers()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

const handleDeleteUser = (user) => {
  if (user.id === currentUser.value?.id) {
    warning('不能删除自己的账号')
    return
  }

  ElMessageBox.confirm(
    `确定要删除用户 "${user.username}" 吗？此操作将永久删除该用户及其所有相关数据（包括发布的宠物、申请记录、收藏等），且不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
      distinguishCancelAndClose: true
    }
  ).then(async () => {
    try {
      await del(`/api/user/delete/${user.id}`, {
        adminUsername: currentUser.value.username
      })
      success('用户删除成功')
      loadUsers()
    } catch (err) {
      error(err.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  currentUser.value = getCurrentUser()
  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    error('无权限访问，请使用管理员账号登录')
    return
  }
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header-card {
  border-radius: 12px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #1f2937;
  font-size: 20px;
}

.table-card {
  border-radius: 12px;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.custom-pagination :deep(.el-pager li.active) {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  color: white;
  border: none;
}

.custom-pagination :deep(.el-pager li:hover:not(.active)) {
  background-color: #FDF8F3;
  color: #E07A5F;
}
</style>
