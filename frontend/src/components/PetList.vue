<template>
  <div class="pet-list-page">

    <!-- 顶部 Banner -->
    <div class="hero-banner">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <h1 class="banner-title">为宠物找到温暖的家 🏠</h1>
        <p class="banner-subtitle">浏览待领养的宠物，给它们一个充满爱的家</p>

        <div class="quick-categories">
          <div class="category-card" @click="filterByType('狗')">
            <div class="category-icon">🐶</div>
            <span class="category-name">狗狗</span>
          </div>
          <div class="category-card" @click="filterByType('猫')">
            <div class="category-icon">🐱</div>
            <span class="category-name">猫咪</span>
          </div>
          <div class="category-card" @click="filterByType('其他')">
            <div class="category-icon">🐰</div>
            <span class="category-name">其他动物</span>
          </div>
          <div class="category-card" @click="resetFilter">
            <div class="category-icon">🐾</div>
            <span class="category-name">全部宠物</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索 + 筛选栏 -->
    <div class="filter-bar">
      <el-row :gutter="15" align="middle">
        <el-col :span="6">
          <el-input v-model="searchKeyword" placeholder="🔍 搜索宠物名称" clearable size="large"                    style="--el-input-border-radius: 20px" />
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
                     class="reset-btn">
            重置筛选
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 宠物卡片：展示宠物图片、基本信息及操作按钮 -->
    <el-row :gutter="20" class="pet-grid">
      <el-col v-for="pet in pets" :key="pet.id" :xs="24" :sm="12" :md="8" :lg="6" class="pet-grid-item">
        <el-card class="pet-card" shadow="never" :body-style="{ padding: '0', display: 'flex', flexDirection: 'column', height: '100%' }">
          <div class="image-container" @click="showDetail(pet)">
            <img v-if="getPetImageUrl(pet)" :src="getPetImageUrl(pet)" class="pet-image" />
            <div v-else class="no-image"></div>

            <!-- 状态标签 -->
            <el-tag v-if="pet.status === 'ADOPTED'" class="status-tag adopted">已找到家 🏠</el-tag>
            <el-tag v-else class="status-tag available">等待领养 </el-tag>

            <!-- 悬浮遮罩 -->
            <div class="hover-overlay">
              <el-button type="primary" size="large" class="view-btn">查看详情</el-button>
            </div>
          </div>

          <!-- 卡片信息区 -->
          <div class="pet-info">
            <h3 class="pet-name" @click="showDetail(pet)">
              {{ pet.name }}
              <span class="pet-detail">（{{ pet.gender }} · {{ pet.age }}岁）</span>
            </h3>

            <!-- 标签预览（即使没有标签也占位） -->
            <div class="card-tags">
              <template v-if="pet.tags">
                <span v-for="(tag, i) in JSON.parse(pet.tags).slice(0, 2)" :key="i" class="mini-tag">
                  {{ tag }}
                </span>
              </template>
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
          :page-sizes="[8, 16, 24, 32]"
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

    <AdoptionApply
        v-if="applyDialogVisible"
        :pet="currentApplyPet"
        @close="applyDialogVisible = false"
        @success="onApplySuccess"
    />
  </div>
</template>

<script>export default {
  name: 'PetList'
}
</script>

<script setup>
/**
 * 宠物列表页面组件
 * 提供宠物浏览、多条件筛选、分页查看、详情预览及领养申请功能
 */
