<template>
  <div class="user-profile-page">
    <!-- 顶部导航 -->
    <div class="nav-bar">
      <button class="back-btn" @click="goBack">
        <span class="icon">←</span> 返回
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 用户信息卡片 -->
    <div v-else-if="userData" class="profile-container">
      <el-card class="user-card" shadow="hover">
        <div class="user-header">
          <div class="avatar-section">
            <div class="avatar-circle">{{ userData.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
            <div class="user-info">
              <h2 class="username">{{ userData.username }}</h2>
              <el-tag :type="getRoleColor(userData.role)" size="large">{{ getRoleText(userData.role) }}</el-tag>
              <span class="join-time">加入于 {{ formatDate(userData.createTime) }}</span>
            </div>
          </div>

          <div class="action-section">
            <!-- 如果不是当前登录用户，显示关注和私信按钮 -->
            <div v-if="!isCurrentUser" class="interaction-buttons">
              <el-button
                  :type="isFollowed ? 'default' : 'primary'"
                  @click="toggleFollow"
                  :loading="followLoading"
                  round
              >
                {{ isFollowed ? '已关注' : '+ 关注' }}
              </el-button>
              <el-button
                  type="success"
                  @click="openMessageDialog"
                  round
              >
                ✉ 私信
              </el-button>
            </div>
          </div>
        </div>

        <div class="stats-section">
          <div class="stat-item">
            <div class="stat-number">{{ publishedPets.length }}</div>
            <div class="stat-label">发布的宠物</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ publishedPosts.length }}</div>
            <div class="stat-label">发布的帖子</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ followersCount }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ followingCount }}</div>
            <div class="stat-label">关注</div>
          </div>
        </div>
      </el-card>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" type="border-card" class="content-tabs">
        <!-- 发布的宠物 -->
        <el-tab-pane label="发布的宠物" name="pets">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
                v-model="petSearchKeyword"
                placeholder="搜索宠物名称、类型..."
                clearable
                prefix-icon="Search"
                @input="handlePetSearch"
                class="search-input"
            />
          </div>

          <div v-if="filteredPets.length === 0" class="empty-state">
            <el-empty :description="petSearchKeyword ? '未找到匹配的宠物' : '该用户还没有发布宠物'" />
          </div>
          <div v-else class="pets-grid">
            <el-card
                v-for="pet in paginatedPets"
                :key="pet.id"
                class="pet-card"
                shadow="hover"
                @click="goToPetDetail(pet.id)"
            >
              <div class="pet-image">
                <el-image
                    :src="getPetImage(pet)"
                    fit="cover"
                    class="pet-img"
                />
              </div>
              <div class="pet-info">
                <h3 class="pet-name">{{ pet.name }}</h3>
                <p class="pet-desc">{{ pet.type }} · {{ pet.age }}岁 · {{ pet.gender }}</p>
              </div>
            </el-card>
          </div>

          <!-- 分页 -->
          <div v-if="filteredPets.length > 0" class="pagination-wrapper">
            <el-pagination
                v-model:current-page="petCurrentPage"
                :page-size="petPageSize"
                :total="filteredPets.length"
                layout="total, prev, pager, next"
                @current-change="handlePetPageChange"
            />
          </div>
        </el-tab-pane>

        <!-- 发布的帖子 -->
        <el-tab-pane label="发布的帖子" name="posts">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
                v-model="postSearchKeyword"
                placeholder="搜索帖子标题、内容..."
                clearable
                prefix-icon="Search"
                @input="handlePostSearch"
                class="search-input"
            />
          </div>

          <div v-if="filteredPosts.length === 0" class="empty-state">
            <el-empty :description="postSearchKeyword ? '未找到匹配的帖子' : '该用户还没有发布帖子'" />
          </div>
          <div v-else class="posts-list">
            <el-card
                v-for="post in paginatedPosts"
                :key="post.id"
                class="post-card"
                shadow="hover"
                @click="goToPostDetail(post.id)"
            >
              <div class="post-header">
                <h3 class="post-title">{{ post.title }}</h3>
                <span class="post-time">{{ formatDateTime(post.createTime) }}</span>
              </div>
              <p class="post-content">{{ post.content?.substring(0, 100) }}...</p>
              <div class="post-footer">
                <el-tag size="small">{{ post.category }}</el-tag>
                <span v-if="post.city" class="post-location">{{ post.city }}</span>
              </div>
            </el-card>
          </div>

          <!-- 分页 -->
          <div v-if="filteredPosts.length > 0" class="pagination-wrapper">
            <el-pagination
                v-model:current-page="postCurrentPage"
                :page-size="postPageSize"
                :total="filteredPosts.length"
                layout="total, prev, pager, next"
                @current-change="handlePostPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 私信侧边栏 -->
    <transition name="slide-drawer">
      <div v-if="showMessageDialog" class="message-drawer">

        <!-- 遮罩层 -->
        <div class="drawer-overlay" @click="showMessageDialog = false"></div>

        <!-- 私信区主体 -->
        <div class="drawer-content">
          <!-- 抽屉头部 -->
          <div class="drawer-header">
            <h3 class="drawer-title">与 {{ route.params.username }} 的对话</h3>
            <button class="close-btn" @click="showMessageDialog = false">✕</button>
          </div>

          <!-- 消息列表（独立滚动） -->
          <div class="drawer-body" v-loading="messagesLoading">
            <div v-if="messages.length === 0" class="empty-state">
              暂无消息，开始对话吧~
            </div>

            <div v-for="msg in messages" :key="msg.id"
                 class="message-item"
                 :class="{ 'message-self': msg.sender.username === currentUser?.username }">
              <!-- 对方的消息 -->
              <template v-if="msg.sender.username !== currentUser?.username">
                <div class="message-avatar">
                  {{ msg.sender.username.charAt(0).toUpperCase() }}
                </div>
                <div class="message-bubble">
                  <div class="message-sender">{{ msg.sender.username }}</div>
                  <div v-if="msg.content" class="message-text">{{ msg.content }}</div>
                  <div v-if="msg.imageUrl" class="message-image">
                    <img :src="getImageUrl(msg.imageUrl)" alt="消息图片" @click="previewImage(msg.imageUrl)" />
                  </div>
                  <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
                </div>
              </template>

              <!-- 自己的消息 -->
              <template v-else>
                <div class="message-bubble">
                  <div class="message-sender">{{ msg.sender.username }}</div>
                  <div v-if="msg.content" class="message-text">{{ msg.content }}</div>
                  <div v-if="msg.imageUrl" class="message-image">
                    <img :src="getImageUrl(msg.imageUrl)" alt="消息图片" @click="previewImage(msg.imageUrl)" />
                  </div>
                  <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
                </div>
                <div class="message-avatar">
                  {{ msg.sender.username.charAt(0).toUpperCase() }}
                </div>
              </template>
            </div>
          </div>

          <!-- 抽屉底部：消息输入框（固定） -->
          <div class="drawer-footer">
            <!-- 图片预览区 -->
            <div v-if="previewImageUrl" class="image-preview">
              <img :src="getImageUrl(previewImageUrl)" alt="预览图片" />
              <button class="remove-image-btn" @click="removePreviewImage">✕</button>
            </div>

            <div class="message-input-box">
              <el-input
                  v-model="messageContent"
                  type="textarea"
                  :rows="2"
                  placeholder="输入消息..."
                  resize="none"
                  @keyup.ctrl.enter="sendMessage"
              />
              <div class="input-footer">
                <div class="input-tools">
                  <label class="upload-image-btn" :class="{ 'has-image': previewImageUrl }">
                    +
                    <input
                        type="file"
                        accept="image/*"
                        @change="handleImageSelect"                      style="display: none"
                    />
                  </label>
                  <span class="tip">按 Ctrl + Enter 发送</span>
                </div>
                <el-button
                    type="primary"
                    :disabled="!messageContent.trim() && !previewImageUrl"
                    @click="sendMessage"
                    class="send-btn"
                    :loading="messageLoading"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>


  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'
