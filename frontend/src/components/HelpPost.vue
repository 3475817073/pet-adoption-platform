<template>
  <div>
    <el-page-header title="互助交流" content="物资共享、医疗咨询、养宠经验 💬" />

    <el-button type="primary" @click="showPublishDialog" style="margin-bottom: 20px; background: #E07A5F; border-color: #E07A5F; border-radius: 20px; padding: 10px 24px">
      + 发布新互助帖
    </el-button>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <el-row :gutter="15">
        <el-col :span="8">
          <el-input v-model="searchKeyword" placeholder="🔍 搜索帖子标题或内容" clearable
                    style="--el-input-border-radius: 20px" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterCategory" placeholder="筛选分类" clearable style="width: 100%; --el-select-border-radius-hover: 20px">
            <el-option label="全部" value="" />
            <el-option label="📦 物资共享" value="物资共享" />
            <el-option label="🏥 医疗咨询" value="医疗咨询" />
            <el-option label="📚 经验分享" value="经验分享" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="sortBy" placeholder="排序方式" style="width: 100%; --el-select-border-radius-hover: 20px">
            <el-option label="最新发布" value="newest" />
            <el-option label="最早发布" value="oldest" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="resetFilter" style="width: 100%; background: #81B29A; border-color: #81B29A; border-radius: 20px">
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 卡片式帖子列表 -->
    <el-row :gutter="20">
      <el-col v-for="post in filteredPosts" :key="post.id" :xs="24" :sm="12" :md="8" style="margin-bottom: 20px">
        <el-card class="post-card" shadow="never" @click="showPostDetail(post)">
          <div class="post-header">
            <el-tag :type="getCategoryType(post.category)" size="small" class="category-tag">{{ post.category }}</el-tag>
            <span class="post-time">{{ formatDateTime(post.createTime) }}</span>
          </div>
          <h4 class="post-title">{{ post.title }}</h4>
          <p class="post-content">{{ post.content }}</p>
          <div class="post-footer">
            <span class="author">👤 {{ post.user?.username || post.author || '匿名' }}</span>
            <span class="comment-count">💬 {{ post.commentCount || 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="filteredPosts.length === 0" description="暂无帖子" :image-size="150" />

    <!-- 发布互助帖弹窗 -->
    <el-dialog v-model="publishVisible" title="发布互助帖" width="600px">
      <el-form :model="postForm" label-width="100px">
        <el-form-item label="分类" required>
          <el-select v-model="postForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="物资共享" value="物资共享" />
            <el-option label="医疗咨询" value="医疗咨询" />
            <el-option label="经验分享" value="经验分享" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="postForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="postForm.content" type="textarea" rows="6" placeholder="请详细描述你的问题或分享经验" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost" :loading="loading" style="background: #E07A5F; border-color: #E07A5F; border-radius: 20px">发布</el-button>
      </template>
    </el-dialog>

    <!-- 帖子详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentPost.title" width="750px">
      <div style="margin-bottom: 20px">
        <el-tag :type="getCategoryType(currentPost.category)" style="margin-right: 10px" class="category-tag">{{ currentPost.category }}</el-tag>
        <span style="color: #6B7280; font-size: 14px">发布者：{{ currentPost.user?.username || currentPost.author }}</span>
        <span style="color: #6B7280; font-size: 14px; margin-left: 15px">发布时间：{{ formatDateTime(currentPost.createTime) }}</span>
      </div>

      <p style="white-space: pre-wrap; font-size: 16px; margin-bottom: 30px; line-height: 1.8; color: #3D405B">
        {{ currentPost.content }}
      </p>

      <h4>💬 评论区（{{ totalComments }}条）</h4>

      <el-alert v-if="!isLoggedIn" title="请先登录后再参与评论" type="warning" :closable="false" style="margin-bottom: 15px; background: #FFF9E6; border-color: #F2CC8F">
        <template #default>
          登录后可发布评论和互助帖
          <el-button type="primary" size="small" @click="handleNeedLogin" style="margin-left: 10px; background: #E07A5F; border-color: #E07A5F">去登录</el-button>
        </template>
      </el-alert>

      <div class="comments-container" v-loading="commentsLoading">
        <div v-for="(comment, index) in comments" :key="'comment-' + comment.id" class="main-comment">
          <div class="comment-header">
            <strong class="comment-user">{{ comment.user }}</strong>
            <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>

          <div class="comment-actions">
            <el-button v-if="isLoggedIn" type="primary" size="small" text @click="showMainReplyBox(comment)">回复</el-button>
            <el-button v-if="canDelete(comment.user)" type="danger" size="small" text @click="deleteMainComment(comment.id)">删除</el-button>
          </div>

          <div v-if="replyBoxIndex === index && replyBoxType === 'main'" class="reply-input-box">
            <el-input v-model="replyText" :placeholder="'回复 @' + comment.user" @keyup.enter="submitReplyToMain(comment.id)" size="small">
              <template #append>
                <el-button type="primary" size="small" :disabled="!replyText.trim()" @click="submitReplyToMain(comment.id)" style="background: #E07A5F; border-color: #E07A5F">发送</el-button>
              </template>
            </el-input>
          </div>

          <div v-if="comment.replies && comment.replies.length > 0" class="replies-container">
            <div v-for="(reply, rIndex) in getVisibleReplies(comment, index)" :key="'reply-' + reply.id" class="reply-item">
              <div class="comment-header">
                <strong class="comment-user">{{ reply.user }}</strong>
                <span v-if="reply.replyTo" class="reply-to">回复 @{{ reply.replyTo }}</span>
                <span class="comment-time">{{ formatCommentTime(reply.createTime) }}</span>
              </div>
              <div class="comment-content">{{ reply.content }}</div>

              <div class="comment-actions">
                <el-button v-if="isLoggedIn" type="primary" size="small" text @click="showReplyReplyBox(index, reply)">回复</el-button>
                <el-button v-if="canDelete(reply.user)" type="danger" size="small" text @click="deleteReply(reply.id)">删除</el-button>
              </div>

              <div v-if="replyBoxIndex === index && replyBoxReplyId === reply.id && replyBoxType === 'reply'" class="reply-input-box">
                <el-input v-model="replyText" :placeholder="'回复 @' + reply.user" @keyup.enter="submitReplyToReply(comment.id, reply.id)" size="small">
                  <template #append>
                    <el-button type="primary" size="small" :disabled="!replyText.trim()" @click="submitReplyToReply(comment.id, reply.id)" style="background: #E07A5F; border-color: #E07A5F">发送</el-button>
                  </template>
                </el-input>
              </div>
            </div>

            <el-button v-if="comment.replies.length > 3 && !expandedReplies[index]" type="primary" text size="small" @click="toggleExpand(index)" style="margin-top: 8px">
              展开 {{ comment.replies.length }} 条回复
            </el-button>
            <el-button v-if="expandedReplies[index]" type="primary" text size="small" @click="toggleExpand(index)" style="margin-top: 8px">收起</el-button>
          </div>
        </div>
      </div>

      <div v-if="isLoggedIn">
        <el-input v-model="newComment" placeholder="写下你的评论..." @keyup.enter="submitComment" style="margin-top: 20px">
          <template #append>
            <el-button type="primary" :disabled="!newComment.trim()" @click="submitComment" style="background: #E07A5F; border-color: #E07A5F">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>
    <!-- 分页组件 -->
    <div v-if="!detailVisible" class="pagination-wrapper">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, prev, pager, next, sizes, jumper"
          :pager-count="5"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
          class="custom-pagination"
      />
    </div>



  </div>
  <!-- 发布互助帖弹窗 -->
  <el-dialog
      v-model="publishVisible"
      title="发布互助帖"
      width="600px"
  >
    <el-form :model="postForm" label-width="80px">
      <el-form-item label="分类">
        <el-select v-model="postForm.category" placeholder="请选择分类">
          <el-option label="物资共享" value="物资共享" />
          <el-option label="医疗咨询" value="医疗咨询" />
          <el-option label="经验分享" value="经验分享" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="postForm.title" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="postForm.content" type="textarea" :rows="6" placeholder="请输入内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="publishVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPost" :loading="loading">发布</el-button>
    </template>
  </el-dialog>

</template>

<script setup>

import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const emit = defineEmits(['needLogin'])

const posts = ref([])

const searchKeyword = ref('')
const filterCategory = ref('')
const sortBy = ref('newest')

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

  result.sort((a, b) => {
    if (sortBy.value === 'newest') {
      return new Date(b.createTime) - new Date(a.createTime)
    }
    return new Date(a.createTime) - new Date(b.createTime)
  })

  return result
})

