<template>
  <div>
    <!-- 领养申请管理主页面 -->
    <el-page-header title="领养申请管理" content="审核领养者的申请" />

    <el-tabs v-model="activeTab">
      <!-- 待审核申请列表 -->
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
          <el-table-column label="居住类型" width="100">
            <template #default="{ row }">{{ row.residenceType || '-' }}</template>
          </el-table-column>
          <el-table-column label="住房面积" width="100">
            <template #default="{ row }">{{ row.housingArea ? row.housingArea + '㎡' : '-' }}</template>
          </el-table-column>
          <el-table-column label="养宠经验" width="120">
            <template #default="{ row }">{{ row.petExperience || '-' }}</template>
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

        <!-- 待审核列表分页组件 -->
        <div style="margin-top: 20px; display: flex; justify-content: center;">
          <el-pagination
              :pager-count="7"
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

      <!-- 所有申请列表 -->
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
          <el-table-column label="回访" width="240">
            <template #default="{ row }">
              <!-- 仅已通过申请提供回访功能 -->
              <div v-if="row.status === 'APPROVED'" style="display: flex; gap: 8px">
                <el-button
                    type="primary"
                    size="small"
                    @click="showVisitDialog(row)"
                    style="background: #E07A5F; border-color: #E07A5F"
                >
                  📝 添加回访
                </el-button>
                <el-button
                    type="info"
                    size="small"
                    @click="viewVisitHistory(row)"
                    style="background: linear-gradient(135deg, #81B29A 0%, #F2CC8F 100%); border: none; color: white"
                >
                  📋 回访历史
                </el-button>
              </div>
              <span v-else style="color: #999">-</span>
            </template>
          </el-table-column>

        </el-table>

        <!-- 所有申请列表分页组件 -->
        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="allPage"
              v-model:page-size="allPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="allTotal"
              layout="total, prev, pager, next, sizes, jumper"
              :pager-count="7"
              @size-change="handleAllSizeChange"
              @current-change="handleAllPageChange"
              background
              class="custom-pagination"
          />
        </div>

      </el-tab-pane>
    </el-tabs>

    <!-- 添加回访记录弹窗 -->
    <el-dialog v-model="visitDialogVisible" :title="`添加回访记录 - ${currentApplication?.pet?.name || ''}`" width="650px">
      <el-form :model="visitForm" label-width="120px" class="visit-form">
        <el-form-item label="回访方式" required>
          <el-radio-group v-model="visitForm.visitType">
            <el-radio label="PHONE">📞 电话回访</el-radio>
            <el-radio label="ON_SITE">🏠 上门回访</el-radio>
            <el-radio label="ONLINE">💬 在线回访</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="回访时间" required>
          <el-date-picker
              v-model="visitForm.visitTime"
              type="datetime"
              placeholder="选择回访时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="宠物状态" required>
          <el-radio-group v-model="visitForm.petStatus">
            <el-radio label="HEALTHY">✅ 适应良好</el-radio>
            <el-radio label="ADAPTING">⚠️ 适应中</el-radio>
            <el-radio label="ISSUES">❌ 存在问题</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="回访内容">
          <el-input
              v-model="visitForm.content"
              type="textarea"
              rows="4"
              placeholder="记录回访的详细情况..."
          />
        </el-form-item>

        <el-form-item label="领养者反馈">
          <el-input
              v-model="visitForm.feedback"
              type="textarea"
              rows="3"
              placeholder="领养者的意见或建议..."
          />
        </el-form-item>

        <el-form-item label="后续回访">
          <el-switch v-model="visitForm.needFollowUp" />
          <span style="margin-left: 10px; color: #666">{{ visitForm.needFollowUp ? '需要后续回访' : '无需后续回访' }}</span>
        </el-form-item>

        <el-form-item v-if="visitForm.needFollowUp" label="下次回访时间">
          <el-date-picker
              v-model="visitForm.nextVisitTime"
              type="date"
              placeholder="选择下次回访日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="visitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitVisitRecord" :loading="submitting" style="background: #E07A5F; border-color: #E07A5F">
          提交回访记录
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看回访历史弹窗 -->
    <el-dialog v-model="historyDialogVisible" title="回访历史" width="700px">
      <div v-if="visitHistory.length === 0" style="text-align: center; padding: 40px; color: #999">
        暂无回访记录
      </div>
      <el-timeline v-else>
        <el-timeline-item
            v-for="record in visitHistory"
            :key="record.id"
            :timestamp="formatTime(record.visitTime)"
            placement="top"
        >
          <el-card shadow="hover" class="visit-record-card">
            <div class="visit-record-header">
              <el-tag :type="getVisitTypeTag(record.visitType)" size="small">
                {{ getVisitTypeText(record.visitType) }}
              </el-tag>
              <el-tag :type="getPetStatusTag(record.petStatus)" size="small" style="margin-left: 10px">
                {{ getPetStatusText(record.petStatus) }}
              </el-tag>
              <span style="margin-left: auto; color: #999; font-size: 12px">
                回访人：{{ record.visitor?.username || '管理员' }}
              </span>
            </div>
            <div v-if="record.content" style="margin-top: 12px">
              <div style="color: #666; font-size: 13px; margin-bottom: 5px">📝 回访内容：</div>
              <div style="color: #333; line-height: 1.6">{{ record.content }}</div>
            </div>
            <div v-if="record.feedback" style="margin-top: 12px">
              <div style="color: #666; font-size: 13px; margin-bottom: 5px">💬 领养者反馈：</div>
              <div style="color: #333; line-height: 1.6">{{ record.feedback }}</div>
            </div>
            <div v-if="record.needFollowUp && record.nextVisitTime" style="margin-top: 12px; padding-top: 12px; border-top: 1px dashed #ddd">
              <span style="color: #E07A5F; font-size: 13px">
                📅 下次回访：{{ formatDate(record.nextVisitTime) }}
              </span>
            </div>
            <div style="margin-top: 12px; text-align: right">
              <el-button type="danger" size="small" text @click="deleteVisitRecord(record.id)">删除</el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 领养申请管理组件
 * 管理员专用页面，用于审核领养申请、添加回访记录、查看回访历史
 */
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/** 当前激活的标签页：pending(待审核) 或 all(所有申请) */
const activeTab = ref('pending')
/** 待审核申请列表数据 */
const pendingList = ref([])
/** 所有申请列表数据 */
const allList = ref([])
/** 当前登录的管理员用户信息 */
const currentUser = ref(null)

