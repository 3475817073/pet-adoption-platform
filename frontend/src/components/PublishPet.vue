<template>
  <div class="publish-container">
    <!-- 欢迎页视图 - 完全保持原样 -->
    <div v-if="!showForm" class="welcome-view">
      <div class="welcome-card">
        <div class="title-section">
          <span class="emoji-left">🐱</span>
          <h1>为小生命找个家</h1>
          <span class="emoji-right">🐶</span>
        </div>

        <div class="banner-wrapper">
          <img src="@/assets/pets-horizontal.png" alt="宠物横幅" class="welcome-banner" />
        </div>

        <p class="subtitle">填写宠物信息，让更多人看到它</p>

        <div class="cta-button-wrapper">
          <el-button
              type="primary"
              size="large"
              class="start-publish-btn"
              @click="handleStartPublish">
            <span class="btn-icon">📝</span>
            开始发布宠物信息
          </el-button>
        </div>

        <div class="doodle-line"></div>
      </div>

      <!-- 小贴士（保留在欢迎页） -->
      <div class="tips-floating">
        <div class="tip-card">
          <h3>💡 小贴士</h3>
          <ul class="tip-list">
            <li>
              <span class="tip-icon">✓</span>
              <div>
                <strong>信息真实</strong>
                <p>准确描述年龄、性别和健康状况</p>
              </div>
            </li>
            <li>
              <span class="tip-icon">✓</span>
              <div>
                <strong>照片清晰</strong>
                <p>建议上传3张清晰照片</p>
              </div>
            </li>
            <li>
              <span class="tip-icon">✓</span>
              <div>
                <strong>宠物介绍</strong>
                <p>告诉领养者它的特点</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 表单页视图 - 柔和现代风格 -->
    <div v-else class="form-view">
      <div class="form-header">
        <el-button
            class="back-btn"
            @click="showForm = false"
            circle>
          ←
        </el-button>
        <h2> 发布宠物信息</h2>
      </div>

      <div class="form-card">
        <el-form :model="form" label-width="100px" class="custom-form">
          <div class="form-row form-row-short">
            <el-form-item label="名字" required>
              <el-input v-model="form.name" placeholder="给它取个好听的名字吧" class="styled-input" />
            </el-form-item>
          </div>

          <div class="form-row form-row-short">
            <el-form-item label="种类" required>
              <el-input v-model="form.type" placeholder="猫 / 狗 / 其他" class="styled-input" />
            </el-form-item>
          </div>

          <div class="form-row form-row-short">
            <el-form-item label="性别" required>
              <el-radio-group v-model="form.gender" class="gender-radio">
                <el-radio label="公">公</el-radio>
                <el-radio label="母">母</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <div class="form-row form-row-short">
            <el-form-item label="年龄" required>
              <div class="age-stepper">
                <button type="button" class="stepper-btn minus" @click="decreaseAge">−</button>
                <input
                    class="stepper-display"
                    type="number"
                    v-model.number="form.age"
                >
                <button type="button" class="stepper-btn plus" @click="increaseAge">+</button>
                <span class="unit-text">岁</span>
              </div>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="宠物介绍" required>
              <el-input
                  type="textarea"
                  v-model="form.description"
                  rows="4"
                  placeholder="描述它的性格、健康状况、是否需要特殊照顾..."
                  class="styled-textarea" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="健康状态">
              <div class="health-checkboxes">
                <label class="custom-checkbox">
                  <input type="checkbox" v-model="form.isVaccinated" />
                  <span class="checkmark"></span>
                  <span class="checkbox-label">💉 已疫苗</span>
                </label>
                <label class="custom-checkbox">
                  <input type="checkbox" v-model="form.isNeutered" />
                  <span class="checkmark"></span>
                  <span class="checkbox-label">✂️ 已绝育</span>
                </label>
                <label class="custom-checkbox">
                  <input type="checkbox" v-model="form.isDewormed" />
                  <span class="checkmark"></span>
                  <span class="checkbox-label">🦠 已驱虫</span>
                </label>
              </div>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="性格标签">
              <el-select
                  v-model="form.tags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="请选择或输入标签（如：活泼、亲人）"
                  class="styled-input">
                <el-option label="活泼好动" value="活泼"></el-option>
                <el-option label="安静温顺" value="安静"></el-option>
                <el-option label="粘人精" value="亲人"></el-option>
                <el-option label="高冷独立" value="高冷"></el-option>
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row photo-upload-section">
            <el-form-item label="照片">
              <div class="upload-area">
                <el-upload
                    action="http://localhost:8080/api/upload/image"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    list-type="picture-card"
                    :file-list="imageList"
                    :limit="3"
                    accept="image/*"
                    class="custom-upload">
                  <div class="upload-placeholder">
                    <span class="upload-icon">📷</span>
                    <p>点击上传照片</p>
                    <p class="upload-hint">最多3张，支持jpg/png，单张≤5MB</p>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </div>

          <div class="submit-section">
            <el-button
                type="primary"
                @click="publishPet"
                :loading="loading"
                class="submit-btn">
              {{ loading ? '发布中...' : '🎉 发布宠物信息' }}
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>

  <!-- 登录弹窗 -->
  <Login
      v-model="showLoginDialog"
      @loginSuccess="handleLoginSuccess"
  />
