<template>
  <div class="help-post-page">

    <!-- 发布按钮居中 -->
    <div class="publish-btn-wrapper">
      <el-button type="primary" @click="showPublishPanel" class="publish-btn">
        发布新互助帖
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <el-row :gutter="15" align="middle">
        <el-col :span="7">
          <el-input v-model="searchKeyword" placeholder="🔍 搜索帖子标题或内容" clearable
                    class="search-input" />
        </el-col>
        <el-col :span="5">
          <el-select v-model="filterCategory" placeholder="筛选分类" clearable class="filter-select">
            <el-option label="全部" value="" />
            <el-option label="📦 物资共享" value="物资共享" />
            <el-option label="🏥 医疗咨询" value="医疗咨询" />
            <el-option label="📚 经验分享" value="经验分享" />
            <el-option label="🔍 寻宠启事" value="寻宠启事" />
            <el-option label="💝 领养回访" value="领养回访" />
            <el-option label="🆘 求助信息" value="求助信息" />
            <el-option label="🎉 活动聚会" value="活动聚会" />
            <el-option label="🛒 闲置转让" value="闲置转让" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="filterPetType" placeholder="🐾 宠物类型" clearable class="filter-select">
            <el-option label="全部" value="" />
            <el-option label="🐱 猫咪" value="猫" />
            <el-option label="🐶 狗狗" value="狗" />
            <el-option label="🐰 其他" value="其他" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="sortBy" placeholder="排序方式" class="filter-select">
            <el-option label="最新发布" value="newest" />
            <el-option label="最早发布" value="oldest" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="resetFilter" class="reset-btn">
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>


    <!-- 卡片式帖子列表 -->
    <el-row :gutter="24">
      <el-col v-for="post in filteredPosts" :key="post.id" :xs="24" :sm="12" :md="8" class="post-col">
        <el-card class="post-card" shadow="hover" @click="goToPostDetail(post)">
          <!-- 帖子封面图 -->
          <div v-if="getPostCover(post)" class="post-cover">
            <img :src="getPostCover(post)" alt="帖子封面" />
            <div class="cover-overlay">
              <span class="view-hint">点击查看</span>
            </div>
          </div>

          <div class="post-content-wrapper">
            <div class="post-header">
              <el-tag :type="getCategoryType(post.category)" size="small" class="category-tag">{{ post.category }}</el-tag>
              <span class="post-time">{{ formatDateTime(post.createTime) }}</span>
              <el-button
                  v-if="currentUser && (currentUser.role === 'ADMIN' || currentUser.username === post.user?.username)"
                  type="danger"
                  size="small"
                  link
                  @click.stop="handleDeletePost(post)"
                  class="delete-btn"
              >
                删除
              </el-button>
            </div>
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-content">{{ post.content }}</p>
            <div class="post-footer">
              <span class="author">👤 {{ post.user?.username || post.author || '匿名' }}</span>
              <span class="comment-count">💬 {{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="filteredPosts.length === 0" description="暂无帖子" :image-size="150" />

    <!-- 全屏发布面板（抽屉式） -->
    <transition name="drawer-fade">
      <div v-if="publishVisible" class="publish-drawer">
        <div class="drawer-overlay" @click="closePublishPanel"></div>
        <div class="drawer-content">
          <!-- 抽屉头部 -->
          <div class="drawer-header">
            <h3 class="drawer-title">✨ 发布互助帖</h3>
            <button class="close-btn" @click="closePublishPanel">✕</button>
          </div>

          <!-- 抽屉主体：左图右文布局 -->
          <div class="drawer-body">
            <!-- 左侧：图片上传区 -->
            <div class="upload-section">
              <h4 class="section-title">📷 上传图片</h4>

              <!-- 隐藏的 file input，始终存在 -->
              <input
                  type="file"
                  ref="fileInput"
                  multiple
                  accept="image/*"
                  @change="handleFileSelect"                  style="display: none"
              />

              <div class="upload-area" @dragover.prevent @drop.prevent="handleDrop">
                <div class="upload-placeholder" v-if="!postForm.images.length">
                  <div class="upload-icon">📸</div>
                  <p class="upload-text">拖拽图片到此处</p>
                  <p class="upload-hint">或点击上传，支持 JPG、PNG 格式，单张不超过5MB</p>
                  <el-button type="primary" @click="triggerFileInput" class="select-btn">
                    选择图片
                  </el-button>
                </div>

                <!-- 图片预览网格 -->
                <div v-else class="image-preview-grid">
                  <div
                      v-for="(img, index) in postForm.images"
                      :key="index"
                      class="image-item"
                  >
                    <img :src="img.preview" :alt="img.name" />
                    <div class="image-actions">
                      <button class="action-btn set-cover" @click="setCover(index)" title="设为封面">
                        ⭐
                      </button>
                      <button class="action-btn delete" @click="removeImage(index)" title="删除">
                        ✕
                      </button>
                    </div>
                    <div v-if="img.isCover" class="cover-badge">封面</div>
                    <div v-if="img.uploading" class="upload-progress">
                      <div class="progress-bar" :style="{ width: img.progress + '%' }"></div>
                    </div>
                  </div>

                  <!-- "继续添加"按钮 -->
                  <div class="add-more" @click="triggerFileInput">
                    <span class="add-icon">+</span>
                    <p>继续添加</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右侧：文字编辑区 -->
            <div class="form-section">
              <h4 class="section-title">📝 帖子内容</h4>
              <el-form :model="postForm" class="publish-form">
                <el-form-item label="分类" required class="form-item">
                  <div class="tag-cloud">
                    <div
                        v-for="cat in categories"
                        :key="cat.value"
                        :class="['tag-item', { active: postForm.category === cat.value }]"
                        @click="postForm.category = cat.value"
                    >
                      {{ cat.icon }} {{ cat.label }}
                    </div>
                  </div>
                </el-form-item>

                <el-form-item label="标题" required class="form-item">
                  <el-input
                      v-model="postForm.title"
                      placeholder="给你的帖子起个标题吧~"
                      maxlength="50"
                      show-word-limit
                      class="title-input"
                  />
                </el-form-item>

                <el-form-item label="内容" required class="form-item">
                  <el-input
                      v-model="postForm.content"
                      type="textarea"
                      rows="8"
                      placeholder="分享你的故事、问题或经验..."
                      maxlength="2000"
                      show-word-limit
                      class="content-input"
                  />
                </el-form-item>

                <el-form-item label="所在位置" class="form-item">
                  <el-cascader
                      v-model="postForm.location"
                      :options="regionOptions"
                      :props="cascaderProps"
                      placeholder="请选择省/市/区"
                      clearable
                      filterable
                      class="location-cascader"
                  />
                </el-form-item>

                <el-form-item label="关联宠物（可选）" class="form-item">
                  <el-select
                      v-model="postForm.relatedPetId"
                      placeholder="选择关联的宠物，方便其他人找到它"
                      clearable
                      filterable
                      class="pet-select"
                  >
                    <el-option
                        v-for="pet in availablePets"
                        :key="pet.id"
                        :label="`${pet.name}（${pet.type}）`"
                        :value="pet.id"
                    >
                      <div style="display: flex; align-items: center; gap: 8px">
                        <img v-if="getPetThumb(pet)" :src="getPetThumb(pet)" style="width: 30px; height: 30px; border-radius: 6px; object-fit: cover" />
                        <span>{{ pet.name }}</span>
                        <el-tag size="small">{{ pet.type }}</el-tag>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>

              </el-form>
            </div>
          </div>

          <div class="drawer-footer">
            <el-button @click="closePublishPanel" class="cancel-btn">取消</el-button>
            <el-button
                type="primary"
                @click="submitPost"
                :loading="loading"
                class="submit-btn"
            >
              {{ uploadingImages ? '上传图片中...' : '发布帖子' }}
            </el-button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 分页组件 -->
    <div v-if="!detailVisible" class="pagination-wrapper">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, prev, pager, next, sizes, jumper"
          :pager-count="7"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
          class="custom-pagination"
      />
    </div>

  </div>
</template>

<script setup>
import {ref, onMounted, computed, nextTick, watch, inject} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { get, post, del } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'
import { regionData, codeToText } from 'element-china-area-data'

const router = useRouter()
const route = useRoute()
const triggerLogin = inject('triggerLogin')

const posts = ref([])
const searchKeyword = ref('')
const filterCategory = ref('')
const filterPetType = ref('')
const sortBy = ref('newest')

// 省市区数据源（直接使用 npm 包提供的完整数据）
const regionOptions = regionData

// 级联选择器配置
const cascaderProps = {
  checkStrictly: true, // 允许选择任意级别
  emitPath: true,      // 返回完整路径数组
  expandTrigger: 'hover'
}

const filteredPosts = computed(() => {
  let result = [...posts.value]
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(post =>
        post.title?.toLowerCase().includes(keyword) ||
        post.content?.toLowerCase().includes(keyword)
    )
  }
  if (filterCategory.value) {
    result = result.filter(post => post.category === filterCategory.value)
  }
  return result
})

const resetFilter = () => {
  searchKeyword.value = ''
  filterCategory.value = ''
  filterPetType.value = ''
  sortBy.value = 'newest'
  currentPage.value = 1
  loadPostsFromServer()
}

watch([sortBy, searchKeyword, filterCategory, filterPetType], () => {
  currentPage.value = 1
  loadPostsFromServer()
})

const publishVisible = ref(false)
const loading = ref(false)
const uploadingImages = ref(false)
const fileInput = ref(null)

const categories = [
  { label: '物资共享', value: '物资共享', icon: '📦' },
  { label: '医疗咨询', value: '医疗咨询', icon: '🏥' },
  { label: '经验分享', value: '经验分享', icon: '📚' },
  { label: '寻宠启事', value: '寻宠启事', icon: '🔍' },
  { label: '领养回访', value: '领养回访', icon: '💝' },
  { label: '求助信息', value: '求助信息', icon: '🆘' },
  { label: '活动聚会', value: '活动聚会', icon: '🎉' },
  { label: '闲置转让', value: '闲置转让', icon: '🛒' }
]

const postForm = ref({
  category: '',
  title: '',
  content: '',
  city: '',
  images: [],
  relatedPetId: null
})


const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jumpPage = ref(1)

//可关联的宠物列表
const availablePets = ref([])

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

const getCategoryType = (category) => {
  const typeMap = {
    '物资共享': '',
    '医疗咨询': 'warning',
    '经验分享': 'info',
    '寻宠启事': 'danger',
    '领养回访': 'success',
    '求助信息': 'warning',
    '活动聚会': '',
    '闲置转让': 'info'
  }
  return typeMap[category] || ''
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 获取帖子封面图片
 */
const getPostCover = (post) => {
  if (!post || !post.photoUrls) return null
  try {
    const photos = JSON.parse(post.photoUrls)
    if (photos && photos.length > 0) {
      const url = photos[0]
      return url.startsWith('http') ? url : 'http://localhost:8080' + url
    }
  } catch (e) {
    // 如果是单个字符串
    if (typeof post.photoUrls === 'string' && post.photoUrls.trim()) {
      const url = post.photoUrls.trim()
      return url.startsWith('http') ? url : 'http://localhost:8080' + url
    }
  }
  return null
}

/**
 * 获取宠物缩略图
 */
const getPetThumb = (pet) => {
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
    } catch {}
  }
  return null
}

