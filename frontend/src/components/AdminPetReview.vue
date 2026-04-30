<template>
  <div>

    <!-- Tab 切换：待审核/已通过/已拒绝 -->
    <el-tabs v-model="activeTab">
      <!-- 待审核列表 -->
      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pendingList" style="width: 100%" v-loading="pendingLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物图片" width="120">
            <template #default="{ row }">
              <img
                  v-if="getPetImageUrl(row)"
                  :src="getPetImageUrl(row)"
                  style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px"
              />
              <div v-else style="width: 80px; height: 80px; background: #f5f5f5; display: flex; align-items: center; justify-content: center; border-radius: 8px">
                🐾
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="type" label="种类" width="100" />
          <el-table-column prop="gender" label="性别" width="80" />
          <el-table-column prop="age" label="年龄" width="80">
            <template #default="{ row }">{{ row.age }}岁</template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="发布者" width="150">
            <template #default="{ row }">
              {{ row.rescuer?.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleReview(row, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReview(row, 'reject')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="pendingPage"
              v-model:page-size="pendingPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="pendingTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePendingSizeChange"
              @current-change="handlePendingPageChange"
              background
          />
        </div>
      </el-tab-pane>

      <!-- 已通过列表 -->
      <el-tab-pane label="已通过" name="approved">
        <el-table :data="approvedList" style="width: 100%" v-loading="approvedLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物图片" width="120">
            <template #default="{ row }">
              <img
                  v-if="getPetImageUrl(row)"
                  :src="getPetImageUrl(row)"
                  style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px"
              />
              <div v-else style="width: 80px; height: 80px; background: #f5f5f5; display: flex; align-items: center; justify-content: center; border-radius: 8px">
                🐾
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="type" label="种类" width="100" />
          <el-table-column prop="gender" label="性别" width="80" />
          <el-table-column prop="age" label="年龄" width="80">
            <template #default="{ row }">{{ row.age }}岁</template>
          </el-table-column>
          <el-table-column label="发布者" width="150">
            <template #default="{ row }">
              {{ row.rescuer?.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="approvedPage"
              v-model:page-size="approvedPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="approvedTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleApprovedSizeChange"
              @current-change="handleApprovedPageChange"
              background
          />
        </div>
      </el-tab-pane>

      <!-- 已拒绝列表 -->
      <el-tab-pane label="已拒绝" name="rejected">
        <el-table :data="rejectedList" style="width: 100%" v-loading="rejectedLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物图片" width="120">
            <template #default="{ row }">
              <img
                  v-if="getPetImageUrl(row)"
                  :src="getPetImageUrl(row)"
                  style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px"
              />
              <div v-else style="width: 80px; height: 80px; background: #f5f5f5; display: flex; align-items: center; justify-content: center; border-radius: 8px">
                🐾
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="type" label="种类" width="100" />
          <el-table-column prop="gender" label="性别" width="80" />
          <el-table-column prop="age" label="年龄" width="80">
            <template #default="{ row }">{{ row.age }}岁</template>
          </el-table-column>
          <el-table-column label="发布者" width="150">
            <template #default="{ row }">
              {{ row.rescuer?.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="rejectedPage"
              v-model:page-size="rejectedPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="rejectedTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRejectedSizeChange"
              @current-change="handleRejectedPageChange"
              background
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import {ref, onMounted, watch} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../utils/request.js'

const activeTab = ref('pending')
const currentUser = ref(null)

// 待审核数据
const pendingList = ref([])
const pendingLoading = ref(false)
const pendingPage = ref(1)
const pendingPageSize = ref(10)
const pendingTotal = ref(0)

// 已通过数据
const approvedList = ref([])
const approvedLoading = ref(false)
const approvedPage = ref(1)
const approvedPageSize = ref(10)
const approvedTotal = ref(0)

// 已拒绝数据
const rejectedList = ref([])
const rejectedLoading = ref(false)
const rejectedPage = ref(1)
const rejectedPageSize = ref(10)
const rejectedTotal = ref(0)

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getPetImageUrl = (pet) => {
  if (pet.photoUrl) {
    return pet.photoUrl.startsWith('http') ? pet.photoUrl : 'http://localhost:8080' + pet.photoUrl
  }
  if (pet.photoUrls) {
    try {
      const photos = JSON.parse(pet.photoUrls)
      if (photos && photos.length > 0) {
        const url = photos[0]
        return url.startsWith('http') ? url : 'http://localhost:8080' + url
      }
    } catch {
      return null
    }
  }
  return null
}

const loadPending = async () => {
  pendingLoading.value = true
  try {
    const data = await get('/api/pet/pending', {
      username: currentUser.value.username,
      page: pendingPage.value - 1,
      size: pendingPageSize.value
    })
    pendingList.value = data.content
    pendingTotal.value = data.totalElements
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    pendingLoading.value = false
  }
}

const loadApproved = async () => {
  approvedLoading.value = true
  try {
    const data = await get('/api/pet/list', {
      page: approvedPage.value - 1,
      size: approvedPageSize.value
    })
    approvedList.value = data.content
    approvedTotal.value = data.totalElements
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    approvedLoading.value = false
  }
}

// 已拒绝列表
const loadRejected = async () => {
  rejectedLoading.value = true
  try {
    const data = await get('/api/pet/rejected', {
      username: currentUser.value.username,
      page: rejectedPage.value - 1,
      size: rejectedPageSize.value
    })
    rejectedList.value = data.content
    rejectedTotal.value = data.totalElements
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    rejectedLoading.value = false
  }
}


const handleReview = async (pet, action) => {
  const actionText = action === 'approve' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该宠物吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    const user = getCurrentUser()
    const url = `/api/pet/review/${pet.id}?username=${user.username}&action=${action}`
    await post(url, null)
    ElMessage.success(`已${actionText}该宠物`)
    loadPending()
    loadApproved()
    loadRejected()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}



const handlePendingPageChange = (page) => {
  pendingPage.value = page
  loadPending()
}

const handlePendingSizeChange = (size) => {
  pendingPageSize.value = size
  pendingPage.value = 1
  loadPending()
}

const handleApprovedPageChange = (page) => {
  approvedPage.value = page
  loadApproved()
}

const handleApprovedSizeChange = (size) => {
  approvedPageSize.value = size
  approvedPage.value = 1
  loadApproved()
}

const handleRejectedPageChange = (page) => {
  rejectedPage.value = page
  loadRejected()
}

const handleRejectedSizeChange = (size) => {
  rejectedPageSize.value = size
  rejectedPage.value = 1
  loadRejected()
}

onMounted(() => {
  currentUser.value = getCurrentUser()
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    return
  }
  loadPending()
})
watch(activeTab, (newTab) => {
  if (newTab === 'pending') {
    loadPending()
  } else if (newTab === 'approved') {
    loadApproved()
  } else if (newTab === 'rejected') {
    loadRejected()
  }
})
</script>

<style scoped>
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
