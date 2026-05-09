<template>
  <div class="post-detail-page">
    <!-- 顶部导航 -->
    <div class="nav-bar">
      <button class="back-btn" @click="goBack">
        <span class="icon">←</span> {{ fromUserProfile ? '返回用户主页' : fromCenter ? '返回个人中心' : '返回互助交流' }}
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
            <div class="avatar-wrapper" @click="goToUserProfile" style="cursor: pointer;">
              <div class="avatar">{{ postData.user?.username?.[0] || 'U' }}</div>
              <span class="badge" title="资深救助者">🏅</span>
            </div>
            <div class="author-meta">
              <div class="name-row">
                <span class="name" @click="goToUserProfile" style="cursor: pointer;">{{ postData.user?.username || '匿名' }}</span>

              </div>
              <span class="time">{{ formatDateTime(postData.createTime) }}</span>
            </div>
          </div>
          <el-button
              class="follow-btn"
              round
              size="small"
              :type="isFollowed ? 'default' : 'primary'"
              :loading="followLoading"
              @click="toggleFollow"
          >
            {{ isFollowed ? '已关注' : '+ 关注' }}
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
            <el-image
                :src="getImageUrl(img)"
                :preview-src-list="previewPostImageList"
                :initial-index="index"
                fit="cover"
                preview-teleported
                class="post-detail-image"
            />
          </div>
        </div>

        <!-- 正文内容 -->
        <div class="content-body">
          <p class="text">{{ postData.content }}</p>
        </div>

        <!-- 关联宠物（轻量化展示） -->
        <div v-if="postData.relatedPet" class="related-pet-inline" @click="goToPetDetailFromUser">
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

    <!-- 底部固定功能栏 -->
    <div class="bottom-toolbar">
      <div class="toolbar-inner">
        <!-- 点赞按钮 -->
        <button
            class="like-button"
            :class="{ 'is-liked': isLiked }"
            @click="handleLike"
        >
      <span class="like-left">
        <svg viewBox="0 0 512 512" class="like-icon" xmlns="http://www.w3.org/2000/svg">
          <path d="M47.6 300.4L228.3 469.1c7.5 7 17.4 10.9 27.7 10.9s20.2-3.9 27.7-10.9L464.4 300.4c30.4-28.3 47.6-68 47.6-109.5v-5.8c0-69.9-50.5-129.5-119.4-141C347 36.5 300.6 51.4 268 84L256 96 244 84c-32.6-32.6-79-47.5-124.6-39.9C50.5 55.6 0 115.2 0 185.1v5.8c0 41.5 17.2 81.2 47.6 109.5z"></path>
        </svg>
        <span class="like-text">点赞</span>
      </span>
          <span class="like-count">{{ likeCount }}</span>
        </button>

        <!-- 收藏按钮 -->
        <button
            class="favorite-button"
            :class="{ 'is-favorited': isFavorited }"
            @click="handleCollect"
        >
      <span class="favorite-icon-wrapper">
        <svg class="favorite-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z"
                stroke-width="2" stroke-linejoin="round" stroke-linecap="round"></path>
        </svg>
      </span>
          <span class="favorite-text">{{ isFavorited ? '已收藏' : '收藏' }}</span>
        </button>

        <!-- 评论按钮 -->
        <button class="comment-button" @click="showCommentPanel">

          <span class="comment-text">评论</span>
          <span class="comment-count">{{ totalComments }}</span>
        </button>

        <!-- 分享按钮 -->
        <button class="share-button" @click="handleShare">
      <span class="share-icon-wrapper">
        <svg class="share-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92 1.61 0 2.92-1.31 2.92-2.92s-1.31-2.92-2.92-2.92z"/>
        </svg>
      </span>
          <span class="share-text">分享</span>
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
const isFollowed = ref(false)
const followLoading = ref(false)

const replyBoxIndex = ref(-1)
const replyBoxReplyId = ref(-1)
const replyBoxType = ref('')
const replyText = ref('')
const expandedReplies = ref({})

const fromUserProfile = ref(false)
const fromUser = ref('')
const fromCenter = ref(false)


// 评论区抽屉状态
const commentPanelVisible = ref(false)

// 点赞收藏状态
const likeCount = ref(0)
const isLiked = ref(false)
const isFavorited = ref(false)

/**
 * 加载帖子点赞数
 */
const loadLikeCount = async () => {
  if (!postData.value) return
  try {
    const data = await get('/api/interaction/like/count', {
      targetType: 'POST',
      targetId: postData.value.id
    })
    likeCount.value = data.count
  } catch (err) {
    console.error('加载点赞数失败:', err)
  }
}

/**
 * 检查当前用户是否已点赞
 */
const checkLiked = async () => {
  if (!isLoggedIn.value || !postData.value) return
  const userStr = localStorage.getItem('user')
  const user = JSON.parse(userStr)

  try {
    const data = await get('/api/interaction/like/check', {
      username: user.username,
      targetType: 'POST',
      targetId: postData.value.id
    })
    isLiked.value = data.liked
  } catch (err) {
    console.error('检查点赞状态失败:', err)
  }
}