/**
 * 加载待领养宠物列表
 */
const loadAvailablePets = async () => {
  try {
    const data = await get('/api/pet/list', { page: 0, size: 50 })
    availablePets.value = data.content.filter(pet => pet.status === 'AVAILABLE')
  } catch (err) {
    console.error('加载宠物列表失败:', err)
  }
}

onMounted(async () => {
  if (route.query.page) {
    currentPage.value = parseInt(route.query.page)
  }
  if (route.query.size) {
    pageSize.value = parseInt(route.query.size)
  }
  await loadPostsFromServer()
  nextTick(() => {
    const savedScrollY = sessionStorage.getItem('helpScrollPosition')
    if (savedScrollY && parseInt(savedScrollY) > 0) {
      const scrollY = parseInt(savedScrollY)
      window.scrollTo(0, scrollY)
      document.documentElement.scrollTop = scrollY
      document.body.scrollTop = scrollY
      sessionStorage.removeItem('helpScrollPosition')
    }
  })
})


const loadPostsFromServer = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value
    }

    if (filterPetType.value) {
      params.petType = filterPetType.value
    }

    const data = await get('/api/help/list', params)
    posts.value = data.content
    total.value = data.totalElements

    const postsWithComments = await Promise.all(
        posts.value.map(async (post) => {
          try {
            const commentData = await get(`/api/help/comments/${post.id}`)
            post.commentCount = commentData.reduce((sum, c) => sum + 1 + (c.replies?.length || 0), 0)
          } catch (e) {
            post.commentCount = 0
          }
          return post
        })
    )
    posts.value = postsWithComments
  } catch (err) {
    error(err.message || '加载帖子失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadPostsFromServer()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadPostsFromServer()
}

const handleJumpPage = () => {
  if (jumpPage.value && jumpPage.value >= 1 && jumpPage.value <= Math.ceil(total.value / pageSize.value)) {
    currentPage.value = jumpPage.value
    loadPostsFromServer()
  }
}

const goToPostDetail = (post) => {
  const currentScrollY = window.scrollY || document.documentElement.scrollTop
  sessionStorage.setItem('helpScrollPosition', currentScrollY.toString())
  router.push({
    path: `/post/${post.id}`,
    query: {
      page: currentPage.value,
      size: pageSize.value
    }
  })
}

const currentUser = JSON.parse(localStorage.getItem('user'))

const handleDeletePost = async (post) => {
  if (!currentUser) {
    warning('请先登录')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除这条互助帖吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await del(`/api/help/post/${post.id}`, { username: currentUser.username })
    success('删除成功')
    loadPostsFromServer()
  } catch (err) {
    error(err.message || '删除失败')
  }
}

const showPublishPanel = () => {
  const user = getCurrentUser()
  if (!user) {
    warning('请先登录后再发布帖子')
    triggerLogin()
    return
  }
  publishVisible.value = true
  document.body.style.overflow = 'hidden'
  loadAvailablePets()
}

const closePublishPanel = () => {
  publishVisible.value = false
  document.body.style.overflow = ''
  postForm.value = {
    category: '',
    title: '',
    content: '',
    city: '',
    images: [],
    relatedPetId: null
  }
}


const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  processFiles(files)
  // 清空 input 值，允许重复选择同一文件
  event.target.value = ''
}

