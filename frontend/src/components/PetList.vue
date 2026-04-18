<template>
  <div>
    <el-page-header title="可领养宠物" content="找到属于你的小伙伴 🐾" />

    <!-- 搜索 + 筛选栏 -->
    <div style="margin: 20px 0; padding: 18px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(61,64,91,0.06)">
      <el-row :gutter="15" align="middle">
        <el-col :span="6">
          <el-input v-model="searchKeyword" placeholder="🔍 搜索宠物名称" clearable size="large"
                    style="--el-input-border-radius: 20px" />
        </el-col>

        <el-col :span="4">
          <el-select v-model="filterType" placeholder="种类" clearable size="large" style="width: 100%; --el-select-border-radius-hover: 20px">
            <el-option label="全部" value="" />
            <el-option label="猫" value="猫" />
            <el-option label="狗" value="狗" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-select v-model="filterGender" placeholder="性别" clearable size="large" style="width: 100%; --el-select-border-radius-hover: 20px">
            <el-option label="全部" value="" />
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-select v-model="filterAge" placeholder="年龄" clearable size="large" style="width: 100%; --el-select-border-radius-hover: 20px">
            <el-option label="全部" value="" />
            <el-option label="1岁以下" value="young" />
            <el-option label="1-3岁" value="adult" />
            <el-option label="3岁以上" value="senior" />
          </el-select>
        </el-col>

        <el-col :span="6">
          <el-button type="primary" @click="resetFilter" size="large"
                     style="background: #E07A5F; border-color: #E07A5F; width: 100%; border-radius: 20px">
            重置筛选
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 宠物卡片：展示宠物图片、基本信息及操作按钮 -->
    <el-row :gutter="20">
      <el-col v-for="pet in pets" :key="pet.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 20px">
        <el-card class="pet-card" shadow="never">
          <div class="image-container" @click="showDetail(pet)">
            <img v-if="getPetImageUrl(pet)" :src="getPetImageUrl(pet)" class="pet-image" />
            <div v-else class="no-image">🐾</div>

            <!-- 状态标签 -->
            <el-tag v-if="pet.status === 'ADOPTED'" class="status-tag adopted">已找到家 🏠</el-tag>
            <el-tag v-else class="status-tag available">等待领养 💕</el-tag>

            <!-- 悬浮遮罩 -->
            <div class="hover-overlay">
              <el-button type="primary" size="large" class="view-btn">查看详情</el-button>
            </div>
          </div>

          <div class="pet-info">
            <h3 class="pet-name" @click="showDetail(pet)">
              {{ pet.name }}
              <span class="pet-detail">（{{ pet.gender }} · {{ pet.age }}岁）</span>
            </h3>
            <el-tag size="small" class="type-tag">{{ pet.type }}</el-tag>
            <p class="description">{{ pet.description }}</p>
            <el-button type="primary" class="adopt-btn" :disabled="pet.status === 'ADOPTED'" @click="applyAdopt(pet)">
              {{ pet.status === 'ADOPTED' ? '已被领养' : '我要领养' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页组件 -->
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[6, 12, 24, 48]"
          :total="total"
          layout="total, prev, pager, next, sizes, jumper"
          :pager-count="7"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="custom-pagination"
      />
    </div>


    <el-empty v-if="pets.length === 0 && !loading" description="暂无符合条件的宠物" :image-size="200" />

    <!-- 宠物详情弹窗：展示多张图片与详细介绍 -->
    <el-dialog v-model="detailVisible" :title="selectedPet?.name" width="700px">
      <div v-if="selectedPet" class="pet-detail-container">
        <div class="detail-images">
          <div v-if="selectedPet.photos && selectedPet.photos.length > 0">
            <img :src="'http://localhost:8080' + selectedPet.photos[0]" class="main-image" />
            <div v-if="selectedPet.photos.length > 1" class="thumbnail-list">
              <img
                  v-for="(photo, index) in selectedPet.photos.slice(1)"
                  :key="index"
                  :src="'http://localhost:8080' + photo"
                  class="thumbnail"
              />
            </div>
          </div>
          <div v-else class="no-image-large">🐾</div>
        </div>

        <div class="detail-info">
          <h2>{{ selectedPet.name }}</h2>
          <div class="detail-tags">
            <el-tag class="type-tag">{{ selectedPet.type }}</el-tag>
            <el-tag class="gender-tag">{{ selectedPet.gender }}</el-tag>
            <el-tag class="age-tag">{{ selectedPet.age }}岁</el-tag>
            <el-tag :class="selectedPet.status === 'ADOPTED' ? 'status-tag adopted' : 'status-tag available'">
              {{ selectedPet.status === 'ADOPTED' ? '已被领养' : '可领养' }}
            </el-tag>
          </div>

          <div class="detail-description">
            <h4>📝 宠物介绍</h4>
            <p class="description-text">{{ selectedPet.description }}</p>
          </div>

          <div class="detail-actions">
            <el-button type="primary" size="large" :disabled="selectedPet.status === 'ADOPTED'" @click="applyFromDetail"
                       style="background: #E07A5F; border-color: #E07A5F; border-radius: 24px; padding: 12px 32px">
              {{ selectedPet.status === 'ADOPTED' ? '已被领养' : '我要领养' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>


    <AdoptionApply
        v-if="applyDialogVisible"
        :pet="selectedPet"
        @close="applyDialogVisible = false"
        @success="onApplySuccess"
    />
  </div>
</template>

<script setup>
/**
 * 宠物列表页面组件
 * 提供宠物浏览、多条件筛选、分页查看、详情预览及领养申请功能
 */
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AdoptionApply from './AdoptionApply.vue'
import { get } from '../utils/request.js'

const emit = defineEmits(['needLogin'])

const pets = ref([])
const searchKeyword = ref('')
const filterType = ref('')
const filterGender = ref('')
const filterAge = ref('')
const applyDialogVisible = ref(false)
const selectedPet = ref(null)
const detailVisible = ref(false)
const loading = ref(false)

/** 当前页码 */
const currentPage = ref(1)
/** 每页显示条数 */
const pageSize = ref(12)
/** 总记录数 */
const total = ref(0)
const jumpPage = ref(1)



/**
 * 从后端加载宠物列表数据，支持分页与多条件筛选
 * 根据当前页码、每页条数以及搜索/筛选条件构建请求参数
 */
const loadPets = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (filterType.value) params.type = filterType.value
    if (filterGender.value) params.gender = filterGender.value
    if (filterAge.value) params.age = filterAge.value
    if (searchKeyword.value) params.name = searchKeyword.value

    const data = await get('/api/pet/list', params)
    pets.value = data.content
    total.value = data.totalElements
  } catch (error) {
    ElMessage.error(error.message || '获取宠物列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 组件挂载时执行：初始化加载宠物列表
 */
onMounted(() => {
  loadPets()
})

/**
 * 监听筛选条件变化，自动重置页码并重新请求数据
 */
watch([filterType, filterGender, filterAge, searchKeyword], () => {
  currentPage.value = 1
  loadPets()
})

/**
 * 处理每页显示条数变化
 */
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadPets()
}

/**
 * 处理页码变化
 */
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  loadPets()
}

/**
 * 处理跳转页码输入，校验范围后执行跳转
 */
const handleJumpPage = () => {
  if (jumpPage.value && jumpPage.value >= 1 && jumpPage.value <= Math.ceil(total.value / pageSize.value)) {
    currentPage.value = jumpPage.value
    loadPets()
  }
}


/**
 * 打开宠物详情弹窗，解析并处理宠物图片数据
 */
const showDetail = (pet) => {

  selectedPet.value = pet
  try {
    if (pet.photoUrls) {
      selectedPet.value.photos = JSON.parse(pet.photoUrls)
    } else if (pet.photoUrl) {
      selectedPet.value.photos = [pet.photoUrl]
    } else {
      selectedPet.value.photos = []
    }
  } catch {
    selectedPet.value.photos = []
  }
  detailVisible.value = true
}

/**
 * 获取宠物首张图片 URL（优先使用 photoUrl，其次解析 photoUrls JSON 数组）
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
 * 从详情页触发领养申请，校验登录状态
 */
const applyFromDetail = () => {
  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录才能申请领养')
    detailVisible.value = false
    emit('needLogin')
    return
  }
  detailVisible.value = false
  applyDialogVisible.value = true
}

/**
 * 从列表页触发领养申请，校验登录状态与宠物领养状态
 */
const applyAdopt = (pet) => {
  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录才能申请领养')
    emit('needLogin')
    return
  }
  if (pet.status === 'ADOPTED') {
    ElMessage.info('这只宠物已经被领养啦~')
    return
  }
  selectedPet.value = pet
  applyDialogVisible.value = true
}

// const onApplySuccess = () => {
//   ElMessage.success('领养申请已提交！请等待管理员审核')
// }

/**
 * 重置所有筛选条件并重新加载第一页数据
 */
const resetFilter = () => {
  searchKeyword.value = ''
  filterType.value = ''
  filterGender.value = ''
  filterAge.value = ''
  currentPage.value = 1
  loadPets()
}
</script>


<style scoped>
.pet-card {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #E5E7EB;
  background: white;
}
.pet-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(61,64,91,0.12);
}
.image-container {
  position: relative;
  overflow: hidden;
  cursor: pointer;
}
.pet-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.pet-card:hover .pet-image {
  transform: scale(1.08);
}
.no-image {
  width: 100%;
  height: 240px;
  background: linear-gradient(135deg, #FDF8F3 0%, #F2CC8F 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
}
.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  font-weight: 500;
  border: none;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
}
.status-tag.available {
  background: #81B29A;
  color: white;
}
.status-tag.adopted {
  background: #E07A5F;
  color: white;
}
.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(61, 64, 91, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}
.pet-card:hover .hover-overlay {
  opacity: 1;
}
.view-btn {
  transform: translateY(20px);
  transition: transform 0.3s ease;
  background: #E07A5F;
  border-color: #E07A5F;
  border-radius: 20px;
}
.pet-card:hover .view-btn {
  transform: translateY(0);
}
.pet-info {
  padding: 16px;
}
.pet-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #3D405B;
  cursor: pointer;
  font-weight: 600;
}
.pet-name:hover {
  color: #E07A5F;
}
.pet-detail {
  color: #E07A5F;
  font-size: 14px;
  font-weight: normal;
}
.type-tag {
  background: #F2CC8F;
  color: #3D405B;
  border-color: #F2CC8F;
  border-radius: 12px;
}
.gender-tag {
  background: #81B29A;
  color: white;
  border-color: #81B29A;
  border-radius: 12px;
}
.age-tag {
  background: #F2CC8F;
  color: #3D405B;
  border-color: #F2CC8F;
  border-radius: 12px;
}
.description {
  margin: 12px 0;
  color: #6B7280;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14px;
}
.adopt-btn {
  width: 100%;
  margin-top: 10px;
  background: #E07A5F;
  border-color: #E07A5F;
  font-weight: 500;
  border-radius: 20px;
}
.adopt-btn:disabled {
  background: #D1D5DB;
  border-color: #D1D5DB;
  cursor: not-allowed;
}

.detail-images {
  flex: 1;
}
.main-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 12px;
  margin-bottom: 10px;
}
.thumbnail-list {
  display: flex;
  gap: 8px;
}
.thumbnail {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}
.thumbnail:hover {
  border-color: #E07A5F;
  transform: scale(1.05);
}
.no-image-large {
  width: 100%;
  height: 300px;
  background: linear-gradient(135deg, #FDF8F3 0%, #F2CC8F 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 100px;
  border-radius: 12px;
}

.pet-detail-container {
  display: flex;
  gap: 20px;
}
.detail-info {
  flex: 1;
}
.detail-info h2 {
  margin: 0 0 15px 0;
  color: #3D405B;
  font-weight: 600;
}
.detail-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.detail-description {
  margin-bottom: 25px;
}
.detail-description h4 {
  margin: 0 0 10px 0;
  color: #6B7280;
}
.detail-description p {
  line-height: 1.8;
  color: #3D405B;
  white-space: pre-wrap;
}
.detail-actions {
  margin-top: 20px;
}

.detail-description p {
  line-height: 1.8;
  color: #3D405B;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
}

.description-text {
  line-height: 1.8;
  color: #3D405B;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

.reply-to { color: #81B29A; font-size: 12px; }

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
