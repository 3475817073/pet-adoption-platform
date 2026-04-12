<template>
  <div>
    <el-page-header title="个人中心" content="查看我的发布和申请记录" />

    <!-- 用户信息卡片：展示用户名、角色标签及统计数据 -->
    <el-card style="max-width: 1200px; margin: 20px auto; border-radius: 16px">
      <div class="user-profile">
        <div class="avatar-section">
          <div class="avatar-circle">{{ user?.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
          <h2>{{ user?.username || '未登录' }}</h2>
          <el-tag :type="getRoleColor(user?.role)" size="large">{{ getRoleText(user?.role) }}</el-tag>
        </div>
        <div class="stats-section">
          <div class="stat-item">
            <div class="stat-number">{{ publishedPets.length }}</div>
            <div class="stat-label">我的发布</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ myApplications.length }}</div>
            <div class="stat-label">领养申请</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Tab 切换：我的发布与我的申请 -->
    <el-tabs v-model="activeTab" type="border-card" style="max-width: 1200px; margin: 20px auto">

      <!-- 我的发布：展示用户已发布的宠物卡片列表 -->
      <el-tab-pane label="🐾 我的发布" name="pets">
        <el-row :gutter="20" v-if="publishedPets.length > 0">
          <el-col v-for="pet in publishedPets" :key="pet.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 20px">
            <el-card shadow="hover" class="pet-card">
              <img v-if="getPetImageUrl(pet)" :src="getPetImageUrl(pet)" class="pet-image" />
              <div v-else class="no-image">🐾</div>
              <div class="pet-info">
                <h4>{{ pet.name }}</h4>
                <p class="description">{{ pet.description }}</p>
              </div>
              <div class="status-actions">
                <el-tag :type="pet.status === 'ADOPTED' ? 'danger' : 'success'" size="small">
                  {{ pet.status === 'ADOPTED' ? '已被领养' : '待领养' }}
                </el-tag>
                <div class="action-buttons" v-if="pet.status === 'AVAILABLE'">
                  <el-button type="primary" size="small" @click.stop="editPet(pet)">编辑</el-button>
                  <el-button type="danger" size="small" @click.stop="deletePet(pet.id)">删除</el-button>
                </div>
              </div>


            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="还没有发布过宠物哦~" :image-size="150" />

        <!-- 我的发布分页 -->
        <div class="pagination-wrapper" v-if="publishedPets.length > 0">
          <el-pagination
              v-model:current-page="petsCurrentPage"
              v-model:page-size="petsPageSize"
              :page-sizes="[4, 8, 16, 32]"
              :total="petsTotal"
              layout="total, prev, pager, next, sizes, jumper"
              :pager-count="7"
              @size-change="handlePetsSizeChange"
              @current-change="handlePetsPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

      <!-- 我的申请：展示用户提交的领养申请记录 -->
      <el-tab-pane label="💕 我的申请" name="applications">
        <el-timeline v-if="myApplications.length > 0">
          <el-timeline-item
              v-for="app in myApplications"
              :key="app.id"
              :timestamp="formatDate(app.applyTime)"
              placement="top">
            <el-card shadow="hover">
              <div class="application-item">
                <div class="app-pet-info">
                  <h4>{{ app.pet?.name || '未知宠物' }}</h4>
                  <el-tag :type="getStatusColor(app.status)" size="small">
                    {{ getStatusText(app.status) }}
                  </el-tag>
                </div>
                <p><strong>申请理由：</strong>{{ app.reason }}</p>
                <p><strong>联系方式：</strong>{{ app.contact }}</p>
                <p v-if="app.reviewTime"><strong>审核时间：</strong>{{ formatDate(app.reviewTime) }}</p>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="还没有提交过领养申请哦~" :image-size="150" />

        <!-- 我的申请分页 -->
        <div class="pagination-wrapper" v-if="myApplications.length > 0">
          <el-pagination
              v-model:current-page="appsCurrentPage"
              v-model:page-size="appsPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="appsTotal"
              layout="total, prev, pager, next, sizes, jumper"
              :pager-count="7"
              @size-change="handleAppsSizeChange"
              @current-change="handleAppsPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

    </el-tabs>

    <!-- 编辑宠物弹窗：提供宠物信息编辑表单 -->
    <el-dialog v-model="editDialogVisible" :title="`编辑宠物 - ${editingPet?.name || ''}`" width="700px">
      <div v-if="editingPet" class="edit-container">
        <!-- 左侧：宠物照片预览 -->
        <div class="edit-image-section">
          <img v-if="getPetImageUrl(editingPet)" :src="getPetImageUrl(editingPet)" class="edit-main-image" />
          <div v-else class="edit-no-image">🐾</div>
        </div>

        <!-- 右侧：编辑表单 -->
        <div class="edit-form-section">
          <el-form :model="editForm" label-width="100px" class="edit-form">
            <el-form-item label="名字">
              <el-input v-model="editForm.name" placeholder="宠物名字" class="edit-input" />
            </el-form-item>

            <el-form-item label="种类">
              <el-input v-model="editForm.type" placeholder="猫 / 狗 / 其他" class="edit-input" />
            </el-form-item>

            <el-form-item label="性别">
              <el-select v-model="editForm.gender" placeholder="请选择性别" class="edit-input">
                <el-option label="♂ 公" value="公" />
                <el-option label="♀ 母" value="母" />
              </el-select>
            </el-form-item>

            <el-form-item label="年龄">
              <el-input-number v-model="editForm.age" :min="0" :max="30" class="edit-input-number" />
              <span class="age-unit">岁</span>
            </el-form-item>

            <el-form-item label="描述">
              <el-input
                  type="textarea"
                  v-model="editForm.description"
                  rows="4"
                  placeholder="描述宠物的性格、健康状况等"
                  class="edit-textarea" />
            </el-form-item>

            <div class="edit-actions">
              <el-button @click="editDialogVisible = false" class="cancel-btn">取消</el-button>
              <el-button type="primary" @click="saveEdit" :loading="saving" class="save-btn">
                {{ saving ? '保存中...' : '保存修改' }}
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 个人中心页面组件
 * 展示用户基本信息、已发布宠物列表、领养申请记录，支持宠物编辑与删除操作
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const user = ref(null)
const activeTab = ref('pets')
const publishedPets = ref([])
const myApplications = ref([])
const editDialogVisible = ref(false)
const editingPet = ref(null)
const editForm = ref({
  name: '',
  type: '',
  gender: '',
  age: 1,
  description: ''
})
const saving = ref(false)