</template>

<script setup>
/**
 * 发布宠物页面组件
 * 提供宠物信息录入、图片上传及发布功能
 */
import { ref } from 'vue'
import { post } from '../utils/request.js'
import Login from './Login.vue'
import { success, warning, error } from '../utils/message.js'

const emit = defineEmits(['needLogin'])

const form = ref({
  name: '',
  type: '',
  gender: '',
  age: 1,
  description: '',
  isVaccinated: false,
  isNeutered: false,
  isDewormed: false,
  tags: []
})

const loading = ref(false)
const imageList = ref([])
const uploadedUrls = ref([])
const showForm = ref(false)
const showLoginDialog = ref(false)

/**
 * 处理"开始发布"按钮点击，校验登录状态
 */
const handleStartPublish = () => {
  if (!localStorage.getItem('user')) {
    warning('请先登录才能发布宠物')
    showLoginDialog.value = true
    return
  }
  showForm.value = true
}

/**
 * 登录成功回调
 */
const handleLoginSuccess = () => {
  showForm.value = true
}

/**
 * 增加年龄
 */
const increaseAge = () => {
  form.value.age++
}

/**
 * 减少年龄
 */
const decreaseAge = () => {
  if (form.value.age > 0) {
    form.value.age--
  }
}

/**
 * 上传前校验：检查文件类型是否为图片且大小不超过 5MB
 */
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    error('图片大小不能超过5MB')
    return false
  }
  return true
}

/**
 * 图片上传成功回调
 */
const handleUploadSuccess = (response, file, fileList) => {
  if (response.url) {
    uploadedUrls.value.push(response.url)
    success('图片上传成功')
  }
}

/**
 * 图片上传失败回调
 */
const handleUploadError = () => {
  error('图片上传失败')
}

/**
 * 提交发布宠物信息
 * 校验必填项后，将表单数据与图片 URL 发送至后端
 */
const publishPet = async () => {
  if (!form.value.name || !form.value.type || !form.value.description) {
    warning('请填写完整信息')
    return
  }

  const user = JSON.parse(localStorage.getItem('user'))
  const data = {
    name: form.value.name,
    type: form.value.type,
    gender: form.value.gender,
    age: form.value.age,
    description: form.value.description,
    username: user.username,
    tags: JSON.stringify(form.value.tags),
    isVaccinated: form.value.isVaccinated,
    isNeutered: form.value.isNeutered,
    isDewormed: form.value.isDewormed
  }

  if (uploadedUrls.value.length > 0) {
    data.photoUrl = uploadedUrls.value[0]
    data.photoUrls = JSON.stringify(uploadedUrls.value)
  }

  try {
    await post('/api/pet/publish', data)
    success('宠物发布成功！请等待管理员审核')

    form.value = {
      name: '',
      type: '',
      gender: '公',
      age: 1,
      description: '',
      isVaccinated: false,
      isNeutered: false,
      isDewormed: false,
      tags: []
    }
    uploadedUrls.value = []
    imageList.value = []

    showForm.value = false

  } catch (err) {
    error(err.message)
  }
}
</script>

<style scoped>
.publish-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 100px);
}