/**
 * 检查当前用户是否已收藏
 */
const checkFavorited = async () => {
  if (!isLoggedIn.value || !postData.value) return
  const userStr = localStorage.getItem('user')
  const user = JSON.parse(userStr)

  try {
    const data = await get('/api/interaction/favorite/check', {
      username: user.username,
      targetType: 'POST',
      targetId: postData.value.id
    })
    isFavorited.value = data.favorited
  } catch (err) {
    console.error('检查收藏状态失败:', err)
  }
}

/**
 * 处理点赞操作
 */
const handleLike = async () => {
  if (!isLoggedIn.value) {
    warning('请先登录后再点赞')
    triggerLogin()
    return
  }

  const userStr = localStorage.getItem('user')
  const user = JSON.parse(userStr)

  try {
    const data = await post('/api/interaction/like/toggle', {
      username: user.username,
      targetType: 'POST',
      targetId: postData.value.id
    })

    isLiked.value = data.liked
    likeCount.value = data.count

    if (isLiked.value) {
      success('已点赞')
    } else {
      success('已取消点赞')
    }
  } catch (err) {
    error(err.message || '操作失败')
  }
}

/**
 * 处理收藏操作
 */
const handleCollect = async () => {
  if (!isLoggedIn.value) {
    warning('请先登录后再收藏')
    triggerLogin()
    return
  }

  const userStr = localStorage.getItem('user')
  const user = JSON.parse(userStr)

  try {
    const data = await post('/api/interaction/favorite/toggle', {
      username: user.username,
      targetType: 'POST',
      targetId: postData.value.id
    })

    isFavorited.value = data.favorited

    if (isFavorited.value) {
      success('收藏成功')
    } else {
      success('已取消收藏')
    }
  } catch (err) {
    error(err.message || '操作失败')
  }
}

/**
 * 处理分享操作 - 复制链接到剪贴板
 */
const handleShare = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    success('链接已复制到剪贴板，快去分享吧！')
  }).catch(() => {
    warning('复制失败，请手动复制链接')
  })
}


// 计算预览图片列表
const previewPostImageList = computed(() => {
  if (!postImages.value || postImages.value.length === 0) return []
  return postImages.value.map(img => getImageUrl(img))
})


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

  // 如果从未登录变为已登录，重新加载评论区和互动状态
  if (!wasLoggedIn && isLoggedIn.value) {
    loadComments()
    if (postData.value) {
      checkLiked()
      checkFavorited()
    }
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

    // 检查登录状态并加载互动数据
    checkLoginStatus()
    await loadLikeCount()
    await checkLiked()
    await checkFavorited()
    await checkFollowStatus()

    // 获取来源用户主页
    fromUserProfile.value = !!route.query.fromUser
    fromUser.value = route.query.fromUser || ''

    // 获取来源个人中心
    fromCenter.value = !!route.query.fromCenter

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
  // 如果来自用户主页，返回用户主页
  if (fromUserProfile.value && fromUser.value) {
    router.push({
      path: `/user/${fromUser.value}`
    })
    return
  }

  // 如果来自个人中心，返回个人中心收藏页面
  if (fromCenter.value) {
    router.push({
      path: '/center',
      query: { tab: 'favorites' }
    })
    return
  }

  const page = route.query.page || 1
  const size = route.query.size || 10
  router.push({
    path: '/help',
    query: { page, size }
  })
}


const goToPetDetail = () => {
  if (postData.value?.relatedPet)
    router.push({
      path: `/pet/${postData.value.relatedPet.id}`,
      query: { fromPostId: postData.value.id }
    })
}

/**
 * 检查是否已关注
 */
const checkFollowStatus = async () => {
  if (!isLoggedIn.value || !postData.value?.user?.username) return
  const userStr = localStorage.getItem('user')
  const user = JSON.parse(userStr)

  try {
    const data = await get('/api/follow/check', {
      followerUsername: user.username,
      followingUsername: postData.value.user.username
    })
    isFollowed.value = data.followed
  } catch (err) {
    console.error('检查关注状态失败:', err)
  }
}

/**
 * 切换关注状态
 */