import { ref, onMounted, watch, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AdoptionApply from './AdoptionApply.vue'
import { get } from '../utils/request.js'
import { success, warning, error, info } from '../utils/message.js'

const router = useRouter()
const route = useRoute()
const triggerLogin = inject('triggerLogin')

const pets = ref([])
const searchKeyword = ref('')
const filterType = ref('')
const filterGender = ref('')
const filterAge = ref('')
const applyDialogVisible = ref(false)
const currentApplyPet = ref(null)
const loading = ref(false)

/** 当前页码 */
const currentPage = ref(1)
/** 每页显示条数 */
const pageSize = ref(12)
/** 总记录数 */
const total = ref(0)


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
  } catch (err) {
    error(err.message || '获取宠物列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 组件挂载时执行：初始化加载宠物列表
 */
onMounted(() => {
  // 从路由参数恢复分页状态
  if (route.query.page) {
    currentPage.value = parseInt(route.query.page)
  }
  if (route.query.size) {
    pageSize.value = parseInt(route.query.size)
  }
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
 * 跳转到宠物详情页面，携带当前分页参数
 */
const showDetail = (pet) => {
  router.push({
    path: `/pet/${pet.id}`,
    query: {
      page: currentPage.value,
      size: pageSize.value
    }
  })
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
    return []
  }
}




/**
 * 从列表页触发领养申请，校验登录状态与宠物领养状态
 */
const applyAdopt = (pet) => {
  if (!localStorage.getItem('user')) {
    warning('请先登录才能申请领养')
    triggerLogin()
    return
  }
  if (pet.status === 'ADOPTED') {
    info('这只宠物已经被领养啦~')
    return
  }
  currentApplyPet.value = pet
  applyDialogVisible.value = true
}

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

/**
 * 快捷筛选：按类型筛选
 */
const filterByType = (type) => {
  filterType.value = type
  currentPage.value = 1
}

/**
 * 领养申请成功回调
 */
const onApplySuccess = () => {
  success('领养申请已提交！请等待管理员审核')
}

</script>


<style scoped>/* ===== 页面背景与整体布局 ===== */
.pet-list-page {
  background: #f9fafb;
  min-height: 100vh;
  padding-bottom: 40px;
  width: 100%;
}

/* ===== 顶部 Banner - 全宽 ===== */
.hero-banner {
  position: relative;
  width: 100%;
  height: 480px;
  background: url('@/assets/top-banner.png') center/cover no-repeat;
  overflow: hidden;
  margin-bottom: 0;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.2));
}

.banner-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px 20px;
  text-align: center;
  max-width: 1400px;
  margin: 0 auto;
}

.banner-title {
  font-size: 42px;
  font-weight: 800;
  color: #ffffff;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.banner-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 40px 0;
  font-weight: 500;
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.2);
}

/* 快捷分类入口 */
.quick-categories {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  justify-content: center;
}

.category-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 24px 32px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 120px;
  backdrop-filter: blur(8px);
}

.category-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
  background: #ffffff;
}

.category-card:active {
  transform: translateY(-4px);
}

.category-icon {
  font-size: 48px;
  line-height: 1;
}

.category-name {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  white-space: nowrap;
}

/* ===== 筛选栏样式 - 全宽 ===== */
.filter-bar {
  margin: 30px 40px;
  padding: 20px 24px;
  background: #ffffff;
  border-radius: 0;
  border: none;
  box-shadow: none;
}

/* ===== 宠物卡片网格 - 全宽等高布局 ===== */
.pet-grid {
  width: 100%;
  padding: 0 40px;
}

.pet-grid-item {
  display: flex;
  margin-bottom: 20px;
}

/* 卡片占满列高度 */
.pet-card {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  box-shadow: 8px 8px 0px rgba(0, 0, 0, 0.08);
  animation: slideUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
  height: 100%;
  display: flex;
  flex-direction: column;
  width: 100%;
}

/* 卡片加载滑入动画 */
@keyframes slideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 为不同位置的卡片添加延迟，形成错落感 */
.pet-grid-item:nth-child(1) .pet-card { animation-delay: 0.05s; }
.pet-grid-item:nth-child(2) .pet-card { animation-delay: 0.1s; }
.pet-grid-item:nth-child(3) .pet-card { animation-delay: 0.15s; }
.pet-grid-item:nth-child(4) .pet-card { animation-delay: 0.2s; }
.pet-grid-item:nth-child(5) .pet-card { animation-delay: 0.25s; }
.pet-grid-item:nth-child(6) .pet-card { animation-delay: 0.3s; }
.pet-grid-item:nth-child(7) .pet-card { animation-delay: 0.35s; }
.pet-grid-item:nth-child(8) .pet-card { animation-delay: 0.4s; }
.pet-grid-item:nth-child(9) .pet-card { animation-delay: 0.45s; }
.pet-grid-item:nth-child(10) .pet-card { animation-delay: 0.5s; }
.pet-grid-item:nth-child(11) .pet-card { animation-delay: 0.55s; }
.pet-grid-item:nth-child(12) .pet-card { animation-delay: 0.6s; }

/* Hover 悬浮提升效果 */
.pet-card:hover {
  transform: translateY(-8px);
  box-shadow: 12px 12px 0px rgba(0, 0, 0, 0.12);
  border-color: #d1d5db;
}

