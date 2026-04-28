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
            <el-tag v-else class="status-tag available">等待领养 </el-tag>

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

            <!-- 新增：列表页标签预览 -->
            <div v-if="pet.tags" class="card-tags">
              <span v-for="(tag, i) in JSON.parse(pet.tags).slice(0, 2)" :key="i" class="mini-tag">
                {{ tag }}
              </span>
            </div>

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

    <!-- 宠物详情弹窗：仿 Petfinder 左右分栏风格 -->
    <el-dialog v-model="detailVisible" width="900px" class="pet-detail-dialog" :show-close="true">
      <div v-if="selectedPet" class="pet-detail-container">
        <!-- 左侧：图片画廊 -->
        <div class="detail-gallery">
          <div class="main-image-wrapper">
            <img v-if="selectedPet.photos && selectedPet.photos.length > 0"
                 :src="'http://localhost:8080' + selectedPet.photos[0]"
                 class="main-image" />
            <div v-else class="no-image-large">🐾</div>
          </div>
          <!-- 缩略图列表 -->
          <div v-if="selectedPet.photos && selectedPet.photos.length > 1" class="thumbnail-list">
            <img
                v-for="(photo, index) in selectedPet.photos.slice(1)"
                :key="index"
                :src="'http://localhost:8080' + photo"
                class="thumbnail"
            />
          </div>
        </div>

        <!-- 右侧：详细信息 -->
        <div class="detail-content">
          <div class="detail-header">
            <h2>{{ selectedPet.name }}</h2>
            <div class="basic-tags">
              <el-tag class="tag-gender">{{ selectedPet.gender }}</el-tag>
              <el-tag class="tag-age">{{ selectedPet.age }} 岁</el-tag>
              <el-tag class="tag-type">{{ selectedPet.type }}</el-tag>
            </div>
          </div>

          <div class="detail-section">
            <h4>📝 关于 {{ selectedPet.name }}</h4>
            <p class="story-text">{{ selectedPet.description || '这只小家伙正在等待一个温暖的家...' }}</p>
          </div>

          <div v-if="selectedPet.tags && selectedPet.tags.length > 0" class="detail-section">
            <h4>✨ 性格特征</h4>
            <div class="personality-tags">
              <el-tag
                  v-for="(tag, index) in getSafeTags(selectedPet.tags)"
                  :key="index"
                  class="p-tag"
                  effect="light">
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div class="detail-section">
            <h4>💉 健康状况</h4>
            <div class="health-grid">
              <div class="health-item" :class="{ 'inactive': !selectedPet.vaccinated }">
                {{ selectedPet.vaccinated ? '✅' : '❌' }} 已疫苗
              </div>
              <div class="health-item" :class="{ 'inactive': !selectedPet.neutered }">
                {{ selectedPet.neutered ? '✅' : '❌' }} 已绝育
              </div>
              <div class="health-item" :class="{ 'inactive': !selectedPet.dewormed }">
                {{ selectedPet.dewormed ? '✅' : '❌' }} 已驱虫
              </div>
            </div>
          </div>

          <div class="detail-actions">
            <el-button type="primary" size="large" class="apply-btn"
                       :disabled="selectedPet.status === 'ADOPTED'"
                       @click="applyFromDetail">
              {{ selectedPet.status === 'ADOPTED' ? '已被领养' : '申请领养' }}
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
 * 安全解析标签，防止 JSON 格式错误或空值导致页面崩溃
 */
const getSafeTags = (tagsStr) => {
  if (!tagsStr) return []
  try {
    const parsed = JSON.parse(tagsStr)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    console.warn('标签格式解析失败:', tagsStr)
    return [] // 如果解析失败，返回空数组，保证页面不崩
  }
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
  border: 1px solid #F3F4F6;
  background: white;
}

.pet-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.image-container {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  height: 240px;
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.pet-card:hover .pet-image {
  transform: scale(1.1);
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
  font-weight: 600;
  border: none;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.status-tag.available { background: #D1FAE5; color: #065F46; }
.status-tag.adopted { background: #FEE2E2; color: #991B1B; }

.hover-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(to top, rgba(61, 64, 91, 0.8), rgba(61, 64, 91, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.pet-card:hover .hover-overlay { opacity: 1; }

.view-btn {
  transform: translateY(10px);
  transition: all 0.3s ease;
  background: #E07A5F;
  border-color: #E07A5F;
  border-radius: 24px;
  font-weight: 600;
}

.pet-card:hover .view-btn { transform: translateY(0); }

.pet-info { padding: 20px; }

.pet-name {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #1F2937;
  cursor: pointer;
  font-weight: 700;
}

.pet-name:hover { color: #E07A5F; }

.pet-detail { color: #6B7280; font-size: 14px; font-weight: 400; }

.type-tag {
  background: #FEF3C7; color: #D97706; border-color: #FEF3C7;
  border-radius: 8px; font-weight: 500;
}

.description {
  margin: 12px 0;
  color: #4B5563;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14px;
}

.adopt-btn {
  width: 100%; margin-top: 12px;
  background: #E07A5F; border-color: #E07A5F;
  font-weight: 600; border-radius: 12px;
}

.adopt-btn:disabled { background: #D1D5DB; border-color: #D1D5DB; }

/* --- 详情页样式 (仿 Petfinder 布局) --- */
.pet-detail-dialog :deep(.el-dialog__header) { display: none; }
.pet-detail-dialog :deep(.el-dialog__body) { padding: 20px; }

.pet-detail-container {
  display: flex;
  gap: 30px;
}

.detail-gallery { flex: 1.2; }

.main-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.thumbnail {
  width: 70px; height: 70px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.thumbnail:hover { border-color: #E07A5F; }

.detail-content { flex: 1; display: flex; flex-direction: column; }

.detail-header h2 {
  font-size: 32px;
  color: #1F2937;
  margin: 0 0 12px 0;
}

.basic-tags { display: flex; gap: 8px; margin-bottom: 24px; }
.tag-gender { background: #DBEAFE; color: #1E40AF; border: none; }
.tag-age { background: #D1FAE5; color: #065F46; border: none; }
.tag-type { background: #FEF3C7; color: #92400E; border: none; }

.detail-section { margin-bottom: 24px; }
.detail-section h4 { color: #374151; margin-bottom: 8px; font-size: 16px; }

.story-text {
  line-height: 1.8;
  color: #4B5563;
  font-size: 15px;
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.health-item {
  background: #F9FAFB;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: #374151;
}

.apply-btn {
  margin-top: auto;
  background: #E07A5F;
  border: none;
  border-radius: 12px;
  height: 50px;
  font-size: 16px;
  font-weight: bold;
}

/* --- 分页样式 --- */
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
.custom-pagination :deep(.el-pager li.active) {
  background: #E07A5F;
  color: white;
}
.personality-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.p-tag {
  background: #FDF2F8 !important; /* 浅粉色背景 */
  color: #BE185D !important;       /* 深粉色文字 */
  border-color: #FBCFE8 !important;
  border-radius: 6px;
  font-weight: 500;
}
.card-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}
.mini-tag {
  font-size: 12px;
  color: #BE185D;
  background: #FDF2F8;
  padding: 2px 8px;
  border-radius: 4px;
}

</style>
