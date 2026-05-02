<template>
  <div class="pet-detail-page">
    <!-- 顶部返回导航 -->
    <div class="detail-nav">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
        <span>返回宠物列表</span>
      </button>
      <span class="nav-breadcrumb">宠物列表 / 详情</span>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 宠物详情内容 -->
    <div v-else-if="pet" class="pet-profile">
      <!-- 左侧：照片展示区 -->
      <div class="profile-photo-section">
        <div class="photo-frame">
          <img v-if="pet.photos && pet.photos.length > 0"
               :src="'http://localhost:8080' + pet.photos[currentPhotoIndex]"
               class="profile-main-img" />
          <div v-else class="profile-main-img no-image-placeholder"></div>

          <!-- 切换箭头 -->
          <button
              v-if="pet.photos && pet.photos.length > 1"
              class="arrow-btn prev-btn"
              @click="prevPhoto">
            ‹
          </button>
          <button
              v-if="pet.photos && pet.photos.length > 1"
              class="arrow-btn next-btn"
              @click="nextPhoto">
            ›
          </button>
        </div>

        <!-- 多照片缩略图 -->
        <div v-if="pet.photos && pet.photos.length > 1" class="thumbnail-strip">
          <img
              v-for="(photo, index) in pet.photos"
              :key="index"
              :src="'http://localhost:8080' + photo"
              class="thumbnail"
              :class="{ 'active': currentPhotoIndex === index }"
              @click="currentPhotoIndex = index"
          />
        </div>
      </div>

      <!-- 右侧：信息展示区 -->
      <div class="profile-info-section">
        <!-- 头部：名字与基础标签 -->
        <div class="profile-header">
          <h1 class="pet-name">{{ pet.name }}</h1>
          <div class="pet-badges">
            <span class="badge badge-type">{{ pet.type }}</span>
            <span class="badge badge-gender">{{ pet.gender }}</span>
            <span class="badge badge-age">{{ pet.age }} 岁</span>
          </div>
        </div>

        <!-- 身份块 -->
        <div class="info-block identity-block">
          <h3 class="block-title">📝 档案摘要</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">品种</span>
              <span class="value">{{ pet.type }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别</span>
              <span class="value">{{ pet.gender }}</span>
            </div>
          </div>
        </div>

        <!-- 健康块 -->
        <div class="info-block health-block">
          <h3 class="block-title">💉 健康状态</h3>
          <div class="health-cards">
            <div class="health-card" :class="{ 'is-active': pet.vaccinated }">
              <span class="card-icon">💉</span>
              <span class="card-text">是否接种疫苗</span>
            </div>
            <div class="health-card" :class="{ 'is-active': pet.dewormed }">
              <span class="card-icon">🦠</span>
              <span class="card-text">是否驱虫</span>
            </div>
            <div class="health-card" :class="{ 'is-active': pet.neutered }">
              <span class="card-icon">✂️</span>
              <span class="card-text">是否绝育</span>
            </div>
          </div>
        </div>

        <!-- 故事块 -->
        <div class="info-block story-block">
          <h3 class="block-title">✨ 详细介绍</h3>
          <p class="story-text">{{ pet.description || '这只小家伙还没有写自我介绍，等你来发现它的独特之处...' }}</p>
          <div class="doodle-decoration"></div>
        </div>

        <!-- 底部操作区 -->
        <div class="action-area">
          <el-button type="primary" class="apply-btn"
                     :disabled="pet.status === 'ADOPTED'"
                     @click="applyAdopt">
            {{ pet.status === 'ADOPTED' ? '已被领养' : '💖 申请领养' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 未找到宠物 -->
    <el-empty v-else description="未找到该宠物信息" :image-size="200" />

    <!-- 领养申请弹窗：复用 AdoptionApply 组件 -->
    <AdoptionApply
        v-if="applyDialogVisible"
        :pet="pet"
        @close="applyDialogVisible = false"
        @success="onApplySuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '../utils/request.js'
import AdoptionApply from './AdoptionApply.vue'
import { warning, error } from '../utils/message.js'

const route = useRoute()
const router = useRouter()

const triggerLogin = inject('triggerLogin')

const pet = ref(null)
const loading = ref(true)
const currentPhotoIndex = ref(0)
const applyDialogVisible = ref(false)



// 加载宠物详情
const loadPetDetail = async () => {
  loading.value = true
  try {
    const response = await get(`/api/pet/${route.params.id}`)

    // 解析照片数据
    if (response.photoUrls) {
      try {
        response.photos = JSON.parse(response.photoUrls)
      } catch {
        response.photos = []
      }
    } else if (response.photoUrl) {
      response.photos = [response.photoUrl]
    } else {
      response.photos = []
    }

    pet.value = response
    currentPhotoIndex.value = 0
  } catch (err) {
    error('加载宠物详情失败')
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 上一张照片
const prevPhoto = () => {
  if (currentPhotoIndex.value > 0) {
    currentPhotoIndex.value--
  }
}

// 下一张照片
const nextPhoto = () => {
  if (pet.value.photos && currentPhotoIndex.value < pet.value.photos.length - 1) {
    currentPhotoIndex.value++
  }
}

// 返回上一页，保持分页状态
const goBack = () => {
  router.back()
}


// 申请领养
const applyAdopt = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    warning('请先登录才能申请领养')
    triggerLogin()
    return
  }
  applyDialogVisible.value = true
}

const onApplySuccess = () => {
  //ElMessage.success('领养申请已提交！请等待管理员审核')
}


onMounted(() => {
  loadPetDetail()
})
</script>


<style scoped>
.pet-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
  background: #ffffff;
  min-height: calc(100vh - 65px);
}

/* 顶部导航 */
.detail-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #1f2937;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 2px solid #1f2937;
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  box-shadow: 4px 4px 0px #1f2937;
  transition: all 0.2s;
}

.back-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0px #1f2937;
}

