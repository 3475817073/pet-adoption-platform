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
              <div class="pet-thumb" :style="{ backgroundImage: getPetImageUrl(row) ? `url(${getPetImageUrl(row)})` : '' }">
                <span v-if="!getPetImageUrl(row)" class="pet-thumb-placeholder"></span>
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
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
              <el-button type="success" size="small" @click="handleReview(row, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
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
              <div class="pet-thumb" :style="{ backgroundImage: getPetImageUrl(row) ? `url(${getPetImageUrl(row)})` : '' }">
                <span v-if="!getPetImageUrl(row)" class="pet-thumb-placeholder"></span>
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
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
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
              <div class="pet-thumb" :style="{ backgroundImage: getPetImageUrl(row) ? `url(${getPetImageUrl(row)})` : '' }">
                <span v-if="!getPetImageUrl(row)" class="pet-thumb-placeholder"></span>
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
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
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

    <!-- 宠物详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="宠物详细信息" width="800px">
      <div v-if="currentPet" class="pet-detail">
        <el-row :gutter="20">
          <el-col :span="10">
            <div class="pet-image-container">
              <div v-if="getPetImageUrl(currentPet)" class="pet-detail-image" :style="{ backgroundImage: `url(${getPetImageUrl(currentPet)})` }"></div>
              <div v-else class="pet-detail-no-image"></div>
            </div>
          </el-col>
          <el-col :span="14">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="宠物名称">{{ currentPet.name }}</el-descriptions-item>
              <el-descriptions-item label="种类">{{ currentPet.type }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ currentPet.gender || '未知' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ currentPet.age }}岁</el-descriptions-item>
              <el-descriptions-item label="健康状态">
                <el-tag v-if="currentPet.vaccinated" type="success" size="small">已疫苗</el-tag>
                <el-tag v-else type="info" size="small">未疫苗</el-tag>
                <el-tag v-if="currentPet.neutered" type="success" size="small" style="margin-left: 8px">已绝育</el-tag>
                <el-tag v-else type="info" size="small" style="margin-left: 8px">未绝育</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="发布者">{{ currentPet.rescuer?.username }}</el-descriptions-item>
              <el-descriptions-item label="发布时间">{{ formatTime(currentPet.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="详细描述">{{ currentPet.description }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝宠物发布" width="500px">
      <el-form label-width="100px">
        <el-form-item label="宠物名称">
          <span>{{ currentPet?.name }}</span>
        </el-form-item>
        <el-form-item label="拒绝理由" required>
          <el-input
              v-model="rejectReason"
              type="textarea"
              :rows="4"
              placeholder="请填写拒绝理由，该理由将展示给用户..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :disabled="!rejectReason.trim()">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, watch} from 'vue'
import { ElMessageBox } from 'element-plus'
import { get, post } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'

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

// 详情对话框
const detailDialogVisible = ref(false)
const currentPet = ref(null)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectingPet = ref(null)

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
  } catch (err) {
    error(err.message || '加载失败')
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
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    approvedLoading.value = false
  }
}

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
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    rejectedLoading.value = false
  }
}

const viewDetail = (pet) => {
  currentPet.value = pet
  detailDialogVisible.value = true
}

const handleReview = async (pet, action) => {
  try {
    await ElMessageBox.confirm(`确定要通过该宠物吗？`, '确认操作', {
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
    success('已通过该宠物')
    loadPending()
    loadApproved()
    loadRejected()
  } catch (err) {
    error(err.message || '操作失败')
  }
}

const handleReject = (pet) => {
  rejectingPet.value = pet
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    warning('请填写拒绝理由')
    return
  }
  try {
    const user = getCurrentUser()
    const url = `/api/pet/review/${rejectingPet.value.id}?username=${user.username}&action=reject&reason=${encodeURIComponent(rejectReason.value)}`
    await post(url, null)
    success('已拒绝该宠物')
    rejectDialogVisible.value = false
    loadPending()
    loadApproved()
    loadRejected()
  } catch (err) {
    error(err.message || '操作失败')
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
    warning('请先登录')
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

.pet-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.pet-thumb-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/pets-banner.png') center/cover no-repeat;
  background-color: #f3f4f6;
}

.pet-detail {
  padding: 10px;
}

.pet-image-container {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.pet-detail-image {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.pet-detail-no-image {
  width: 100%;
  height: 100%;
  background: url('@/assets/pets-banner.png') center/cover no-repeat;
  background-color: #f3f4f6;
}
</style>