const resetFilter = () => {
  searchKeyword.value = ''
  filterCategory.value = ''
  sortBy.value = 'newest'
}

const detailVisible = ref(false)
const publishVisible = ref(false)
const currentPost = ref({})
const comments = ref([])
const newComment = ref('')
const isLoggedIn = ref(false)
const loading = ref(false)
const commentsLoading = ref(false)

const postForm = ref({
  category: '',
  title: '',
  content: ''
})

const replyBoxIndex = ref(-1)
const replyBoxReplyId = ref(-1)
const replyBoxType = ref('')
const replyText = ref('')
const expandedReplies = ref({})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jumpPage = ref(1)


const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

const canDelete = (commentUser) => {
  const user = getCurrentUser()
  return isLoggedIn.value && user && commentUser === user.username
}

const checkLoginStatus = () => { isLoggedIn.value = !!localStorage.getItem('user') }

const totalComments = computed(() => {
  let count = 0
  comments.value.forEach(c => {
    count += 1
    if (c.replies) count += c.replies.length
  })
  return count
})

const getCategoryType = (category) => {
  const typeMap = {
    '物资共享': '',
    '医疗咨询': 'warning',
    '经验分享': 'info'
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

const formatCommentTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

onMounted(async () => {
  checkLoginStatus()
  await loadPostsFromServer()
})

const loadPostsFromServer = async () => {
  loading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/help/list?page=${currentPage.value - 1}&size=${pageSize.value}`)
    if (res.ok) {
      const data = await res.json()
      posts.value = data.content
      total.value = data.totalElements

      const postsWithComments = await Promise.all(
          posts.value.map(async (post) => {
            try {
              const commentRes = await fetch(`http://localhost:8080/api/help/comments/${post.id}`)
              if (commentRes.ok) {
                const commentData = await commentRes.json()
                post.commentCount = commentData.reduce((sum, c) => sum + 1 + (c.replies?.length || 0), 0)
              }
            } catch (e) {
              post.commentCount = 0
            }
            return post
          })
      )

      posts.value = postsWithComments
    } else {
      const errorText = await res.text()
      ElMessage.error('加载帖子失败：' + errorText)
    }
  } catch (error) {
    ElMessage.error('网络错误：' + error.message)
    console.error(error)
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


const showPublishDialog = () => {
  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录才能发布互助帖')
    emit('needLogin')
    return
  }
  postForm.value = { category: '', title: '', content: '' }
  publishVisible.value = true
}