/**
 * 触发文件选择器
 */
const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

const handleDrop = (event) => {
  const files = Array.from(event.dataTransfer.files).filter(f => f.type.startsWith('image/'))
  processFiles(files)
}

const processFiles = (files) => {
  files.forEach(file => {
    if (file.size > 5 * 1024 * 1024) {
      warning(`${file.name} 超过5MB限制`)
      return
    }
    const reader = new FileReader()
    reader.onload = (e) => {
      postForm.value.images.push({
        file: file,
        name: file.name,
        preview: e.target.result,
        uploading: false,
        progress: 0,
        isCover: false,
        url: null
      })
    }
    reader.readAsDataURL(file)
  })
}

const removeImage = (index) => {
  postForm.value.images.splice(index, 1)
}

const setCover = (index) => {
  postForm.value.images.forEach((img, i) => {
    img.isCover = i === index
  })
}

const uploadImages = async () => {
  const uploadedUrls = []
  for (const img of postForm.value.images) {
    if (!img.url) {
      img.uploading = true
      img.progress = 0

      const formData = new FormData()
      formData.append('file', img.file)

      try {
        const response = await fetch('http://localhost:8080/api/upload/image', {
          method: 'POST',
          body: formData
        })
        const result = await response.json()
        if (result.url) {
          img.url = result.url
          uploadedUrls.push(result.url)
        }
      } catch (err) {
        error(`图片 ${img.name} 上传失败`)
      } finally {
        img.uploading = false
        img.progress = 100
      }
    } else {
      uploadedUrls.push(img.url)
    }
  }
  return uploadedUrls
}