/** 宠物列表当前页码 */
const petsCurrentPage = ref(1)
/** 宠物列表每页条数 */
const petsPageSize = ref(8)
/** 宠物列表总条数 */
const petsTotal = ref(0)
/** 申请列表当前页码 */
const appsCurrentPage = ref(1)
/** 申请列表每页条数 */
const appsPageSize = ref(10)
/** 申请列表总条数 */
const appsTotal = ref(0)


/**
 * 获取用户角色对应的标签颜色类型
 * @param {string} role - 用户角色标识
 * @returns {string} Element Plus Tag 组件的颜色类型
 */
const getRoleColor = (role) => {
  const colors = { ADMIN: 'danger', USER: 'info' }
  return colors[role] || ''
}

/**
 * 将用户角色标识转换为中文显示
 * @param {string} role - 用户角色标识
 * @returns {string} 中文角色文本
 */
const getRoleText = (role) => {
  const texts = { ADMIN: '管理员', USER: '普通用户' }
  return texts[role] || '未知'
}


/**
 * 获取申请状态对应的标签颜色类型
 * @param {string} status - 申请状态码
 * @returns {string} Element Plus Tag 组件的颜色类型
 */
const getStatusColor = (status) => {
  const colors = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return colors[status] || ''
}

/**
 * 将申请状态码转换为中文显示
 * @param {string} status - 申请状态码
 * @returns {string} 中文状态文本
 */
const getStatusText = (status) => {
  const texts = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
  return texts[status] || '未知'
}

/**
 * 格式化日期时间为本地系统格式
 * @param {string} dateStr - 原始日期字符串
 * @returns {string} 格式化后的时间字符串
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 获取宠物首张图片 URL（优先使用 photoUrl，其次解析 photoUrls JSON 数组）
 * @param {Object} pet - 宠物对象
 * @returns {string|null} 完整的图片 URL 或 null
 */
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

/**
 * 从后端加载用户数据：包括发布的宠物列表与提交的领养申请记录
 * 支持分页加载，根据当前页码与每页条数请求对应数据
 */
const loadUserData = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    user.value = JSON.parse(userStr)
    const username = user.value.username

    // 加载我的发布（分页）
    const petsRes = await fetch(`http://localhost:8080/api/pet/my-pets?username=${username}&page=${petsCurrentPage.value - 1}&size=${petsPageSize.value}`)
    if (petsRes.ok) {
      const petsData = await petsRes.json()
      publishedPets.value = petsData.content
      petsTotal.value = petsData.totalElements
    }

    // 加载我的申请（分页）
    const appsRes = await fetch(`http://localhost:8080/api/adoption/my-applications?username=${username}&page=${appsCurrentPage.value - 1}&size=${appsPageSize.value}`)
    if (appsRes.ok) {
      const appsData = await appsRes.json()
      myApplications.value = appsData.content
      appsTotal.value = appsData.totalElements
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error(error)
  }
}

