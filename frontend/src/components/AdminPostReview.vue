<template>
  <div>

    <!-- Tab 切换：待审核/已通过/已拒绝 -->
    <el-tabs v-model="activeTab">
      <!-- 待审核列表 -->
      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pendingList" style="width: 100%" v-loading="pendingLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120">
            <template #default="{ row }">
              <el-tag :type="getCategoryTag(row.category)">
                {{ getCategoryText(row.category) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布者" width="150">
            <template #default="{ row }">
              {{ row.user?.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
              <el-button type="success" size="small" @click="handleReview(row, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="pendingPage"
              v-model:page-size="pendingPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="pendingTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePendingSizeChange"
              @current-change="handlePendingPageChange"
              background
          />
        </div>
      </el-tab-pane>

      <!-- 已通过列表 -->
      <el-tab-pane label="已通过" name="approved">
        <el-table :data="approvedList" style="width: 100%" v-loading="approvedLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120">
            <template #default="{ row }">
              <el-tag :type="getCategoryTag(row.category)">
                {{ getCategoryText(row.category) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布者" width="150">
            <template #default="{ row }">
              {{ row.user?.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="approvedPage"
              v-model:page-size="approvedPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="approvedTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleApprovedSizeChange"
              @current-change="handleApprovedPageChange"
              background
          />
        </div>
      </el-tab-pane>

      <!-- 已拒绝列表 -->
      <el-tab-pane label="已拒绝" name="rejected">
        <el-table :data="rejectedList" style="width: 100%" v-loading="rejectedLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120">
            <template #default="{ row }">
              <el-tag :type="getCategoryTag(row.category)">
                {{ getCategoryText(row.category) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布者" width="150">
            <template #default="{ row }">
              {{ row.user?.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="rejectedPage"
              v-model:page-size="rejectedPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="rejectedTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRejectedSizeChange"
              @current-change="handleRejectedPageChange"
              background
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="帖子详细信息" width="800px">
      <div v-if="currentPost" class="post-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">{{ currentPost.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">
            <el-tag :type="getCategoryTag(currentPost.category)">
              {{ getCategoryText(currentPost.category) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="城市">{{ currentPost.city || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentPost.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatTime(currentPost.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="内容">
            <div style="white-space: pre-wrap; line-height: 1.8">{{ currentPost.content }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 帖子图片 -->
        <div v-if="getPostImages(currentPost).length > 0" style="margin-top: 20px">
          <h4 style="margin-bottom: 10px; color: #666">帖子图片</h4>
          <el-row :gutter="10">
            <el-col :span="6" v-for="(img, index) in getPostImages(currentPost)" :key="index">
              <img :src="img" style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px; cursor: pointer" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝帖子发布" width="500px">
      <el-form label-width="100px">
        <el-form-item label="帖子标题">
          <span>{{ currentPost?.title }}</span>
        </el-form-item>
        <el-form-item label="拒绝理由" required>
          <el-input
              v-model="rejectReason"
              type="textarea"
              :rows="4"
              placeholder="请填写拒绝理由，该理由将展示给用户..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :disabled="!rejectReason.trim()">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, watch} from 'vue'
import { ElMessageBox } from 'element-plus'
import { get, post } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'

const activeTab = ref('pending')
const currentUser = ref(null)

// 待审核数据
const pendingList = ref([])
const pendingLoading = ref(false)
const pendingPage = ref(1)
const pendingPageSize = ref(10)
const pendingTotal = ref(0)

// 已通过数据
const approvedList = ref([])
const approvedLoading = ref(false)
const approvedPage = ref(1)
const approvedPageSize = ref(10)
const approvedTotal = ref(0)

// 已拒绝数据
const rejectedList = ref([])
const rejectedLoading = ref(false)
const rejectedPage = ref(1)
const rejectedPageSize = ref(10)
const rejectedTotal = ref(0)

// 详情对话框
const detailDialogVisible = ref(false)
const currentPost = ref(null)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectingPost = ref(null)

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getCategoryText = (category) => {
  const map = {
    '物资共享': '📦 物资共享',
    '医疗咨询': ' 医疗咨询',
    '经验分享': ' 经验分享'
  }
  return map[category] || category
}

const getCategoryTag = (category) => {
  const map = {
    '物资共享': 'success',
    '医疗咨询': 'danger',
    '经验分享': 'warning'
  }
  return map[category] || ''
}

const getPostImages = (post) => {
  if (!post.photoUrls) return []
  try {
    const photos = JSON.parse(post.photoUrls)
    if (photos && photos.length > 0) {
      return photos.map(url => url.startsWith('http') ? url : 'http://localhost:8080' + url)
    }
  } catch {
    return []
  }
  return []
}

const loadPending = async () => {
  pendingLoading.value = true
  try {
    const data = await get('/api/help/pending', {
      username: currentUser.value.username,
      page: pendingPage.value - 1,
      size: pendingPageSize.value
    })
    pendingList.value = data.content
    pendingTotal.value = data.totalElements
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    pendingLoading.value = false
  }
}

const loadApproved = async () => {
  approvedLoading.value = true
  try {
    const data = await get('/api/help/list', {
      page: approvedPage.value - 1,
      size: approvedPageSize.value
    })
    approvedList.value = data.content
    approvedTotal.value = data.totalElements
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    approvedLoading.value = false
  }
}

const loadRejected = async () => {
  rejectedLoading.value = true
  try {
    const data = await get('/api/help/rejected', {
      username: currentUser.value.username,
      page: rejectedPage.value - 1,
      size: rejectedPageSize.value
    })
    rejectedList.value = data.content
    rejectedTotal.value = data.totalElements
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    rejectedLoading.value = false
  }
}

const viewDetail = (post) => {
  currentPost.value = post
  detailDialogVisible.value = true
}

const handleReview = async (helpPost, action) => {
  try {
    await ElMessageBox.confirm(`确定要通过该帖子吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    const user = getCurrentUser()
    const url = `/api/help/review/${helpPost.id}?username=${user.username}&action=${action}`
    await post(url, null)
    success('已通过该帖子')
    loadPending()
    loadApproved()
    loadRejected()
  } catch (err) {
    error(err.message || '操作失败')
  }
}

const handleReject = (post) => {
  rejectingPost.value = post
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    warning('请填写拒绝理由')
    return
  }
  try {
    const user = getCurrentUser()
    const url = `/api/help/review/${rejectingPost.value.id}?username=${user.username}&action=reject&reason=${encodeURIComponent(rejectReason.value)}`
    await post(url, null)
    success('已拒绝该帖子')
    rejectDialogVisible.value = false
    loadPending()
    loadApproved()
    loadRejected()
  } catch (err) {
    error(err.message || '操作失败')
  }
}

const handlePendingPageChange = (page) => {
  pendingPage.value = page
  loadPending()
}

const handlePendingSizeChange = (size) => {
  pendingPageSize.value = size
  pendingPage.value = 1
  loadPending()
}

const handleApprovedPageChange = (page) => {
  approvedPage.value = page
  loadApproved()
}

const handleApprovedSizeChange = (size) => {
  approvedPageSize.value = size
  approvedPage.value = 1
  loadApproved()
}

const handleRejectedPageChange = (page) => {
  rejectedPage.value = page
  loadRejected()
}

const handleRejectedSizeChange = (size) => {
  rejectedPageSize.value = size
  rejectedPage.value = 1
  loadRejected()
}

onMounted(() => {
  currentUser.value = getCurrentUser()
  if (!currentUser.value) {
    warning('请先登录')
    return
  }
  loadPending()
})
watch(activeTab, (newTab) => {
  if (newTab === 'pending') {
    loadPending()
  } else if (newTab === 'approved') {
    loadApproved()
  } else if (newTab === 'rejected') {
    loadRejected()
  }
})
</script>

<style scoped>
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.post-detail {
  padding: 10px;
}
</style>