const submitPost = async () => {
  if (!postForm.value.category || !postForm.value.title || !postForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  const user = getCurrentUser()

  try {
    const res = await fetch('http://localhost:8080/api/help/publish', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: user.username,
        title: postForm.value.title,
        content: postForm.value.content,
        category: postForm.value.category
      })
    })

    if (res.ok) {
      ElMessage.success('帖子发布成功！')
      publishVisible.value = false
      postForm.value = { category: '', title: '', content: '' }
      await loadPostsFromServer()
    } else {
      ElMessage.error(await res.text())
    }
  } catch (error) {
    ElMessage.error('网络错误')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const showDetail = async (post) => {
  currentPost.value = post
  detailVisible.value = true
  await loadComments(post.id)
}

const loadComments = async (postId) => {
  commentsLoading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/help/comments/${postId}`)
    if (res.ok) {
      const data = await res.json()
      comments.value = data
    } else {
      comments.value = []
    }
  } catch (error) {
    ElMessage.error('加载评论失败')
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录才能评论')
    emit('needLogin')
    return
  }

  const user = getCurrentUser()
  try {
    const res = await fetch('http://localhost:8080/api/help/comment', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        postId: currentPost.value.id,
        username: user.username,
        content: newComment.value
      })
    })

    if (res.ok) {
      ElMessage.success('评论成功')
      newComment.value = ''
      await loadComments(currentPost.value.id)
      await loadPostsFromServer()
    } else {
      ElMessage.error(await res.text())
    }
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

const submitReply = async (commentId, type) => {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录才能回复')
    emit('needLogin')
    return
  }

  const user = getCurrentUser()
  try {
    const res = await fetch('http://localhost:8080/api/help/reply', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        postId: currentPost.value.id,
        username: user.username,
        content: replyText.value,
        parentCommentId: commentId,
        replyToCommentId: type === 'reply' ? null : replyBoxReplyId.value
      })
    })

    if (res.ok) {
      ElMessage.success('回复成功')
      replyText.value = ''
      replyBoxIndex.value = -1
      replyBoxReplyId.value = -1
      replyBoxType.value = ''
      await loadComments(currentPost.value.id)
    } else {
      ElMessage.error(await res.text())
    }
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const toggleReplies = (commentId) => {
  expandedReplies.value[commentId] = !expandedReplies.value[commentId]
}

const showReplyBox = (index, type, replyId = -1) => {
  replyBoxIndex.value = index
  replyBoxType.value = type
  replyBoxReplyId.value = replyId
  replyText.value = ''
}

const cancelReply = () => {
  replyBoxIndex.value = -1
  replyBoxReplyId.value = -1
  replyBoxType.value = ''
  replyText.value = ''
}

const deleteComment = async (commentId) => {
  try {
    ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const res = await fetch(`http://localhost:8080/api/help/comment/${commentId}`, {
        method: 'DELETE'
      })
      if (res.ok) {
        ElMessage.success('删除成功')
        await loadComments(currentPost.value.id)
        await loadPostsFromServer()
      } else {
        ElMessage.error(await res.text())
      }
    }).catch(() => {})
  } catch (error) {
    ElMessage.error('删除失败')
  }
}
</script>