import defaultPetImage from '../assets/pets-banner.png'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const userData = ref(null)
const publishedPets = ref([])
const publishedPosts = ref([])
const isCurrentUser = ref(false)
const isFollowed = ref(false)
const followLoading = ref(false)
const followersCount = ref(0)
const followingCount = ref(0)
const activeTab = ref('pets')
const showMessageDialog = ref(false)
const messageContent = ref('')
const messageLoading = ref(false)
const messagesLoading = ref(false)
const messages = ref([])
const currentUser = ref(null)
const previewImageUrl = ref('')
const uploadedImageUrl = ref(null)
const selectedImageFile = ref(null)
const uploadingImage = ref(false)

// 宠物分页和搜索
const petSearchKeyword = ref('')
const petCurrentPage = ref(1)
const petPageSize = 12

// 帖子分页和搜索
const postSearchKeyword = ref('')
const postCurrentPage = ref(1)
const postPageSize = 10


/**
 * 获取当前登录用户
 */
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try {
    currentUser.value = JSON.parse(userStr)
    return currentUser.value
  } catch {
    return null
  }
}

/**
 * 加载用户数据
 */
const loadUserData = async () => {
  loading.value = true
  const username = route.params.username

  try {
    // 获取用户基本信息
    const userResponse = await get(`/api/user/profile/${username}`)
    userData.value = userResponse

    // 检查是否是当前登录用户
    const currentUser = getCurrentUser()
    isCurrentUser.value = currentUser && currentUser.username === username

    // 加载粉丝和关注数量
    const followersRes = await get('/api/follow/followers/count', { username })
    followersCount.value = followersRes.count

    const followingRes = await get('/api/follow/following/count', { username })
    followingCount.value = followingRes.count

    // 如果不是当前用户，检查是否已关注
    if (!isCurrentUser.value && currentUser) {
      const followCheck = await get('/api/follow/check', {
        followerUsername: currentUser.username,
        followingUsername: username
      })
      isFollowed.value = followCheck.followed
    }


    // 加载用户发布的宠物（只显示已审核通过的）
    const petsResponse = await get('/api/pet/my-pets', {
      username: username,
      page: 0,
      size: 50
    })
    // 过滤只显示审核通过的宠物
    publishedPets.value = (petsResponse.content || []).filter(pet =>
        pet.reviewStatus === 'APPROVED'
    )

    // 加载用户发布的帖子（只显示已审核通过的）
    const postsResponse = await get('/api/help/my-posts', {
      username: username,
      page: 0,
      size: 50
    })
    // 过滤只显示审核通过的帖子
    publishedPosts.value = (postsResponse.content || []).filter(post =>
        post.status === 'APPROVED'
    )


  } catch (err) {
    error('加载用户信息失败')
    console.error(err)
  } finally {
    loading.value = false
  }
}