/* ===== 欢迎页样式 - 保持原样 ===== */
.welcome-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 140px);
  position: relative;
}

.welcome-card {
  background: white;
  border-radius: 32px;
  padding: 60px 80px;
  box-shadow: 0 20px 60px rgba(61, 64, 91, 0.15);
  text-align: center;
  max-width: 800px;
  width: 100%;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.title-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.title-section h1 {
  font-size: 42px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  font-family: 'Comic Sans MS', '楷体', cursive;
  letter-spacing: 2px;
}

.emoji-left, .emoji-right {
  font-size: 48px;
  animation: bounce 2s infinite;
}

.emoji-left {
  animation-delay: 0s;
}

.emoji-right {
  animation-delay: 1s;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.banner-wrapper {
  margin: 30px 0;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.welcome-banner {
  width: 100%;
  height: 180px;
  object-fit: cover;
  object-position: center top;
  display: block;
}

.subtitle {
  font-size: 18px;
  color: #6b7280;
  margin: 25px 0 35px;
  font-weight: 400;
}

.cta-button-wrapper {
  margin-bottom: 30px;
}

.start-publish-btn {
  background: linear-gradient(135deg, #cc785c 0%, #a9583e 100%) !important;
  border: none !important;
  border-radius: 16px !important;
  padding: 18px 48px !important;
  font-size: 18px !important;
  font-weight: 600 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  box-shadow: 0 8px 24px rgba(204, 120, 92, 0.3) !important;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.start-publish-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.start-publish-btn:hover::before {
  left: 100%;
}

.start-publish-btn:hover {
  transform: translateY(-3px) scale(1.02) !important;
  box-shadow: 0 12px 32px rgba(204, 120, 92, 0.4) !important;
}

.start-publish-btn:active {
  transform: translateY(-1px) scale(0.98) !important;
}

.btn-icon {
  margin-right: 10px;
  font-size: 22px;
}

.doodle-line {
  width: 150px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #E07A5F, #F2CC8F, #81B29A, transparent);
  margin: 0 auto;
  border-radius: 2px;
}

/* 小贴士悬浮卡片 - 保持原样 */
.tips-floating {
  position: fixed;
  right: 60px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}

.tip-card {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 12px 40px rgba(61, 64, 91, 0.12);
  border: 1px solid #e5e7eb;
  width: 320px;
  animation: slideInRight 0.8s ease-out 0.3s both;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.tip-card h3 {
  font-size: 18px;
  color: #374151;
  margin: 0 0 20px 0;
  padding-bottom: 15px;
  border-bottom: 2px solid #e5e7eb;
  font-weight: 600;
}

.tip-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tip-list li {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.tip-list li:last-child {
  margin-bottom: 0;
}

.tip-list li:hover {
  background: #f3f4f6;
  transform: translateX(5px);
}

.tip-icon {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #cc785c, #a9583e);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
}

.tip-list li strong {
  display: block;
  color: #374151;
  margin-bottom: 5px;
  font-size: 15px;
  font-weight: 600;
}

.tip-list li p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

/* ===== 表单页样式 - 柔和现代风格 ===== */
.form-view {
  max-width: 950px;
  margin: 0 auto;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.form-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.back-btn {
  width: 48px;
  height: 48px;
  font-size: 20px;
  border-radius: 50% !important;
  background: #ffffff !important;
  border: 2px solid #e8d5c4 !important;
  color: #cc785c !important;
  font-weight: 700 !important;
  box-shadow: 0 2px 8px rgba(204, 120, 92, 0.15) !important;
  transition: all 0.2s ease !important;
}

.back-btn:hover {
  background: #fffaf5 !important;
  border-color: #cc785c !important;
  transform: translateX(-3px);
  box-shadow: 0 4px 12px rgba(204, 120, 92, 0.25) !important;
}

.back-btn:active {
  transform: translateX(0);
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

/* 表单卡片 - 柔和现代风格 */
.form-card {
  background: #ffffff;
  border: 1px solid #f0e6d6;
  border-radius: 20px;
  padding: 50px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}

.custom-form {
  margin-top: 10px;
}

.form-row {
  margin-bottom: 28px;
}

/* 限制短输入框的宽度 */
.form-row-short {
  max-width: 520px;
}

/* 性别单选框 */
.gender-radio {
  display: flex;
  gap: 20px;
  align-items: center;
}

/* 输入框 - 柔和现代风格 */
.styled-input :deep(.el-input__wrapper) {
  border: 2px solid #e8d5c4 !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  background: #fffaf5 !important;
  padding: 12px 16px !important;
  transition: all 0.2s ease !important;
}

.styled-input :deep(.el-input__wrapper:hover) {
  border-color: #d4a574 !important;
  background: #fff3e6 !important;
}

.styled-input :deep(.el-input__wrapper.is-focus) {
  border-color: #cc785c !important;
  box-shadow: 0 0 0 4px rgba(204, 120, 92, 0.1) !important;
  background: #ffffff !important;
}

/* 年龄步进器 - 保留动画 */
.age-stepper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stepper-btn {
  width: 44px;
  height: 44px;
  border-radius: 50% !important;
  border: 2px solid #e8d5c4 !important;
  background: #fffaf5 !important;
  color: #cc785c !important;
  font-size: 22px !important;
  font-weight: 700 !important;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(204, 120, 92, 0.15) !important;
  transition: all 0.15s ease !important;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  padding: 0 !important;
}

.stepper-btn:hover {
  background: #fff3e6 !important;
  border-color: #cc785c !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(204, 120, 92, 0.25) !important;
}

.stepper-btn:active {
  transform: scale(0.9) !important;
  box-shadow: 0 1px 4px rgba(204, 120, 92, 0.15) !important;
}

.stepper-display {
  width: 70px;
  height: 44px;
  border: 2px solid #e8d5c4 !important;
  border-radius: 12px !important;
  background: #fffaf5 !important;
  text-align: center !important;
  font-size: 18px !important;
  font-weight: 700 !important;
  color: #2d3748 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  outline: none;
  -moz-appearance: textfield;
  -webkit-appearance: none;
}

.stepper-display:focus {
  border-color: #cc785c !important;
  box-shadow: 0 0 0 4px rgba(204, 120, 92, 0.1) !important;
  background: #ffffff !important;
}

.stepper-display::-webkit-outer-spin-button,
.stepper-display::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.unit-text {
  margin-left: 8px;
  color: #718096;
  font-size: 15px;
  font-weight: 600;
}

/* 文本域 - 柔和风格 */
.styled-textarea :deep(.el-textarea__inner) {
  border: 2px solid #e8d5c4 !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  background: #fffaf5 !important;
  padding: 14px 18px !important;
  transition: all 0.2s ease !important;
  font-size: 14px;
}

.styled-textarea :deep(.el-textarea__inner:hover) {
  border-color: #d4a574 !important;
  background: #fff3e6 !important;
}

.styled-textarea :deep(.el-textarea__inner:focus) {
  border-color: #cc785c !important;
  box-shadow: 0 0 0 4px rgba(204, 120, 92, 0.1) !important;
  background: #ffffff !important;
}

/* 健康状态复选框 - 保留不规则圆角 */
.health-checkboxes {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.custom-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  position: relative;
  cursor: pointer;
  user-select: none;
}

.custom-checkbox input[type="checkbox"] {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  width: 0;
  height: 0;
}

.custom-checkbox .checkmark {
  position: relative;
  height: 1.6em;
  width: 1.6em;
  background-color: #fffaf5;
  border: 3px solid #e8d5c4;
  border-radius: 20% 80% 10% 90% / 90% 10% 80% 20%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  flex-shrink: 0;
}

.custom-checkbox:hover .checkmark {
  transform: scale(1.05) rotate(2deg);
  border-color: #cc785c;
}

.custom-checkbox input:checked ~ .checkmark {
  background-color: #cc785c;
  border-color: #cc785c;
  border-radius: 80% 20% 90% 10% / 10% 90% 20% 80%;
  transform: scale(1.1) rotate(-2deg);
}

.custom-checkbox .checkmark:after {
  content: "";
  position: absolute;
  display: none;
  left: 0.42em;
  top: 0.12em;
  width: 0.32em;
  transform: translate(-50%, -50%) rotate(40deg);
  height: 0.65em;
  border: solid #ffffff;
  border-width: 0 0.22em 0.22em 0;
  border-radius: 2px;
}

.custom-checkbox input:checked ~ .checkmark:after {
  display: block;
  animation: splash 0.3s forwards;
}

.custom-checkbox:active .checkmark {
  transform: scale(0.9) translateY(3px);
  box-shadow: 0px 0px 0px;
}

@keyframes splash {
  0% {
    transform: scale(0) rotate(40deg);
    opacity: 0;
  }
  70% {
    transform: scale(1.2) rotate(40deg);
  }
  100% {
    transform: scale(1) rotate(40deg);
    opacity: 1;
  }
}

.checkbox-label {
  font-size: 15px;
  color: #2d3748;
  font-weight: 700;
}

/* ===== 性别单选框样式  ===== */
.gender-radio :deep(.el-radio) {
  margin-right: 0 !important;
}

.gender-radio :deep(.el-radio__input) {
  width: 24px !important;
  height: 24px !important;
}

.gender-radio :deep(.el-radio__inner) {
  width: 24px !important;
  height: 24px !important;
  border: 3px solid #e8d5c4 !important;
  border-radius: 20% 80% 10% 90% / 90% 10% 80% 20% !important;
  background: #fffaf5 !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06) !important;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275) !important;
}

.gender-radio :deep(.el-radio__inner::after) {
  width: 10px !important;
  height: 10px !important;
  background: #cc785c !important;
  border-radius: 50% !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) scale(0) !important;
  transition: all 0.2s !important;
}

.gender-radio :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #cc785c !important;
  border-color: #cc785c !important;
  border-radius: 80% 20% 90% 10% / 10% 90% 20% 80% !important;
  transform: scale(1.1) rotate(-2deg) !important;
  box-shadow: 0 2px 8px rgba(204, 120, 92, 0.3) !important;
}

.gender-radio :deep(.el-radio__input.is-checked .el-radio__inner::after) {
  transform: translate(-50%, -50%) scale(1) !important;
  background: #ffffff !important;
}

.gender-radio :deep(.el-radio__label) {
  font-weight: 700 !important;
  color: #2d3748 !important;
  font-size: 15px !important;
}

/* ===== 表单左侧标签文字加粗 ===== */
.custom-form :deep(.el-form-item__label) {
  font-weight: 700 !important;
  color: #2d3748 !important;
  font-size: 15px !important;
}

/* 上传区域 - 柔和风格 */
.photo-upload-section {
  margin-top: 30px;
}

.upload-area {
  background: #fffaf5;
  border: 2px dashed #e8d5c4;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  box-shadow: none;
  transition: all 0.2s ease;
}

.upload-area:hover {
  border-color: #cc785c;
  background: #fff3e6;
}

.custom-upload {
  width: 100%;
}

.upload-placeholder {
  padding: 30px;
}

.upload-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 10px;
}

.upload-placeholder p {
  margin: 5px 0;
  color: #2d3748;
  font-weight: 600;
}

.upload-hint {
  font-size: 12px;
  color: #a0aec0;
}

/* 提交区域  */
.submit-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #f0e6d6;
  display: flex;
  justify-content: center;
}

.submit-btn {
  background: #cc785c !important;
  border: 3px solid #1f2937 !important;
  border-radius: 12px !important;
  padding: 18px 64px !important;
  font-weight: 800 !important;
  color: #ffffff !important;
  font-size: 20px !important;
  box-shadow: 5px 5px 0px #1f2937 !important;
  transition: all 0.2s ease !important;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.submit-btn:hover::before {
  left: 100%;
}

.submit-btn:hover {
  background: #b3654c !important;
  transform: translate(-2px, -2px) !important;
  box-shadow: 7px 7px 0px #1f2937 !important;
}

.submit-btn:active {
  transform: translate(0, 0) !important;
  box-shadow: 3px 3px 0px #1f2937 !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .tips-floating {
    position: static;
    transform: none;
    margin-top: 40px;
    width: 100%;
    display: flex;
    justify-content: center;
  }

  .tip-card {
    width: 100%;
    max-width: 400px;
  }
}

@media (max-width: 768px) {
  .welcome-card {
    padding: 40px 30px;
  }

  .title-section h1 {
    font-size: 28px;
  }

  .form-card {
    padding: 30px;
  }

  .form-header h2 {
    font-size: 24px;
  }
}
</style>