/** 待审核列表加载状态 */
const pendingLoading = ref(false)
/** 所有申请列表加载状态 */
const allLoading = ref(false)
/** 待审核列表当前页码 */
const pendingPage = ref(1)
/** 待审核列表每页条数 */
const pendingPageSize = ref(10)
/** 待审核列表总条数 */
const pendingTotal = ref(0)
/** 所有申请列表当前页码 */
const allPage = ref(1)
/** 所有申请列表每页条数 */
const allPageSize = ref(10)
/** 所有申请列表总条数 */
const allTotal = ref(0)

/** 回访记录弹窗显示状态 */
const visitDialogVisible = ref(false)
/** 回访历史弹窗显示状态 */
const historyDialogVisible = ref(false)
/** 当前操作的申请记录 */
const currentApplication = ref(null)
/** 回访历史数据列表 */
const visitHistory = ref([])
/** 提交回访记录加载状态 */
const submitting = ref(false)

/** 回访记录表单数据 */
const visitForm = ref({
  visitType: 'PHONE',
  visitTime: '',
  petStatus: 'HEALTHY',
  content: '',
  feedback: '',
  needFollowUp: false,
  nextVisitTime: ''
})

/**
 * 获取当前登录用户信息
 * @returns {Object|null} 用户信息对象，未登录返回 null
 */
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

/**
 * 格式化时间为本地字符串格式
 * @param {string|Date} time - 需要格式化的时间
 * @returns {string} 格式化后的时间字符串，如 "2024/1/1 12:00:00"
 */
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

/**
 * 格式化时间为日期格式
 * @param {string|Date} time - 需要格式化的时间
 * @returns {string} 格式化后的日期字符串，如 "2024/1/1"
 */
const formatDate = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN')
}

/**
 * 将申请状态码转换为中文显示
 * @param {string} status - 申请状态码：PENDING/APPROVED/REJECTED
 * @returns {string} 中文状态文本
 */
const statusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

/**
 * 将回访方式转换为中文显示
 * @param {string} type - 回访方式：PHONE/ON_SITE/ONLINE
 * @returns {string} 中文回访方式文本
 */
const getVisitTypeText = (type) => {
  const map = {
    'PHONE': '📞 电话回访',
    'ON_SITE': '🏠 上门回访',
    'ONLINE': '💬 在线回访'
  }
  return map[type] || type
}

/**
 * 获取回访方式对应的标签颜色类型
 * @param {string} type - 回访方式
 * @returns {string} Element Plus Tag 组件的颜色类型
 */
const getVisitTypeTag = (type) => {
  const map = {
    'PHONE': 'primary',
    'ON_SITE': 'success',
    'ONLINE': 'info'
  }
  return map[type] || ''
}

/**
 * 将宠物状态转换为中文显示
 * @param {string} status - 宠物状态：HEALTHY/ADAPTING/ISSUES
 * @returns {string} 中文状态文本
 */