/**
 * 宠物列表页码变化处理
 * @param {number} page - 新的页码
 */
const handlePetsPageChange = (page) => {
  petsCurrentPage.value = page
  loadUserData()
}

/**
 * 宠物列表每页条数变化处理
 * @param {number} size - 新的每页条数
 */
const handlePetsSizeChange = (size) => {
  petsPageSize.value = size
  petsCurrentPage.value = 1
  loadUserData()
}

/**
 * 申请列表页码变化处理
 * @param {number} page - 新的页码
 */
const handleAppsPageChange = (page) => {
  appsCurrentPage.value = page
  loadUserData()
}

/**
 * 申请列表每页条数变化处理
 * @param {number} size - 新的每页条数
 */
const handleAppsSizeChange = (size) => {
  appsPageSize.value = size
  appsCurrentPage.value = 1
  loadUserData()
}


/**
 * 打开编辑宠物弹窗，初始化表单数据
 * @param {Object} pet - 待编辑的宠物对象
 */
const editPet = (pet) => {
  editingPet.value = pet
  editForm.value = {
    name: pet.name,
    type: pet.type,
    gender: pet.gender,
    age: pet.age,
    description: pet.description
  }
  editDialogVisible.value = true
}

/**
 * 保存宠物信息修改
 * 校验必填项后向 PUT 接口提交更新数据
 */
const saveEdit = async () => {
  if (!editForm.value.name || !editForm.value.type || !editForm.value.description) {
    ElMessage.warning('请填写完整信息')
    return
  }

  saving.value = true
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    saving.value = false
    return
  }

  const user = JSON.parse(userStr)

  try {
    const res = await fetch(`http://localhost:8080/api/pet/${editingPet.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: user.username,
        name: editForm.value.name,
        type: editForm.value.type,
        gender: editForm.value.gender,
        age: editForm.value.age,
        description: editForm.value.description
      })
    })

    if (res.ok) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      await loadUserData()
    } else {
      ElMessage.error(await res.text())
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}


/**
 * 删除指定宠物信息
 * 弹出确认框，校验权限后向 DELETE 接口发送请求
 * @param {number} petId - 待删除宠物的 ID
 */
const deletePet = (petId) => {
  ElMessageBox.confirm('确定要删除这个宠物信息吗？此操作不可恢复', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      ElMessage.warning('请先登录')
      return
    }

    const user = JSON.parse(userStr)

    try {
      const res = await fetch(`http://localhost:8080/api/pet/${petId}?username=${user.username}`, {
        method: 'DELETE'
      })

      if (res.ok) {
        ElMessage.success('删除成功')
        await loadUserData()
      } else {
        ElMessage.error(await res.text())
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}


/**
 * 组件挂载时执行：加载用户数据
 */
onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.user-profile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF7E5F 0%, #6EE7B7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
  font-weight: bold;
}

.avatar-section h2 {
  margin: 0;
  color: #333;
}

.stats-section {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #FF7E5F;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.pet-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.pet-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(255, 126, 95, 0.2);
}

.pet-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.no-image {
  width: 100%;
  height: 180px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
}

.pet-info {
  padding: 15px;
}

.pet-info h4 {
  margin: 0 0 8px 0;
  color: #3D405B;
  font-size: 16px;
  font-weight: 600;
}


.status-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10px 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
}


.description {
  margin: 10px 0 0 0;
  color: #666;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.application-item h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.application-item p {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

.edit-container {
  display: flex;
  gap: 25px;
}

.edit-image-section {
  flex: 0 0 280px;
}

.edit-main-image {
  width: 100%;
  height: 280px;
  object-fit: cover;
  border-radius: 12px;
}

.edit-no-image {
  width: 100%;
  height: 280px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  border-radius: 12px;
}

.edit-form-section {
  flex: 1;
}

.edit-form {
  margin-top: 10px;
}

.edit-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 14px;
}

.edit-input-number {
  width: 150px;
}

.edit-input-number :deep(.el-input-number .el-input__wrapper) {
  border-radius: 10px;
}

.age-unit {
  margin-left: 10px;
  color: #6B7280;
}

.edit-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 10px 14px;
}

.edit-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #E5E7EB;
}

.cancel-btn {
  border-radius: 10px;
  padding: 10px 24px;
}

.save-btn {
  background: #E07A5F !important;
  border-color: #E07A5F !important;
  border-radius: 10px;
  padding: 10px 24px;
}

.save-btn:hover {
  background: #D06A4F !important;
}

.save-btn:hover {
  background: #D06A4F !important;
}

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