.back-btn:active {
  transform: translate(0, 0);
  box-shadow: 2px 2px 0px #1f2937;
}

.nav-breadcrumb {
  color: #6b7280;
  font-weight: 600;
  font-size: 14px;
}

/* 加载状态 */
.loading-container {
  padding: 40px;
  background: #f9fafb;
  border-radius: 24px;
  border: 2px solid #e5e7eb;
}

/* 宠物详情布局 */
.pet-profile {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

/* 左侧：照片区 */
.profile-photo-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 32px;
}

.photo-frame {
  position: relative;
  border: 3px solid #1f2937;
  border-radius: 24px;
  box-shadow: 10px 10px 0px #1f2937;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s ease;
}

.photo-frame:hover {
  transform: translate(-3px, -3px);
  box-shadow: 13px 13px 0px #1f2937;
}

.profile-main-img {
  width: 100%;
  height: 420px;
  object-fit: cover;
  display: block;
}

.no-image-placeholder {
  background: #f3f4f6 url('@/assets/pets-banner.png') center/cover no-repeat;
}

/* 切换箭头 */
.arrow-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #1f2937;
  border-radius: 50%;
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 4px 4px 0px #1f2937;
  z-index: 10;
}

.arrow-btn:hover {
  background: #ffffff;
  transform: translateY(-50%) scale(1.1);
  box-shadow: 6px 6px 0px #1f2937;
}

.arrow-btn:active {
  transform: translateY(-50%) scale(0.95);
  box-shadow: 2px 2px 0px #1f2937;
}

.prev-btn { left: 16px; }
.next-btn { right: 16px; }

/* 缩略图 */
.thumbnail-strip {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px;
}

.thumbnail {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 12px;
  border: 2px solid #1f2937;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  box-shadow: 3px 3px 0px #1f2937;
}

.thumbnail:hover {
  transform: scale(1.05);
}

.thumbnail.active {
  border-color: #cc785c;
  transform: scale(1.05) rotate(-3deg);
  box-shadow: 4px 4px 0px #cc785c;
}