const submitPost = async () => {
  if (!postForm.value.category || !postForm.value.title || !postForm.value.content) {
    warning('请填写完整信息')
    return
  }

  const user = getCurrentUser()
  if (!user) {
    warning('请先登录')
    return
  }

  loading.value = true
  uploadingImages.value = postForm.value.images.length > 0

  try {
    let photoUrls = []
    if (postForm.value.images.length > 0) {
      photoUrls = await uploadImages()
    }

    // 安全处理位置信息
    let locationStr = ''
    if (postForm.value.location && Array.isArray(postForm.value.location) && postForm.value.location.length > 0) {
      try {
        locationStr = postForm.value.location
            .map(code => codeToText[code] || '')
            .filter(text => text)
            .join('/')
      } catch (e) {
        console.warn('位置转换失败:', e)
        locationStr = ''
      }
    }

    // 构建请求体，空值不传
    const payload = {
      category: postForm.value.category,
      title: postForm.value.title,
      content: postForm.value.content,
      username: user.username
    }

    // 只有非空时才添加这些字段
    if (locationStr) {
      payload.city = locationStr
    }
    if (photoUrls.length > 0) {
      payload.photoUrls = JSON.stringify(photoUrls)
    }
    if (postForm.value.relatedPetId) {
      payload.relatedPetId = postForm.value.relatedPetId
    }

    console.log('发送发布请求:', payload)

    await post('/api/help/publish', payload)

    success('发布成功，请等待管理员审核')
    closePublishPanel()
    await loadPostsFromServer()
  } catch (err) {
    console.error('发布失败详情:', err)
    error(err.message || '发布失败')
  } finally {
    loading.value = false
    uploadingImages.value = false
  }
}

