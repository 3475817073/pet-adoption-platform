<template>
  <div class="user-management">
    <el-card class="header-card">
      <div class="header-content">
        <h2>用户管理</h2>
        <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名或真实姓名"
            style="width: 300px"
            clearable
            @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </el-card>

    <el-card class="table-card" style="margin-top: 20px">
      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 8px">
              <el-button
                  type="success"
                  size="small"
                  @click="openMessageDialog(row)"
                  :disabled="row.id === currentUser?.id"
              >
                私信
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteUser(row)"
                  :disabled="row.id === currentUser?.id"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            background
            class="custom-pagination"
        />
      </div>
    </el-card>

    <!-- 私信侧边栏 -->
    <transition name="slide-drawer">
      <div v-if="showMessageDialog" class="message-drawer">
        <!-- 遮罩层 -->
        <div class="drawer-overlay" @click="showMessageDialog = false"></div>

        <!-- 私信区主体 -->
        <div class="drawer-content">
          <!-- 抽屉头部 -->
          <div class="drawer-header">
            <h3 class="drawer-title">与 {{ currentChatUser?.username || '用户' }} 的对话</h3>
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
                  <label class="upload-image-btn" :class="{ 'has-image': previewImageUrl, 'uploading': uploadingImage }">
                    <span v-if="uploadingImage"></span>
                    <span v-else>+</span>
                    <input
                        type="file"
                        accept="image/*"
                        @change="handleImageSelect"
                        :disabled="uploadingImage"                      style="display: none"
                    />
                  </label>
                  <span class="tip">按 Ctrl + Enter 发送</span>
                </div>
                <el-button
                    type="primary"
                    :disabled="(!messageContent.trim() && !previewImageUrl) || uploadingImage"
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
import { ref, onMounted, nextTick } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { get, del, post } from '../utils/request.js'
import { success, error, warning } from '../utils/message.js'

const userList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentUser = ref(null)

// 私信相关
const showMessageDialog = ref(false)
const messageContent = ref('')
const messageLoading = ref(false)
const messagesLoading = ref(false)
const messages = ref([])
const currentChatUser = ref(null)
const previewImageUrl = ref('')
const uploadedImageUrl = ref(null)
const selectedImageFile = ref(null)
const uploadingImage = ref(false)

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

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getRoleType = (role) => {
  return role === 'ADMIN' ? 'danger' : 'info'
}

const getRoleText = (role) => {
  return role === 'ADMIN' ? '管理员' : '普通用户'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    const allUsers = await get('/api/user/list', params)
    userList.value = allUsers.content || []
    total.value = allUsers.totalElements || 0
  } catch (err) {
    error(err.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadUsers()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

/**
 * 打开私信对话框
 */
const openMessageDialog = async (user) => {
  if (user.id === currentUser.value?.id) {
    warning('不能给自己发送消息')
    return
  }

  currentChatUser.value = user
  showMessageDialog.value = true
  messages.value = []
  messageContent.value = ''
  previewImageUrl.value = ''
  selectedImageFile.value = null
  uploadingImage.value = false
  await loadMessages()
}

/**
 * 加载消息记录
 */
const loadMessages = async () => {
  messagesLoading.value = true
  const user = getCurrentUser()

  try {
    const data = await get('/api/message/conversation', {
      username1: user.username,
      username2: currentChatUser.value.username,
      page: 0,
      size: 50
    })
    // 反转消息顺序，让最新的在底部
    messages.value = (data.content || []).reverse()

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (err) {
    console.error('加载消息失败:', err)
    messages.value = []
  } finally {
    messagesLoading.value = false
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
    // 清空input防止重复选择同一文件不触发change
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
 * 移除预览图片
 */
const removePreviewImage = () => {
  previewImageUrl.value = ''
  selectedImageFile.value = null
  uploadedImageUrl.value = null
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
      receiverUsername: currentChatUser.value.username,
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
  } catch (err) {
    console.error('发送消息失败:', err)
    error(err.message || '发送失败')
  } finally {
    messageLoading.value = false
  }
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

const handleDeleteUser = (user) => {
  if (user.id === currentUser.value?.id) {
    warning('不能删除自己的账号')
    return
  }

  ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作将永久删除该用户及其所有相关数据（包括发布的宠物、申请记录、收藏等），且不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        distinguishCancelAndClose: true
      }
  ).then(async () => {
    try {
      await del(`/api/user/delete/${user.id}`, {
        adminUsername: currentUser.value.username
      })
      success('用户删除成功')
      loadUsers()
    } catch (err) {
      error(err.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  currentUser.value = getCurrentUser()
  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    error('无权限访问，请使用管理员账号登录')
    return
  }
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header-card {
  border-radius: 12px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #1f2937;
  font-size: 20px;
}

.table-card {
  border-radius: 12px;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.custom-pagination :deep(.el-pager li.active) {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  color: white;
  border: none;
}

.custom-pagination :deep(.el-pager li:hover:not(.active)) {
  background-color: #FDF8F3;
  color: #E07A5F;
}

/* ===== 私信侧边栏 ===== */
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
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  color: white;
  flex-shrink: 0;
}

.drawer-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: transparent;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f7f8fa;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 60px 0;
  font-size: 14px;
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

.upload-image-btn.uploading {
  cursor: not-allowed;
  opacity: 0.6;
}

.tip {
  font-size: 12px;
  color: #999;
}

.send-btn {
  border-radius: 8px;
  padding: 8px 20px;
}

/* 侧边栏滑入动画 */
.slide-drawer-enter-active,
.slide-drawer-leave-active {
  transition: all 0.3s ease;
}

.slide-drawer-enter-from {
  opacity: 0;
}

.slide-drawer-enter-from .drawer-content {
  transform: translateX(100%);
}

.slide-drawer-leave-to {
  opacity: 0;
}

.slide-drawer-leave-to .drawer-content {
  transform: translateX(100%);
}

.drawer-content {
  transition: transform 0.3s ease;
}
</style>