/* 右侧：信息区 */
.profile-info-section {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-header {
  border-bottom: 3px solid #1f2937;
  padding-bottom: 20px;
}

.pet-name {
  font-size: 42px;
  font-weight: 900;
  color: #111827;
  margin: 0 0 16px 0;
  line-height: 1.1;
  letter-spacing: -1px;
}

.pet-badges {
  display: flex;
  gap: 12px;
}

.badge {
  padding: 6px 14px;
  border: 2px solid #1f2937;
  border-radius: 8px;
  font-weight: 800;
  font-size: 14px;
  box-shadow: 4px 4px 0px #1f2937;
  background: #fff;
  text-transform: uppercase;
}

.badge-type { background: #e0f2fe; color: #0369a1; }
.badge-gender { background: #fce7f3; color: #be185d; }
.badge-age { background: #d1fae5; color: #047857; }

/* 通用信息块 */
.info-block {
  background: #fff;
  border: 2px solid #1f2937;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 6px 6px 0px #1f2937;
  position: relative;
  overflow: hidden;
}

.block-title {
  font-size: 16px;
  font-weight: 900;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #374151;
}

/* 身份块 */
.identity-block {
  background: #fffbeb;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item .label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 700;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 20px;
  font-weight: 800;
  color: #111827;
}

/* 健康块 */
.health-block {
  background: #f0fdf4;
}

.health-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.health-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  transition: all 0.2s;
}

.health-card.is-active {
  border-color: #10b981;
  background: #d1fae5;
  box-shadow: 4px 4px 0px #10b981;
  transform: translateY(-2px);
}

.health-card:not(.is-active) {
  border-color: #ef4444;
  background: #fef2f2;
  box-shadow: 4px 4px 0px #ef4444;
  opacity: 0.85;
}

.card-icon {
  font-size: 24px;
  margin-bottom: 6px;
}

.card-text {
  font-size: 13px;
  font-weight: 700;
  color: #374151;
}

/* 故事块 */
.story-block {
  background: #faf5ff;
  display: flex;
  flex-direction: column;
}

.story-block .block-title {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.story-text {
  font-size: 14px;
  line-height: 1.7;
  color: #4b5563;
  font-weight: 500;
  margin: 0;
  position: relative;
  z-index: 1;
  max-height: 120px;
  overflow-y: auto;
  padding-right: 12px;
}

/* 滚动条 */
.story-text::-webkit-scrollbar {
  width: 8px;
}

.story-text::-webkit-scrollbar-track {
  background: #fff;
  border: 2px solid #1f2937;
  border-radius: 4px;
  margin: 4px 0;
}

.story-text::-webkit-scrollbar-thumb {
  background: #cc785c;
  border: 2px solid #1f2937;
  border-radius: 4px;
  box-shadow: 2px 2px 0px #1f2937;
}

.story-text::-webkit-scrollbar-thumb:hover {
  background: #b3654c;
  box-shadow: 3px 3px 0px #1f2937;
}

.doodle-decoration {
  position: absolute;
  bottom: -15px;
  right: -15px;
  width: 80px;
  height: 80px;
  background-image: radial-gradient(#d1d5db 3px, transparent 3px);
  background-size: 12px 12px;
  opacity: 0.5;
  z-index: 0;
}

/* 操作按钮 */
.action-area {
  margin-top: auto;
}

.apply-btn {
  width: 100%;
  height: 64px;
  font-size: 20px;
  font-weight: 900;
  background: #cc785c !important;
  border: 3px solid #1f2937 !important;
  color: #fff;
  border-radius: 16px !important;
  box-shadow: 8px 8px 0px #1f2937 !important;
  transition: all 0.2s ease;
}

.apply-btn:hover:not(:disabled) {
  background: #b3654c !important;
  transform: translate(-4px, -4px);
  box-shadow: 12px 12px 0px #1f2937 !important;
}

.apply-btn:active:not(:disabled) {
  transform: translate(0, 0);
  box-shadow: 4px 4px 0px #1f2937 !important;
}

.apply-btn:disabled {
  background: #e5e7eb !important;
  border-color: #d1d5db !important;
  box-shadow: none !important;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 900px) {
  .pet-profile {
    flex-direction: column;
  }
  .profile-main-img { height: 250px; }
  .pet-detail-page { padding: 20px; }
}
</style>