/**
 * 切换关注状态
 */
const toggleFollow = async () => {
  if (!getCurrentUser()) {
    warning('请先登录')
    return
  }

  followLoading.value = true
  const currentUser = getCurrentUser()

  try {
    const response = await post('/api/follow/toggle', {
      followerUsername: currentUser.username,
      followingUsername: route.params.username
    })

    isFollowed.value = response.followed
    followersCount.value = response.followersCount

    if (isFollowed.value) {
      success('关注成功')
    } else {
      success('已取消关注')
    }
  } catch (err) {
    error(err.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

/**
 * 打开私信对话框并加载消息
 */
const openMessageDialog = async () => {
  const user = getCurrentUser()
  console.log('当前用户:', user)

  if (!user) {
    warning('请先登录')
    return
  }

  showMessageDialog.value = true
  previewImageUrl.value = ''
  selectedImageFile.value = null
  uploadedImageUrl.value = null
  uploadingImage.value = false
  messageContent.value = ''
  await loadMessages()
}


/**
 * 加载消息记录
 */
const loadMessages = async () => {
  messagesLoading.value = true
  const currentUser = getCurrentUser()

  try {
    const data = await get('/api/message/conversation', {
      username1: currentUser.username,
      username2: route.params.username,
      page: 0,
      size: 50
    })
    // 反转消息顺序，让最新的在底部
    messages.value = (data.content || []).reverse()
  } catch (err) {
    console.error('加载消息失败:', err)
    messages.value = []
  } finally {
    messagesLoading.value = false
  }
}

/**
 * 发送私信
 */
const sendMessage = async () => {
  if (!messageContent.value.trim() && !previewImageUrl.value) {
    warning('请输入消息内容或选择图片')
    return
  }

  messageLoading.value = true
  const user = getCurrentUser()

  try {
    const response = await post('/api/message/send', {
      senderUsername: user.username,
      receiverUsername: route.params.username,
      content: messageContent.value.trim() || null,
      imageUrl: uploadedImageUrl.value || null
    })

    // 将新消息添加到列表
    messages.value.push(response)
    messageContent.value = ''
    previewImageUrl.value = ''
    selectedImageFile.value = null
    uploadedImageUrl.value = null

    // 滚动到底部
    await nextTick()
    scrollToBottom()

    success('消息发送成功')
  } catch (err) {
    console.error('发送消息失败:', err)
    console.error('错误详情:', err.message)
    error(err.message || '发送失败')
  } finally {
    messageLoading.value = false
  }
}

/**
 * 处理图片选择
 */
const handleImageSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    warning('请选择图片文件')
    return
  }

  // 验证文件大小（限制10MB）
  if (file.size > 10 * 1024 * 1024) {
    warning('图片大小不能超过10MB')
    return
  }

  selectedImageFile.value = file
  uploadingImage.value = true

  try {
    // 先上传到服务器获取URL
    const uploadResult = await uploadImageToServer(file)
    uploadedImageUrl.value = uploadResult.url
    previewImageUrl.value = uploadResult.url
  } catch (err) {
    console.error('图片上传失败:', err)
    error(err.message || '图片上传失败')
    selectedImageFile.value = null
    previewImageUrl.value = ''
  } finally {
    uploadingImage.value = false
    event.target.value = ''
  }
}

/**
 * 上传图片到服务器
 */
const uploadImageToServer = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('http://localhost:8080/api/upload/image', {
    method: 'POST',
    body: formData
  })

  if (!response.ok) {
    const errorMsg = await response.text()
    throw new Error(errorMsg || '上传失败')
  }

  return await response.json()
}