const getPetStatusText = (status) => {
  const map = {
    'HEALTHY': '✅ 适应良好',
    'ADAPTING': '⚠️ 适应中',
    'ISSUES': '❌ 存在问题'
  }
  return map[status] || status
}

/**
 * 获取宠物状态对应的标签颜色类型
 * @param {string} status - 宠物状态
 * @returns {string} Element Plus Tag 组件的颜色类型
 */
const getPetStatusTag = (status) => {
  const map = {
    'HEALTHY': 'success',
    'ADAPTING': 'warning',
    'ISSUES': 'danger'
  }
  return map[status] || ''
}

/**
 * 加载待审核申请列表数据
 */
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

/**
 * 加载所有申请列表数据
 */
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

/**
 * 待审核列表页码变化处理
 * @param {number} page - 新的页码
 */
const handlePendingPageChange = (page) => {
  pendingPage.value = page
  loadPending()
}

/**
 * 待审核列表每页条数变化处理
 * @param {number} size - 新的每页条数
 */
const handlePendingSizeChange = (size) => {
  pendingPageSize.value = size
  pendingPage.value = 1
  loadPending()
}

/**
 * 所有申请列表页码变化处理
 * @param {number} page - 新的页码
 */
const handleAllPageChange = (page) => {
  allPage.value = page
  loadAll()
}

/**
 * 所有申请列表每页条数变化处理
 * @param {number} size - 新的每页条数
 */
const handleAllSizeChange = (size) => {
  allPageSize.value = size
  allPage.value = 1
  loadAll()
}

/**
 * 审核申请（通过/拒绝）
 * @param {number} applicationId - 申请记录 ID
 * @param {string} action - 操作类型：approve(通过) / reject(拒绝)
 */
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

/**
 * 打开添加回访记录弹窗
 * @param {Object} application - 当前审核通过的申请记录
 */
const showVisitDialog = async (application) => {
  currentApplication.value = application
  visitForm.value = {
    visitType: 'PHONE',
    visitTime: new Date().toISOString().slice(0, 16),
    petStatus: 'HEALTHY',
    content: '',
    feedback: '',
    needFollowUp: false,
    nextVisitTime: ''
  }

  try {
    const res = await fetch(`http://localhost:8080/api/visit/list/${application.id}`)
    if (res.ok) {
      visitHistory.value = await res.json()
      if (visitHistory.value.length > 0) {
        const latest = visitHistory.value[0]
        if (latest.needFollowUp && latest.nextVisitTime) {
          visitForm.value.nextVisitTime = latest.nextVisitTime
          visitForm.value.needFollowUp = true
        }
      }
    }
  } catch (error) {
    console.error(error)
  }

  visitDialogVisible.value = true
}

/**
 * 提交回访记录
 */
const submitVisitRecord = async () => {
  if (!visitForm.value.visitType || !visitForm.value.visitTime || !visitForm.value.petStatus) {
    ElMessage.warning('请填写必填项')
    return
  }

  submitting.value = true
  try {
    const res = await fetch('http://localhost:8080/api/visit/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: currentUser.value.username,
        applicationId: currentApplication.value.id,
        ...visitForm.value
      })
    })

    if (res.ok) {
      ElMessage.success('回访记录添加成功')
      visitDialogVisible.value = false
      loadAll()
    } else {
      ElMessage.error(await res.text())
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

/**
 * 查看回访历史记录
 * @param {Object} application - 当前审核通过的申请记录
 */
const viewVisitHistory = async (application) => {
  currentApplication.value = application
  try {
    const res = await fetch(`http://localhost:8080/api/visit/list/${application.id}`)
    if (res.ok) {
      visitHistory.value = await res.json()
      historyDialogVisible.value = true
    } else {
      ElMessage.error(await res.text())
    }
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

/**
 * 删除回访记录
 * @param {number} recordId - 回访记录 ID
 */
const deleteVisitRecord = async (recordId) => {
  ElMessageBox.confirm('确定要删除这条回访记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/visit/${recordId}?username=${currentUser.value.username}`, {
        method: 'DELETE'
      })
      if (res.ok) {
        ElMessage.success('删除成功')
        visitHistory.value = visitHistory.value.filter(r => r.id !== recordId)
        loadAll()
      } else {
        ElMessage.error(await res.text())
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

/**
 * 组件挂载时初始化：获取用户信息并加载列表数据
 */
onMounted(() => {
  currentUser.value = getCurrentUser()

  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    ElMessage.error('无权限访问，请使用管理员账号登录')
    return
  }

  loadPending()
  loadAll()
})

/**
 * 监听标签页切换，自动刷新对应列表数据
 */
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

.visit-form {
  margin-top: 20px;
}

.visit-record-card {
  border-radius: 12px;
  border: 1px solid #E5E7EB;
}

.visit-record-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