/* ===== 图片容器 ===== */
.image-container {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  height: 280px;
  flex-shrink: 0;
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.pet-card:hover .pet-image {
  transform: scale(1.08);
}

.no-image {
  width: 100%;
  height: 280px;
  background: url('@/assets/pets-banner.png') center/cover no-repeat;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== 状态标签系统 ===== */
.status-tag {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 2;
  font-weight: 600;
  border: none;
  padding: 8px 16px;
  border-radius: 9999px;
  font-size: 13px;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.status-tag.available {
  background: rgba(16, 185, 129, 0.12);
  color: #065f46;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-tag.adopted {
  background: rgba(244, 63, 94, 0.12);
  color: #9f1239;
  border: 1px solid rgba(244, 63, 94, 0.2);
}

/* ===== 悬浮遮罩 ===== */
.hover-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.1));
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
  background: #cc785c;
  border-color: #cc785c;
  border-radius: 16px;
  font-weight: 600;
  padding: 12px 32px;
  font-size: 15px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.view-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.view-btn:hover::before {
  left: 100%;
}

.pet-card:hover .view-btn { transform: translateY(0); }

/* ===== 卡片信息区 - 固定高度确保按钮对齐 ===== */
.pet-info {
  padding: 24px;
  display: flex;
  flex-direction: column;
  height: 220px;
}

.pet-name {
  margin: 0 0 8px 0;
  font-size: 22px;
  color: #111827;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: -0.3px;
  line-height: 1.3;
  transition: color 0.2s ease;
  min-height: 32px;
}

.pet-name:hover {
  color: #cc785c;
}

.pet-detail {
  color: #6b7280;
  font-size: 14px;
  font-weight: 400;
}

/* 卡片内标签预览 - 固定高度占位 */
.card-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  height: 28px;
  align-items: center;
}

.mini-tag {
  font-size: 12px;
  color: #be185d;
  background: rgba(236, 72, 153, 0.1);
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 500;
}

/* 描述文本 - 固定高度 */
.description {
  margin: 0 0 16px 0;
  color: #4b5563;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14px;
  height: 44px;
  flex-shrink: 0;
}

/* ===== 我要领养按钮 - 自动推到底部 ===== */
.adopt-btn {
  width: 100%;
  margin-top: auto;
  background: #a9583e;
  border-color: #a9583e;
  font-weight: 600;
  border-radius: 16px;
  padding: 14px 24px;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(169, 88, 62, 0.25);
  position: relative;
  overflow: hidden;
  z-index: 1;
  flex-shrink: 0;
}

.adopt-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.adopt-btn:hover::before {
  left: 100%;
}

.adopt-btn:hover:not(:disabled) {
  background: #8f4a33;
  border-color: #8f4a33;
  transform: scale(1.03);
  box-shadow: 0 6px 16px rgba(169, 88, 62, 0.35);
}

.adopt-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.adopt-btn:disabled {
  background: #e5e7eb;
  border-color: #e5e7eb;
  color: #9ca3af;
  box-shadow: none;
  cursor: not-allowed;
}

/* ===== 重置筛选按钮 ===== */
.reset-btn {
  background: #cc785c;
  border-color: #cc785c;
  width: 100%;
  border-radius: 16px;
  padding: 14px 24px;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(204, 120, 92, 0.25);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.reset-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.reset-btn:hover::before {
  left: 100%;
}

.reset-btn:hover {
  background: #a9583e;
  border-color: #a9583e;
  transform: scale(1.03);
  box-shadow: 0 6px 16px rgba(204, 120, 92, 0.35);
}

/* ===== 分页样式 ===== */
.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.custom-pagination :deep(.el-pager li.active) {
  background: #cc785c;
  color: white;
  border-radius: 12px;
  font-weight: 600;
}

.custom-pagination :deep(.el-pager li:hover) {
  color: #cc785c;
}

/* ===== 性格标签 ===== */
.personality-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.p-tag {
  background: rgba(236, 72, 153, 0.1) !important;
  color: #be185d !important;
  border-color: rgba(236, 72, 153, 0.2) !important;
  border-radius: 12px;
  font-weight: 600;
  padding: 6px 14px;
}

/* ===== 卡片内标签预览 ===== */
.card-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  min-height: 28px;
}

.mini-tag {
  font-size: 12px;
  color: #be185d;
  background: rgba(236, 72, 153, 0.1);
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 500;
}

</style>