/**
 * 处理图片URL（补全相对路径）
 */
const getImageUrl = (url) => {
  if (!url) return ''
  return url.startsWith('http') ? url : 'http://localhost:8080' + url
}

/**
 * 预览图片
 */
const previewImage = (imageUrl) => {
  window.open(getImageUrl(imageUrl), '_blank')
}


/**
 * 移除预览图片
 */
const removePreviewImage = () => {
  previewImageUrl.value = ''
  selectedImageFile.value = null
  uploadedImageUrl.value = null
}


/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  const drawerBody = document.querySelector('.drawer-body')
  if (drawerBody) {
    drawerBody.scrollTop = drawerBody.scrollHeight
  }
}

/**
 * 宠物搜索过滤
 */
const filteredPets = computed(() => {
  if (!petSearchKeyword.value) return publishedPets.value

  const keyword = petSearchKeyword.value.toLowerCase()
  return publishedPets.value.filter(pet =>
      pet.name?.toLowerCase().includes(keyword) ||
      pet.type?.toLowerCase().includes(keyword) ||
      pet.gender?.toLowerCase().includes(keyword)
  )
})

/**
 * 帖子搜索过滤
 */
const filteredPosts = computed(() => {
  if (!postSearchKeyword.value) return publishedPosts.value

  const keyword = postSearchKeyword.value.toLowerCase()
  return publishedPosts.value.filter(post =>
      post.title?.toLowerCase().includes(keyword) ||
      post.content?.toLowerCase().includes(keyword) ||
      post.category?.toLowerCase().includes(keyword)
  )
})

/**
 * 宠物分页数据
 */
const paginatedPets = computed(() => {
  const start = (petCurrentPage.value - 1) * petPageSize
  const end = start + petPageSize
  return filteredPets.value.slice(start, end)
})

/**
 * 帖子分页数据
 */
const paginatedPosts = computed(() => {
  const start = (postCurrentPage.value - 1) * postPageSize
  const end = start + postPageSize
  return filteredPosts.value.slice(start, end)
})

/**
 * 宠物搜索处理
 */
const handlePetSearch = () => {
  petCurrentPage.value = 1
}

/**
 * 帖子搜索处理
 */
const handlePostSearch = () => {
  postCurrentPage.value = 1
}

/**
 * 宠物分页变化
 */
const handlePetPageChange = (page) => {
  petCurrentPage.value = page
}

/**
 * 帖子分页变化
 */
const handlePostPageChange = (page) => {
  postCurrentPage.value = page
}



/**
 * 获取宠物图片
 */
const getPetImage = (pet) => {
  if (!pet) return defaultPetImage
  if (pet.photoUrl) return pet.photoUrl.startsWith('http') ? pet.photoUrl : 'http://localhost:8080' + pet.photoUrl
  if (pet.photoUrls) {
    try {
      const photos = JSON.parse(pet.photoUrls)
      if (photos && photos.length > 0) {
        const url = photos[0]
        return url.startsWith('http') ? url : 'http://localhost:8080' + url
      }
    } catch {}
  }
  // 如果没有图片，返回默认图片
  return defaultPetImage
}

/**
 * 格式化日期
 */
const formatDate = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/**
 * 格式化消息时间
 */
const formatMessageTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}


/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

/**
 * 获取角色颜色
 */
const getRoleColor = (role) => {
  const colors = { ADMIN: 'danger', USER: 'info' }
  return colors[role] || ''
}

/**
 * 获取角色文本
 */
const getRoleText = (role) => {
  const texts = { ADMIN: '管理员', USER: '普通用户' }
  return texts[role] || '未知'
}

/**
 * 跳转到宠物详情
 */
