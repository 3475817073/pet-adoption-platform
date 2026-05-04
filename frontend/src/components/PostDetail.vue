<template>
  <div class="post-detail-page">
    <!-- 顶部导航 -->
    <div class="nav-bar">
      <button class="back-btn" @click="goBack">
        <span class="icon">←</span> 返回互助交流
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 帖子内容 -->
    <div v-else-if="postData" class="main-container">

      <!-- 1. 帖子文章区 -->
      <article class="article-card">
        <!-- 作者信息头 -->
        <header class="author-header">
          <div class="author-left">
            <div class="avatar-wrapper">
              <div class="avatar">{{ postData.user?.username?.[0] || 'U' }}</div>
              <span class="badge" title="资深救助者">🏅</span>
            </div>
            <div class="author-meta">
              <div class="name-row">
                <span class="name">{{ postData.user?.username || '匿名' }}</span>
                <span class="badge-text">资深救助</span>
              </div>
              <span class="time">{{ formatDateTime(postData.createTime) }}</span>
            </div>
          </div>
          <el-button class="follow-btn" round size="small">
            + 关注
          </el-button>
        </header>

        <!-- 标题与标签 -->
        <div class="post-head">
          <h1 class="title">{{ postData.title }}</h1>
          <div class="tags">
            <span class="tag-item category-tag">{{ postData.category }}</span>
            <span v-if="postData.city" class="tag-item location-tag">  {{ postData.city }}</span>
            <span class="tag-item status-tag"># 待领养</span>
          </div>
        </div>

        <!-- 图片展示区域 -->
        <div v-if="postImages && postImages.length > 0" class="post-images">
          <div
              v-for="(img, index) in postImages"
              :key="index"
              class="image-wrapper"
              :class="{ 'single-image': postImages.length === 1 }"
          >
            <img :src="getImageUrl(img)" :alt="`图片${index + 1}`" />
          </div>
        </div>

        <!-- 正文内容 -->
        <div class="content-body">
          <p class="text">{{ postData.content }}</p>
        </div>

        <!-- 关联宠物（轻量化展示） -->
        <div v-if="postData.relatedPet" class="related-pet-inline" @click="goToPetDetail">
          <div class="pet-thumb">
            <img v-if="getPetImage(postData.relatedPet)" :src="getPetImage(postData.relatedPet)" />
            <div v-else class="placeholder">🐾</div>
          </div>
          <div class="pet-info">
            <div class="pet-name">相关宠物：{{ postData.relatedPet.name }}</div>
            <div class="pet-desc">{{ postData.relatedPet.type }} · {{ postData.relatedPet.age }}岁</div>
          </div>
          <span class="arrow">→</span>
        </div>
      </article>
    </div>

    <el-empty v-else description="帖子走丢了" :image-size="150" />

    <!-- 底部固定功能栏（CSDN风格） -->
    <div v-if="postData" class="bottom-toolbar">
      <div class="toolbar-inner">
        <button class="toolbar-btn" @click="handleLike">
          <span class="icon">👍</span>
          <span class="label">点赞 {{ likeCount }}</span>
        </button>
        <button class="toolbar-btn" @click="handleCollect">
          <span class="icon">⭐</span>
          <span class="label">收藏</span>
        </button>
        <button class="toolbar-btn active" @click="showCommentPanel">
          <span class="icon">💬</span>
          <span class="label">评论 {{ totalComments }}</span>
        </button>
        <button class="toolbar-btn" @click="handleShare">
          <span class="icon">🔄</span>
          <span class="label">分享</span>
        </button>
      </div>
    </div>

    <!-- 右侧抽屉式评论区 -->
    <transition name="slide-drawer">
      <div v-if="commentPanelVisible" class="comment-drawer">
        <!-- 遮罩层 -->
        <div class="drawer-overlay" @click="closeCommentPanel"></div>

        <!-- 评论区主体 -->
        <div class="drawer-content">
          <!-- 抽屉头部 -->
          <div class="drawer-header">
            <h3 class="drawer-title">评论区 ({{ totalComments }})</h3>
            <button class="close-btn" @click="closeCommentPanel"></button>
          </div>

          <!-- 评论列表（独立滚动） -->
          <div class="drawer-body" v-loading="commentsLoading">
            <div v-if="comments.length === 0" class="empty-state">
              暂无评论，快来抢沙发吧~
            </div>

            <div v-for="(comment, index) in comments" :key="comment.id" class="comment-item">
              <!-- 一级评论头像 -->
              <div class="comment-avatar">{{ comment.user[0] }}</div>

              <div class="comment-body">
                <div class="comment-meta">
                  <span class="user-name">{{ comment.user }}</span>
                  <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>

                <div class="comment-actions">
                  <span class="action-link" @click="showMainReplyBox(comment)">回复</span>
                  <span v-if="canDelete(comment.user)" class="action-link danger" @click="deleteMainComment(comment.id)">删除</span>
                </div>

                <!-- 回复输入框 -->
                <div v-if="replyBoxIndex === index && replyBoxType === 'main'" class="reply-box">
                  <el-input v-model="replyText" placeholder="回复..." size="small" @keyup.ctrl.enter="submitReplyToMain(comment.id)" @keyup.enter="submitReplyToMain(comment.id)">
                    <template #append>
                      <el-button @click="submitReplyToMain(comment.id)" :disabled="!replyText.trim()">发送</el-button>
                    </template>
                  </el-input>
                </div>

                <!-- 二级回复列表（小红书风格） -->
                <div v-if="comment.replies && comment.replies.length > 0" class="replies-container">
                  <div v-for="reply in getVisibleReplies(comment, index)" :key="reply.id" class="reply-item">
                    <div class="reply-meta">
                      <span class="user-name">{{ reply.user }}</span>
                      <span v-if="reply.replyTo" class="reply-to">回复 <span class="target-name">{{ reply.replyTo }}</span></span>
                      <span class="comment-time">{{ formatCommentTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-text">{{ reply.content }}</div>

                    <div class="reply-actions">
                      <span class="action-link" @click="showReplyReplyBox(index, reply)">回复</span>
                      <span v-if="canDelete(reply.user)" class="action-link danger" @click="deleteReply(reply.id)">删除</span>
                    </div>

                    <!-- 二级回复的输入框 -->
                    <div v-if="replyBoxIndex === index && replyBoxReplyId === reply.id && replyBoxType === 'reply'" class="reply-box">
                      <el-input v-model="replyText" placeholder="回复..." size="small" @keyup.ctrl.enter="submitReplyToReply(comment.id, reply.id)" @keyup.enter="submitReplyToReply(comment.id, reply.id)">
                        <template #append>
                          <el-button @click="submitReplyToReply(comment.id, reply.id)" :disabled="!replyText.trim()">发送</el-button>
                        </template>
                      </el-input>
                    </div>

                    <!-- 展开/收起 -->
                    <div v-if="comment.replies.length > 3" class="expand-btn" @click="toggleExpand(index)">
                      {{ expandedReplies[index] ? '收起回复' : `查看全部 ${comment.replies.length} 条回复` }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 抽屉底部：评论输入框（固定） -->
            <div class="drawer-footer">
              <div v-if="isLoggedIn" class="comment-input-box">
                <el-input
                    v-model="newComment"
                    type="textarea"
                    :rows="2"
                    placeholder="写下你的想法..."
                    resize="none"
                    @keyup.ctrl.enter="submitComment"
                />
                <div class="input-footer">
                  <span class="tip">按 Ctrl + Enter 发送</span>
                  <el-button type="primary" :disabled="!newComment.trim()" @click="submitComment" class="submit-btn">
                    发送
                  </el-button>
                </div>
              </div>


              <!-- 未登录提示 -->
              <div v-else class="login-prompt">
                <div class="prompt-icon"></div>
                <div class="prompt-content">
                  <div class="prompt-title">登录后参与互动</div>
                  <div class="prompt-desc">登录后即可发表评论、点赞收藏</div>
                </div>
                <el-button type="primary" @click="triggerLogin" class="login-btn">
                  立即登录
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
import { ref, computed, onMounted, inject, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { get, post, del } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'

const route = useRoute()
const router = useRouter()
const triggerLogin = inject('triggerLogin')

const postData = ref(null)
const loading = ref(true)
const comments = ref([])
const commentsLoading = ref(false)
const newComment = ref('')
const isLoggedIn = ref(false)
const postImages = ref([])

const replyBoxIndex = ref(-1)
const replyBoxReplyId = ref(-1)
const replyBoxType = ref('')
const replyText = ref('')
const expandedReplies = ref({})

// 评论区抽屉状态
const commentPanelVisible = ref(false)

// 点赞收藏（预留功能）
const likeCount = ref(27)
const handleLike = () => { warning('点赞功能开发中') }
const handleCollect = () => { warning('收藏功能开发中') }
const handleShare = () => { warning('分享功能开发中') }

const totalComments = computed(() => {
  let count = 0
  comments.value.forEach(c => {
    count += 1
    if (c.replies) count += c.replies.length
  })
  return count
})

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

const checkLoginStatus = () => {
  const wasLoggedIn = isLoggedIn.value
  isLoggedIn.value = !!localStorage.getItem('user')

  // 如果从未登录变为已登录，重新加载评论区
  if (!wasLoggedIn && isLoggedIn.value) {
    loadComments()
  }
}

// 监听 localStorage 变化，实时更新登录状态
const handleStorageChange = (e) => {
  if (e.key === 'user') {
    checkLoginStatus()
  }
}

const canDelete = (commentUser) => {
  const user = getCurrentUser()
  return isLoggedIn.value && user && commentUser === user.username
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatCommentTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const getPetImage = (pet) => {
  if (!pet) return null
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
  return null
}

const getImageUrl = (url) => {
  if (!url) return ''
  return url.startsWith('http') ? url : 'http://localhost:8080' + url
}

const loadPostDetail = async () => {
  loading.value = true
  try {
    postData.value = await get(`/api/help/${route.params.id}`)

    // 解析图片URL列表
    if (postData.value.photoUrls) {
      try {
        postImages.value = JSON.parse(postData.value.photoUrls)
      } catch (e) {
        // 如果是单个字符串而不是JSON数组
        postImages.value = [postData.value.photoUrls]
      }
    }
  } catch (err) {
    error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  commentsLoading.value = true
  try {
    comments.value = await get(`/api/help/comments/${route.params.id}`)
  } catch {
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return warning('请输入内容')
  if (!isLoggedIn.value) { warning('请先登录'); triggerLogin(); return }

  try {
    await post('/api/help/comment', { postId: postData.value.id, username: getCurrentUser().username, content: newComment.value })
    success('评论成功')
    newComment.value = ''
    await loadComments()
  } catch (err) { error('评论失败') }
}

const submitReply = async (commentId) => {
  if (!replyText.value.trim()) return warning('请输入回复内容')
  if (!isLoggedIn.value) { warning('请先登录'); triggerLogin(); return }

  try {
    await post('/api/help/comment', { postId: postData.value.id, username: getCurrentUser().username, content: replyText.value, parentId: commentId })
    success('回复成功')
    replyText.value = ''
    replyBoxIndex.value = -1
    replyBoxReplyId.value = -1
    replyBoxType.value = ''
    await loadComments()
  } catch (err) { error('回复失败') }
}

const showMainReplyBox = (comment) => {
  replyBoxIndex.value = comments.value.indexOf(comment)
  replyBoxType.value = 'main'
  replyBoxReplyId.value = -1
  replyText.value = ''
}

const showReplyReplyBox = (index, reply) => {
  replyBoxIndex.value = index
  replyBoxType.value = 'reply'
  replyBoxReplyId.value = reply.id
  replyText.value = ''
}

const submitReplyToMain = (id) => submitReply(id)
const submitReplyToReply = (cId, rId) => submitReply(cId)

const getVisibleReplies = (comment, index) => {
  if (!comment.replies) return []
  return expandedReplies.value[index] ? comment.replies : comment.replies.slice(0, 3)
}

const toggleExpand = (index) => { expandedReplies.value[index] = !expandedReplies.value[index] }

const deleteComment = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await del(`/api/help/comment/${id}?username=${getCurrentUser().username}`)
    success('已删除')
    await loadComments()
  } catch {}
}
const deleteMainComment = (id) => deleteComment(id)
const deleteReply = (id) => deleteComment(id)

const goBack = () => {
  const page = route.query.page || 1
  const size = route.query.size || 10
  router.push({
    path: '/help',
    query: { page, size }
  })
}

const goToPetDetail = () => {
  if (postData.value?.relatedPet)
    router.push(`/pet/${postData.value.relatedPet.id}`)
}

// 评论区抽屉控制
const showCommentPanel = () => {
  commentPanelVisible.value = true
  // 防止背景滚动
  document.body.style.overflow = 'hidden'
}
const closeCommentPanel = () => {
  commentPanelVisible.value = false
  document.body.style.overflow = ''
}

onMounted(() => {
  checkLoginStatus()
  loadPostDetail()
  loadComments()

  // 监听 localStorage 变化
  window.addEventListener('storage', handleStorageChange)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<style scoped>
/* 页面背景 */
.post-detail-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 80px; /* 为底部工具栏留空间 */
  position: relative;
}

/* 顶部导航 */
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
.back-btn:hover { background: #f5f6f7; color: #333; }

/* 主容器 */
.main-container {
  max-width: 800px;
  margin: 24px auto;
  padding: 0 16px;
}

/* 1. 文章卡片 */
.article-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

/* 作者头部 */
.author-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.author-left { display: flex; align-items: center; gap: 12px; }
.avatar-wrapper { position: relative; }
.avatar {
  width: 48px; height: 48px;
  background: linear-gradient(135deg, #E07A5F, #F2CC8F);
  color: #fff;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 20px;
}
.badge {
  position: absolute; bottom: -2px; right: -2px;
  background: #fff; border-radius: 50%;
  font-size: 14px;
}
.name-row { display: flex; align-items: center; gap: 8px; }
.name { font-weight: 700; font-size: 16px; color: #222; }
.badge-text {
  font-size: 10px; color: #E07A5F;
  background: rgba(224, 122, 95, 0.1);
  padding: 2px 6px; border-radius: 4px;
}
.time { font-size: 12px; color: #999; margin-top: 2px; display: block; }
.follow-btn {
  background: #E07A5F;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 24px;
  font-weight: 600;
  font-size: 13px;
  box-shadow: 0 4px 12px rgba(224, 122, 95, 0.3);
  transition: all 0.3s ease;
}
.follow-btn:hover {
  background: #d4694f;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(224, 122, 95, 0.4);
}

/* 标题与标签 */
.title {
  font-size: 24px; font-weight: 800; color: #222;
  margin: 0 0 16px 0; line-height: 1.4;
}

/* 标签容器 - 使用 Flexbox 对齐 */
.tags {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
}

/* 统一标签样式 */
.tag-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.5;
  white-space: nowrap;
}

/* 分类标签 - 琥珀橙色 */
.category-tag {
  background: linear-gradient(135deg, rgba(224, 122, 95, 0.12), rgba(242, 204, 143, 0.12));
  border: 1.5px solid rgba(224, 122, 95, 0.4);
  color: #D4694F;
}

/* 位置标签 - 森林绿色 */
.location-tag {
  background: linear-gradient(135deg, rgba(129, 178, 154, 0.12), rgba(168, 213, 186, 0.12));
  border: 1.5px solid rgba(129, 178, 154, 0.4);
  color: #6B9E85;
}

/* 状态标签 - 灰色 */
.status-tag {
  background: #F3F4F6;
  border: 1.5px solid #E5E7EB;
  color: #6B7280;
}

/* 图片展示区域 */
.post-images {
  display: grid;
  gap: 12px;
  margin-bottom: 28px;
  border-radius: 16px;
  overflow: hidden;
}

/* 单张图片 */
.post-images .single-image {
  grid-template-columns: 1fr;
  max-width: 600px;
}

.post-images .single-image .image-wrapper {
  aspect-ratio: 16 / 9;
}

/* 2张图片 */
.post-images:has(.image-wrapper:nth-child(2)):not(:has(.image-wrapper:nth-child(3))) {
  grid-template-columns: repeat(2, 1fr);
}

/* 3张图片 */
.post-images:has(.image-wrapper:nth-child(3)):not(:has(.image-wrapper:nth-child(4))) {
  grid-template-columns: repeat(3, 1fr);
}

/* 4张图片 - 2x2网格 */
.post-images:has(.image-wrapper:nth-child(4)) {
  grid-template-columns: repeat(2, 1fr);
}

/* 5-9张图片 - 3x3网格 */
.post-images:has(.image-wrapper:nth-child(5)) {
  grid-template-columns: repeat(3, 1fr);
}

.image-wrapper {
  aspect-ratio: 1;
  overflow: hidden;
  background: #F3F4F6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.image-wrapper:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 正文 */
.content-body { margin-bottom: 32px; }
.text {
  font-size: 16px; line-height: 1.8; color: #333;
  white-space: pre-wrap; margin: 0;
}

/* 关联宠物（内嵌轻量） */
.related-pet-inline {
  display: flex; align-items: center; gap: 12px;
  background: #f9f9f9; padding: 12px;
  border-radius: 12px; margin-bottom: 24px;
  cursor: pointer; transition: all 0.2s;
}
.related-pet-inline:hover { background: #f0f0f0; transform: translateX(4px); }
.pet-thumb {
  width: 48px; height: 48px; border-radius: 8px; overflow: hidden; background: #eee;
  display: flex; align-items: center; justify-content: center;
}
.pet-thumb img { width: 100%; height: 100%; object-fit: cover; }
.pet-info { flex: 1; }
.pet-name { font-weight: 600; font-size: 14px; color: #333; }
.pet-desc { font-size: 12px; color: #999; }
.arrow { color: #ccc; font-size: 18px; }

/* ========== 底部固定工具栏（CSDN风格） ========== */
.bottom-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e8e8e8;
  box-shadow: 0 -2px 12px rgba(0,0,0,0.06);
  z-index: 999;
}
.toolbar-inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 12px 16px;
  display: flex;
  justify-content: space-around;
  align-items: center;
}
.toolbar-btn {
  background: none;
  border: none;
  padding: 8px 20px;
  border-radius: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  color: #666;
  font-size: 14px;
}
.toolbar-btn:hover {
  background: #f5f6f7;
  color: #E07A5F;
}
.toolbar-btn.active {
  background: rgba(224, 122, 95, 0.1);
  color: #E07A5F;
  font-weight: 600;
}
.toolbar-btn .icon { font-size: 18px; }
.toolbar-btn .label { font-size: 14px; }

/* ========== 右侧抽屉评论区 ========== */
.comment-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1000;
}

/* 遮罩层 */
.drawer-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
}

/* 抽屉内容 */
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

/* 抽屉头部 */
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

/* 抽屉主体（独立滚动区） */
.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

/* 评论列表 */
.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-avatar {
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
}
.comment-body { flex: 1; }
.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.user-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}
.comment-time {
  font-size: 12px;
  color: #999;
}
.comment-text {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 8px;
}

/* 评论操作 */
.comment-actions,
.reply-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}
.action-link {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}
.action-link:hover { color: #E07A5F; }
.action-link.danger:hover { color: #f56c6c; }

/* 二级回复（小红书风格） */
.replies-container {
  margin-top: 12px;
  margin-left: -12px;
  background: #f5f6f7;
  padding: 12px;
  border-radius: 12px;
}
.reply-item {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}
.reply-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}
.reply-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}
.reply-to {
  font-size: 12px;
  color: #999;
}
.target-name {
  color: #E07A5F;
  font-weight: 500;
}
.reply-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}
.expand-btn {
  font-size: 13px;
  color: #E07A5F;
  cursor: pointer;
  margin-top: 8px;
  font-weight: 500;
}
.expand-btn:hover { text-decoration: underline; }

/* 回复输入框 */
.reply-box {
  margin-top: 8px;
}

/* 抽屉底部：评论输入框（固定） */
.drawer-footer {
  padding: 16px 24px;
  border-top: 1px solid #eee;
  background: #fff;
  flex-shrink: 0;
}
.comment-input-box { margin-bottom: 0; }
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.tip { font-size: 12px; color: #999; }
.submit-btn {
  background: #E07A5F;
  border: none;
  border-radius: 20px;
  padding: 10px 28px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(224, 122, 95, 0.3);
  transition: all 0.3s ease;
}
.submit-btn:hover {
  background: #d4694f;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(224, 122, 95, 0.4);
}
.submit-btn:disabled {
  background: #d1d5db;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

/* 空状态 */
.empty-state {
  text-align: center;
  color: #999;
  padding: 60px 0;
  font-size: 14px;
}
.login-alert { margin-bottom: 0; background: #f0f9ff; border: none; }

/* 加载状态 */
.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  max-width: 800px;
  margin: 24px auto;
}

/* 美化后的未登录提示 */
.login-prompt {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(224, 122, 95, 0.08) 0%, rgba(242, 204, 143, 0.08) 100%);
  border-radius: 12px;
  border: 1px solid rgba(224, 122, 95, 0.2);
}
.prompt-icon {
  font-size: 28px;
  flex-shrink: 0;
}
.prompt-content {
  flex: 1;
}
.prompt-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}
.prompt-desc {
  font-size: 12px;
  color: #999;
}
.login-btn {
  background: #E07A5F;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 13px;
}
.login-btn:hover {
  background: #d4694f;
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
</style>