const toggleFollow = async () => {
  if (!isLoggedIn.value) {
    warning('请先登录后再关注')
    triggerLogin()
    return
  }

  const userStr = localStorage.getItem('user')
  const user = JSON.parse(userStr)

  followLoading.value = true
  try {
    const data = await post('/api/follow/toggle', {
      followerUsername: user.username,
      followingUsername: postData.value.user.username
    })

    isFollowed.value = data.followed

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
 * 跳转到用户主页
 */
const goToUserProfile = () => {
  if (postData.value?.user?.username) {
    router.push(`/user/${postData.value.user.username}`)
  }
}

/**
 * 跳转到关联宠物详情，标记来源为当前用户主页
 */
const goToPetDetailFromUser = () => {
  if (postData.value?.relatedPet) {
    router.push({
      path: `/pet/${postData.value.relatedPet.id}`,
      query: {
        fromPostId: postData.value.id,
        fromUser: fromUser.value || postData.value.user?.username
      }
    })
  }
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
  loadPostDetail()
  loadComments()

  // 监听登录状态变化
  window.addEventListener('storage', (e) => {
    if (e.key === 'user') {
      checkLoginStatus()
      if (isLoggedIn.value && postData.value) {
        checkLiked()
        checkFavorited()
        checkFollowStatus()
      }
    }
  })
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

.post-detail-image {
  width: 100%;
  height: 100%;
  display: block;
}

.post-detail-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: zoom-in;
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

/* ========== 底部固定工具栏 ========== */
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
  gap: 12px;
}

/* 点赞按钮样式 */
.like-button {
  width: 140px;
  height: 38px;
  display: flex;
  align-items: center;
  border: none;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  background-color: transparent;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.like-button .like-left {
  width: 65%;
  height: 100%;
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.3s ease;
}

.like-button .like-icon {
  width: 18px;
  height: 18px;
  fill: white;
  transition: transform 0.2s ease;
}

.like-button .like-text {
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.like-button .like-count {
  width: 35%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #E07A5F;
  font-weight: 600;
  font-size: 14px;
  position: relative;
  background-color: white;
}

.like-button .like-count::before {
  content: "";
  position: absolute;
  left: -6px;
  width: 12px;
  height: 12px;
  background-color: white;
  transform: rotate(45deg);
}

.like-button:hover .like-left {
  background: linear-gradient(135deg, #d4694f 0%, #e5b87a 100%);
}

.like-button:active .like-left .like-icon {
  transform: scale(1.2);
  transform-origin: center;
}

/* 点赞激活状态 */
.like-button.is-liked .like-left {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.like-button.is-liked .like-count {
  color: #ff6b6b;
}

/* 收藏按钮样式 */
.favorite-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 25px;
  border: 2px solid #F2CC8F;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.favorite-button .favorite-icon-wrapper {
  position: relative;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-button .favorite-icon {
  width: 20px;
  height: 20px;
  stroke: #F2CC8F;
  fill: none;
  transition: all 0.5s ease;
}

.favorite-button .favorite-text {
  font-weight: 600;
  font-size: 14px;
  color: #F2CC8F;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.favorite-button:hover {
  border-color: #e5b87a;
  background: linear-gradient(135deg, rgba(242, 204, 143, 0.1) 0%, rgba(224, 122, 95, 0.05) 100%);
}

.favorite-button:hover .favorite-icon {
  stroke: #e5b87a;
  transform: scale(1.1);
}

/* 收藏激活状态 */
.favorite-button.is-favorited {
  border-color: #F2CC8F;
  background: linear-gradient(135deg, #F2CC8F 0%, #e5b87a 100%);
}

.favorite-button.is-favorited .favorite-icon {
  stroke: white;
  fill: white;
  animation: favoritePop 0.5s ease;
}

.favorite-button.is-favorited .favorite-text {
  color: white;
}

@keyframes favoritePop {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.4);
  }
  100% {
    transform: scale(1);
  }
}

/* 评论按钮样式 */
.comment-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 25px;
  border: 2px solid #999;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.comment-button .comment-icon {
  font-size: 18px;
}

.comment-button .comment-text {
  font-weight: 600;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.comment-button .comment-count {
  font-size: 13px;
  color: #999;
  font-weight: 600;
}

.comment-button:hover {
  border-color: #E07A5F;
  background: linear-gradient(135deg, rgba(224, 122, 95, 0.08) 0%, rgba(242, 204, 143, 0.05) 100%);
}

.comment-button:hover .comment-text {
  color: #E07A5F;
}

/* 分享按钮样式 */
.share-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 25px;
  border: 2px solid #999;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.share-button .share-icon-wrapper {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.share-button .share-icon {
  width: 18px;
  height: 18px;
  fill: #999;
  transition: all 0.3s ease;
}

.share-button .share-text {
  font-weight: 600;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.share-button:hover {
  border-color: #66BB6A;
  background: linear-gradient(135deg, rgba(102, 187, 106, 0.08) 0%, rgba(129, 199, 132, 0.05) 100%);
}

.share-button:hover .share-icon {
  fill: #66BB6A;
  transform: rotate(-15deg) scale(1.1);
}

.share-button:active {
  transform: scale(0.95);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .toolbar-inner {
    gap: 8px;
    padding: 10px 12px;
  }

  .like-button {
    width: 120px;
    height: 34px;
  }

  .like-button .like-icon {
    width: 16px;
    height: 16px;
  }

  .like-button .like-text,
  .like-button .like-count {
    font-size: 12px;
  }

  .favorite-button,
  .comment-button,
  .share-button {
    padding: 8px 14px;
  }

  .favorite-button .favorite-icon,
  .share-button .share-icon {
    width: 16px;
    height: 16px;
  }

  .favorite-button .favorite-text,
  .comment-button .comment-text,
  .share-button .share-text {
    font-size: 12px;
  }
}


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