const goToPetDetail = (petId) => {
  router.push({
    path: `/pet/${petId}`,
    query: { fromUser: route.params.username }
  })
}

/**
 * 跳转到帖子详情
 */
const goToPostDetail = (postId) => {
  router.push({
    path: `/post/${postId}`,
    query: { fromUser: route.params.username }
  })
}


/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.user-profile-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 40px;
  position: relative;
}

.nav-bar {
  background: #fff;
  padding: 12px 24px;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f5f6f7;
  color: #333;
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  max-width: 1200px;
  margin: 24px auto;
}

.profile-container {
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 16px;
}

.user-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.user-header {
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

.user-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.join-time {
  font-size: 12px;
  color: #999;
}

.interaction-buttons {
  display: flex;
  gap: 12px;
}

.stats-section {
  display: flex;
  gap: 40px;
  padding: 20px;
  border-top: 1px solid #eee;
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

.content-tabs {
  border-radius: 16px;
}

.search-box {
  padding: 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.search-input {
  max-width: 400px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 20px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

.empty-state {
  padding: 60px 0;
}


.pets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 20px;
}

.pet-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.pet-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.pet-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.pet-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-info {
  padding: 16px;
}

.pet-name {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
}

.pet-desc {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.posts-list {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.post-card:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-title {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-content {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-location {
  font-size: 12px;
  color: #999;
}

/* 私信侧边栏样式 */
.message-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 3000;
}

.drawer-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
}

.drawer-content {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 500px;
  max-width: 90vw;
  background: #fff;
  box-shadow: -4px 0 20px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

.drawer-header {
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  flex-shrink: 0;
}

.drawer-title {
  font-size: 18px;
  font-weight: 700;
  color: #222;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f5f6f7;
  color: #333;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f7f8fa;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-item.message-self {
  flex-direction: row;
  justify-content: flex-end;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #E07A5F, #F2CC8F);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  flex-shrink: 0;
  order: 1;
}

.message-item.message-self .message-avatar {
  background: linear-gradient(135deg, #6EE7B7, #3B82F6);
  order: 2;
}

.message-bubble {
  max-width: 70%;
  background: #fff;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  order: 2;
}

.message-item.message-self .message-bubble {
  background: #E07A5F;
  color: #fff;
  order: 1;
}

.message-sender {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #666;
}

.message-item.message-self .message-sender {
  color: rgba(255,255,255,0.8);
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.6;
  text-align: right;
}

.message-image {
  margin-top: 8px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.message-image img {
  max-width: 100%;
  max-height: 200px;
  object-fit: cover;
  display: block;
  border-radius: 8px;
  transition: transform 0.2s;
}

.message-image img:hover {
  transform: scale(1.02);
}

.drawer-footer {
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.image-preview {
  position: relative;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e5e7eb;
}

.image-preview img {
  max-width: 100%;
  max-height: 150px;
  display: block;
  object-fit: cover;
}

.remove-image-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: background 0.2s;
}

.remove-image-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.message-input-box {
  background: white;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  overflow: hidden;
  transition: border-color 0.3s;
}

.message-input-box:focus-within {
  border-color: #E07A5F;
}

.message-input-box :deep(.el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  padding: 12px 16px;
  resize: none;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
}

.input-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-image-btn {
  cursor: pointer;
  font-size: 20px;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
  user-select: none;
}

.upload-image-btn:hover {
  background: #f3f4f6;
}

.upload-image-btn.has-image {
  background: #E07A5F;
  color: white;
}

.tip {
  font-size: 12px;
  color: #999;
}


.send-btn {
  background: #E07A5F;
  border: none;
  border-radius: 20px;
  padding: 10px 28px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(224, 122, 95, 0.3);
  transition: all 0.3s ease;
}

.send-btn:hover {
  background: #d4694f;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(224, 122, 95, 0.4);
}

.send-btn:disabled {
  background: #d1d5db;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

/* 过渡动画 */
.slide-drawer-enter-active,
.slide-drawer-leave-active {
  transition: opacity 0.3s;
}

.slide-drawer-enter-active .drawer-content,
.slide-drawer-leave-active .drawer-content {
  transition: transform 0.3s ease-out;
}

.slide-drawer-enter-from,
.slide-drawer-leave-to {
  opacity: 0;
}

.slide-drawer-enter-from .drawer-content,
.slide-drawer-leave-to .drawer-content {
  transform: translateX(100%);
}

@media (max-width: 768px) {
  .user-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .stats-section {
    flex-wrap: wrap;
    gap: 20px;
  }

  .pets-grid {
    grid-template-columns: 1fr;
  }
}
</style>