</script>

<style scoped>
.help-post-page {
  background: #FDF8F3;
  min-height: 100vh;
  padding: 20px;
}

/* 发布按钮居中容器 */
.publish-btn-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 28px;
}

.publish-btn-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 28px;
}

.publish-btn {
  background: #E8836A;
  border: none;
  border-radius: 12px;
  padding: 14px 48px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  box-shadow: 0 4px 12px rgba(232, 131, 106, 0.3);
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.publish-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #05e85dec 0%, #05e85dec  50%, #e83105ec 50%, #e83105ec 100%);
  background-size: 300% 300%;
  background-position: 0% 0%;
  transition: background-position 0.6s ease;
  z-index: -1;
}

.publish-btn:hover::before {
  background-position: 100% 100%;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(232, 131, 106, 0.4);
}

.publish-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(232, 131, 106, 0.3);
}

.btn-icon {
  margin-right: 8px;
}

.filter-section {
  margin: 24px 0;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(61,64,91,0.06);
}

.search-input,
.filter-select {
  --el-input-border-radius: 20px;
  --el-select-border-radius-hover: 20px;
}

.reset-btn {
  width: 100%;
  background: linear-gradient(135deg, #81B29A 0%, #A8D5BA 100%);
  border: none;
  border-radius: 20px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(129, 178, 154, 0.3);
}

.post-col {
  margin-bottom: 28px;
}

.post-card {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 20px;
  cursor: pointer;
  height: 100%;
  border: none;
  background: white;
  box-shadow: 0 4px 16px rgba(61,64,91,0.06);
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.post-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(61,64,91,0.15);
}

/* 帖子封面图 */
.post-cover {
  position: relative;
  width: 100%;
  height: 200px;
  min-height: 200px;
  overflow: hidden;
  background: #F3F4F6;
  flex-shrink: 0;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.post-card:hover .post-cover img {
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.post-card:hover .cover-overlay {
  opacity: 1;
}

.view-hint {
  color: white;
  font-size: 16px;
  font-weight: 600;
  background: rgba(224, 122, 95, 0.9);
  padding: 8px 20px;
  border-radius: 20px;
}

/* 帖子内容区 */
.post-content-wrapper {
  padding: 20px 24px 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.category-tag {
  border-radius: 15px;
  border: none;
  padding: 5px 14px;
  font-weight: 500;
  font-size: 13px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.post-time {
  color: #9CA3AF;
  font-size: 13px;
  flex: 1;
}

.delete-btn {
  font-size: 13px;
}

.post-title {
  font-size: 18px;
  margin: 0 0 14px 0;
  line-height: 1.5;
  color: #3D405B;
  font-weight: 700;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 54px;
}

.post-content {
  color: #6B7280;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 18px;
  font-size: 14px;
  flex: 1;
  min-height: 71px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  color: #9CA3AF;
  font-size: 13px;
  padding-top: 16px;
  border-top: 1px solid #F3F4F6;
  margin-top: auto;
}

.publish-drawer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
}

.drawer-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.drawer-content {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 90vw;
  max-width: 1200px;
  background: #FDF8F3;
  box-shadow: -8px 0 30px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  animation: slideInRight 0.4s ease-out;
}

@keyframes slideInRight {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

.drawer-header {
  padding: 24px 32px;
  background: white;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.drawer-title {
  font-size: 22px;
  font-weight: 700;
  color: #3D405B;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #9CA3AF;
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #F3F4F6;
  color: #3D405B;
}

.drawer-body {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 32px;
  padding: 32px;
  overflow-y: auto;
}

.upload-section,
.form-section {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(61,64,91,0.06);
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #3D405B;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #F3F4F6;
}

.upload-area {
  border: 2px dashed #D1D5DB;
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  transition: all 0.3s;
  background: #FAFAFA;
}

.upload-area:hover {
  border-color: #E07A5F;
  background: #FFF5F0;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 16px;
  color: #3D405B;
  font-weight: 600;
  margin: 0;
}

.upload-hint {
  font-size: 13px;
  color: #9CA3AF;
  margin: 0;
}

.select-btn {
  margin-top: 12px;
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  border: none;
  border-radius: 20px;
  padding: 10px 24px;
}

.image-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.action-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  transition: all 0.2s;
}

.action-btn:hover {
  transform: scale(1.1);
}

.action-btn.delete:hover {
  background: #FEE2E2;
  color: #EF4444;
}

.cover-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: linear-gradient(135deg, #E07A5F, #F2CC8F);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.upload-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #E07A5F, #F2CC8F);
  transition: width 0.3s;
}

.add-more {
  aspect-ratio: 1;
  border: 2px dashed #D1D5DB;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #FAFAFA;
}

.add-more:hover {
  border-color: #E07A5F;
  background: #FFF5F0;
}

.add-icon {
  font-size: 32px;
  color: #D1D5DB;
}

.add-more p {
  margin: 0;
  font-size: 12px;
  color: #9CA3AF;
}

.form-section {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(61,64,91,0.06);
}

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-item {
  margin-bottom: 0;
}

.form-item :deep(.el-form-item__label) {
  font-weight: 600;
  color: #3D405B;
  font-size: 15px;
}

.tag-cloud {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-item {
  padding: 10px 20px;
  border-radius: 20px;
  border: 2px solid #E5E7EB;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #6B7280;
  font-weight: 500;
}

.tag-item:hover {
  border-color: #E07A5F;
  color: #E07A5F;
  transform: translateY(-2px);
}

.tag-item.active {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(224, 122, 95, 0.3);
}

.title-input,
.content-input,
.city-select {
  width: 100%;
}

.title-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 16px;
}

.content-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 14px 16px;
  line-height: 1.7;
}

.drawer-footer {
  padding: 24px 32px;
  background: white;
  border-top: 1px solid #E5E7EB;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  flex-shrink: 0;
}

.cancel-btn {
  border-radius: 20px;
  padding: 10px 28px;
}

.submit-btn {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  border: none;
  border-radius: 20px;
  padding: 10px 32px;
  font-weight: 600;
  font-size: 15px;
  box-shadow: 0 4px 15px rgba(224, 122, 95, 0.3);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(224, 122, 95, 0.4);
}

.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  padding: 24px 0;
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
  border-radius: 10px;
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
  background-color: #FFF5F0;
  color: #E07A5F;
}

.custom-pagination :deep(.btn-prev),
.custom-pagination :deep(.btn-next) {
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  border-radius: 10px;
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

.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.4s;
}

.drawer-fade-enter-active .drawer-content,
.drawer-fade-leave-active .drawer-content {
  transition: transform 0.4s ease-out;
}

.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}

.drawer-fade-enter-from .drawer-content,
.drawer-fade-leave-to .drawer-content {
  transform: translateX(100%);
}

@media (max-width: 768px) {
  .drawer-body {
    grid-template-columns: 1fr;
  }
  .drawer-content {
    width: 100vw;
  }
}
</style>
