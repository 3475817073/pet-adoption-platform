<template>
  <div>

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
        <el-card class="post-card" shadow="never" @click="goToPostDetail(post)">
          <div class="post-header">
            <el-tag :type="getCategoryType(post.category)" size="small" class="category-tag">{{ post.category }}</el-tag>
            <span class="post-time">{{ formatDateTime(post.createTime) }}</span>
            <!-- 【新增】删除按钮：仅管理员或作者可见 -->
            <el-button
                v-if="currentUser && (currentUser.role === 'ADMIN' || currentUser.username === post.user?.username)"
                type="danger"
                size="small"
                link
                @click.stop="handleDeletePost(post)"              style="margin-left: auto;"
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

/**
 * 互助交流页面组件
 * 提供帖子浏览、筛选、发布、详情查看、评论与回复、删除等完整交互功能
 */
import {ref, onMounted, computed, nextTick, watch, inject} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { get, post, del } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'

const router = useRouter()
const route = useRoute()
const triggerLogin = inject('triggerLogin')

const posts = ref([])

const searchKeyword = ref('')
const filterCategory = ref('')
const sortBy = ref('newest')

/**
 * 根据搜索关键词和分类动态过滤帖子列表
 */
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

/**
 * 重置所有筛选条件并重新加载第一页数据
 */
const resetFilter = () => {
  searchKeyword.value = ''
  filterCategory.value = ''
  sortBy.value = 'newest'
  currentPage.value = 1
  loadPostsFromServer()
}

/**
 * 监听排序方式、搜索关键词、分类变化，自动重置页码并重新请求数据
 */
watch([sortBy, searchKeyword, filterCategory], () => {
  currentPage.value = 1
  loadPostsFromServer()
})



const publishVisible = ref(false)

const loading = ref(false)


const postForm = ref({
  category: '',
  title: '',
  content: ''
})



const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jumpPage = ref(1)


/**
 * 获取本地存储中的当前登录用户信息
 */
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}



/**
 * 根据帖子分类返回对应的 Element Plus 标签颜色类型
 */
const getCategoryType = (category) => {
  const typeMap = {
    '物资共享': '',
    '医疗咨询': 'warning',
    '经验分享': 'info'
  }
  return typeMap[category] || ''
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm 格式
 */
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
 * 组件挂载时执行：检查登录状态并加载帖子列表
 */
onMounted(async () => {
  // 1. 从路由参数恢复分页状态
  if (route.query.page) {
    currentPage.value = parseInt(route.query.page)
  }
  if (route.query.size) {
    pageSize.value = parseInt(route.query.size)
  }

  await loadPostsFromServer()

  // 2. 数据加载完成后，恢复滚动位置
  nextTick(() => {
    const savedScrollY = sessionStorage.getItem('helpScrollPosition')
    if (savedScrollY && parseInt(savedScrollY) > 0) {
      const scrollY = parseInt(savedScrollY)
      window.scrollTo(0, scrollY)
      document.documentElement.scrollTop = scrollY
      document.body.scrollTop = scrollY
      // 清除已使用的记录
      sessionStorage.removeItem('helpScrollPosition')
    }
  })
})

/**
 * 从后端服务器加载分页帖子数据，并异步获取每条帖子的评论数量
 */
const loadPostsFromServer = async () => {
  loading.value = true
  try {
    const data = await get('/api/help/list', {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value
    })
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


/**
 * 处理页码变化，重新请求数据
 */
const handlePageChange = (page) => {
  currentPage.value = page
  loadPostsFromServer()
}

/**
 * 处理每页显示条数变化，重置页码并重新请求
 */
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadPostsFromServer()
}

/**
 * 处理跳转页码输入，校验范围后执行跳转
 */
const handleJumpPage = () => {
  if (jumpPage.value && jumpPage.value >= 1 && jumpPage.value <= Math.ceil(total.value / pageSize.value)) {
    currentPage.value = jumpPage.value
    loadPostsFromServer()
  }
}




/**
 * 跳转到帖子独立详情页
 */
const goToPostDetail = (post) => {
  // 1. 跳转前保存滚动位置
  const currentScrollY = window.scrollY || document.documentElement.scrollTop
  sessionStorage.setItem('helpScrollPosition', currentScrollY.toString())

  // 2. 携带分页参数跳转
  router.push({
    path: `/post/${post.id}`,
    query: {
      page: currentPage.value,
      size: pageSize.value
    }
  })
}



// 获取当前登录用户信息
const currentUser = JSON.parse(localStorage.getItem('user'))

/**
 * 删除互助帖子
 */
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

/**
 * 打开发布互助帖弹窗（带登录检查）
 */
const showPublishDialog = () => {
  const user = getCurrentUser()
  if (!user) {
    warning('请先登录后再发布帖子')
    triggerLogin()
    return
  }
  publishVisible.value = true
}


/**
 * 提交发布互助帖
 */
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
  try {
    await post('/api/help/post', {
      category: postForm.value.category,
      title: postForm.value.title,
      content: postForm.value.content,
      username: user.username
    })

    success('发布成功')
    publishVisible.value = false
    postForm.value = { category: '', title: '', content: '' }
    await loadPostsFromServer()
  } catch (err) {
    error(err.message || '发布失败')
  } finally {
    loading.value = false
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
