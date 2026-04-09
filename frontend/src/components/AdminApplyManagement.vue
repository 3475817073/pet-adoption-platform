<template>
  <div>
    <el-page-header title="领养申请管理" content="审核领养者的申请" />

    <el-tabs v-model="activeTab">
      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pendingList" style="width: 100%" v-loading="pendingLoading">
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
        <!--分页组件-->
        <div style="margin-top: 20px; display: flex; justify-content: center;">
          <el-pagination
              :pager-count="3"
              v-model:current-page="pendingPage"
              v-model:page-size="pendingPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="pendingTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePendingSizeChange"
              @current-change="handlePendingPageChange"
              background
          >

          <template #total="{ total }">
              <span>总计 {{ total }} 条</span>
            </template>
            <template #jumper>
              <span style="margin-left: 8px;">跳转至</span>
              <el-input
                  type="number"
                  :min="1"
                  :max="Math.ceil(pendingTotal / pendingPageSize)"
                  v-model.number="jumpPendingPage"
                  @keyup.enter="handleJumpPendingPage"
                  style="width: 50px; margin-left: 8px;"
              />
            </template>
          </el-pagination>
        </div>

      </el-tab-pane>

      <el-tab-pane label="所有申请" name="all">
        <el-table :data="allList" style="width: 100%" v-loading="allLoading">
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
         <!-- 分页组件-->
        <div style="margin-top: 20px; display: flex; justify-content: center;">
          <el-pagination
              :pager-count="3"
              v-model:current-page="pendingPage"
              v-model:page-size="pendingPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="pendingTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePendingSizeChange"
              @current-change="handlePendingPageChange"
              background
          >

          <template #total="{ total }">
              <span>总计 {{ total }} 条</span>
            </template>
            <template #jumper>
              <span style="margin-left: 8px;">跳转至</span>
              <el-input
                  type="number"
                  :min="1"
                  :max="Math.ceil(allTotal / allPageSize)"
                  v-model.number="jumpAllPage"
                  @keyup.enter="handleJumpAllPage"
                  style="width: 50px; margin-left: 8px;"
              />
            </template>
          </el-pagination>
        </div>

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

const pendingLoading = ref(false)
const allLoading = ref(false)
const pendingPage = ref(1)
const pendingPageSize = ref(10)
const pendingTotal = ref(0)
const allPage = ref(1)
const allPageSize = ref(10)
const allTotal = ref(0)

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
  pendingLoading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/adoption/pending?username=${currentUser.value.username}&page=${pendingPage.value - 1}&size=${pendingPageSize.value}`)
    if (res.ok) {
      const data = await res.json()
      pendingList.value = data.content
      pendingTotal.value = data.totalElements
    } else {
      const errorText = await res.text()
      ElMessage.error('加载失败：' + errorText)
    }
  } catch (error) {
    ElMessage.error('网络错误：' + error.message)
  } finally {
    pendingLoading.value = false
  }
}

const loadAll = async () => {
  if (!currentUser.value) return
  allLoading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/adoption/all?username=${currentUser.value.username}&page=${allPage.value - 1}&size=${allPageSize.value}`)
    if (res.ok) {
      const data = await res.json()
      allList.value = data.content
      allTotal.value = data.totalElements
    } else {
      const errorText = await res.text()
      ElMessage.error('加载失败：' + errorText)
    }
  } catch (error) {
    ElMessage.error('网络错误：' + error.message)
  } finally {
    allLoading.value = false
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

const handleAllPageChange = (page) => {
  allPage.value = page
  loadAll()
}

const handleAllSizeChange = (size) => {
  allPageSize.value = size
  allPage.value = 1
  loadAll()
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
    ElMessage.error('无权限访问，请使用管理员账号登录')
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

<style scoped>
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.custom-pagination :deep(.el-pagination__total) {
  font-size: 14px;
  color: #6B7280;
  margin-right: 20px;
}

.custom-pagination :deep(.el-pagination__sizes) {
  margin: 0 15px;
}

.custom-pagination :deep(.el-pagination__jump) {
  margin-left: 20px;
}

.custom-pagination :deep(.el-pager li) {
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  border-radius: 8px;
  margin: 0 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.custom-pagination :deep(.el-pager li.active) {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  color: white;
  border: none;
  font-weight: 600;
}

.custom-pagination :deep(.el-pager li:hover:not(.active)) {
  background-color: #FDF8F3;
  color: #E07A5F;
}

.custom-pagination :deep(.btn-prev),
.custom-pagination :deep(.btn-next) {
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  border-radius: 8px;
  padding: 0;
  border: 1px solid #E5E7EB;
  background: white;
  transition: all 0.3s;
}

.custom-pagination :deep(.btn-prev:hover),
.custom-pagination :deep(.btn-next:hover) {
  color: #E07A5F;
  border-color: #E07A5F;
}

.custom-pagination :deep(.el-select) {
  --el-select-border-radius-hover: 8px;
}

.custom-pagination :deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>