<style scoped>
.filter-section {
  margin: 20px 0;
  padding: 18px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(61,64,91,0.06);
}

.post-card {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  cursor: pointer;
  height: 100%;
  border: 1px solid #E5E7EB;
  background: white;
}
.post-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(61,64,91,0.12);
}
.category-tag {
  border-radius: 12px;
  border: none;
}
.post-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.post-title {
  font-size: 18px;
  margin: 0 0 12px 0;
  line-height: 1.4;
  color: #3D405B;
  font-weight: 600;
}
.post-content {
  color: #6B7280;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 15px;
}
.post-footer {
  display: flex;
  justify-content: space-between;
  color: #9CA3AF;
  font-size: 14px;
  padding-top: 12px;
  border-top: 1px solid #E5E7EB;
}

.comments-container { max-height: 450px; overflow-y: auto; margin-bottom: 15px; padding-right: 5px; }
.comments-container::-webkit-scrollbar { width: 6px; }
.comments-container::-webkit-scrollbar-thumb { background-color: #D1D5DB; border-radius: 3px; }
.main-comment { margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #E5E7EB; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.comment-user { color: #81B29A; font-size: 14px; font-weight: 600; }
.comment-time { color: #9CA3AF; font-size: 12px; }
.comment-content { font-size: 14px; line-height: 1.6; color: #3D405B; margin-bottom: 8px; }
.comment-actions { display: flex; gap: 10px; margin-bottom: 10px; }
.reply-input-box { margin: 10px 0 15px 0; }
.replies-container { margin-left: 30px; background: #F9FAFB; padding: 12px; border-radius: 8px; }
.reply-item { margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #E5E7EB; }
.reply-item:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
.reply-to { color: #81B29A; font-size: 12px; }

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

