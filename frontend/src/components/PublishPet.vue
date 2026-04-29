<template>
  <div class="publish-container">
    <!-- 欢迎页视图 -->
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
              @click="showForm = true">
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
                <strong>性格描述</strong>
                <p>告诉领养者它的特点</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 表单页视图 -->
    <div v-else class="form-view">
      <div class="form-header">
        <el-button
            class="back-btn"
            @click="showForm = false"
            circle>
          ←
        </el-button>
        <h2>🐾 发布宠物信息</h2>
      </div>

      <div class="form-card">
        <el-form :model="form" label-width="120px" class="custom-form">
          <div class="form-row">
            <el-form-item label="名字">
              <el-input v-model="form.name" placeholder="给它取个好听的名字吧" class="styled-input" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="种类">
              <el-input v-model="form.type" placeholder="猫 / 狗 / 其他" class="styled-input" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="性别">
              <el-select v-model="form.gender" placeholder="请选择性别" class="styled-input">
                <el-option label="♂ 公" value="公" />
                <el-option label="♀ 母" value="母" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="年龄">
              <el-input-number v-model="form.age" :min="0" :max="30" class="styled-input-number" />
              <span class="unit-text">岁</span>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="性格描述">
              <el-input
                  type="textarea"
                  v-model="form.description"
                  rows="4"
                  placeholder="描述它的性格、健康状况、是否已绝育、是否需要特殊照顾..."
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
</template>

<script setup>
/**
 * 发布宠物页面组件
 * 提供宠物信息录入、图片上传及发布功能
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { post } from '../utils/request.js'

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

/**
 * 上传前校验：检查文件类型是否为图片且大小不超过 5MB
 */
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
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
    ElMessage.success('图片上传成功')
  }
}

/**
 * 图片上传失败回调
 */
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

/**
 * 提交发布宠物信息
 * 校验必填项与登录状态后，将表单数据与图片 URL 发送至后端
 */
const publishPet = async () => {
  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录才能发布宠物')
    emit('needLogin')
    return
  }

  if (!form.value.name || !form.value.type || !form.value.description) {
    ElMessage.warning('请填写完整信息')
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
    // 新增：传递健康状态
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
    ElMessage.success('宠物发布成功！请等待管理员审核')

    // 重置表单
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

    // 发布成功后返回欢迎页
    showForm.value = false

  } catch (error) {
    ElMessage.error(error.message)
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

/* ===== 欢迎页样式 ===== */
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

/* 小贴士悬浮卡片 */
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

/* ===== 表单页样式 ===== */
.form-view {
  max-width: 900px;
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
  background: #f3f4f6 !important;
  border: none !important;
  transition: all 0.3s ease !important;
}

.back-btn:hover {
  background: #e5e7eb !important;
  transform: scale(1.1) !important;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  letter-spacing: 1px;
}

.form-card {
  background: white;
  border-radius: 24px;
  padding: 50px;
  box-shadow: 0 12px 40px rgba(61, 64, 91, 0.1);
  border: 1px solid #e5e7eb;
}

.custom-form {
  margin-top: 10px;
}

.form-row {
  margin-bottom: 20px;
}

.styled-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.styled-input :deep(.el-input__wrapper:hover) {
  border-color: #f2cc8f;
}

.styled-input :deep(.el-input__wrapper.is-focus) {
  border-color: #cc785c;
  box-shadow: 0 0 0 4px rgba(204, 120, 92, 0.1);
}

.styled-input-number :deep(.el-input-number) {
  width: 200px;
}

.styled-input-number :deep(.el-input-number .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e5e7eb;
}

.unit-text {
  margin-left: 12px;
  color: #6b7280;
  font-size: 15px;
  font-weight: 500;
}

.styled-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  padding: 14px 18px;
  transition: all 0.3s ease;
  font-size: 14px;
}

.styled-textarea :deep(.el-textarea__inner:focus) {
  border-color: #cc785c;
  box-shadow: 0 0 0 4px rgba(204, 120, 92, 0.1);
}

.health-checkboxes {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

/* 自定义复选框容器 */
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
  background-color: #fdfcf0;
  border: 3px solid #1a1a1a;
  border-radius: 8% 92% 12% 88% / 87% 11% 89% 13%;
  box-shadow: 4px 4px 0px #1a1a1a;
  transition:
      transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275),
      box-shadow 0.2s;
  flex-shrink: 0;
}

.custom-checkbox:hover .checkmark {
  transform: scale(1.05) rotate(2deg);
}

.custom-checkbox input:checked ~ .checkmark {
  background-color: #cc785c;
  border-radius: 92% 8% 88% 12% / 11% 87% 13% 89%;
  transform: scale(1.05) rotate(-2deg);
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
  border: solid #1a1a1a;
  border-width: 0 0.22em 0.22em 0;
  border-radius: 2px;
}

.custom-checkbox input:checked ~ .checkmark:after {
  display: block;
  animation: splash 0.3s forwards;
}

.custom-checkbox:active .checkmark {
  transform: scale(0.9) translateY(3px);
  box-shadow: 0px 0px 0px #1a1a1a;
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
  color: #374151;
  font-weight: 500;
}

.photo-upload-section {
  margin-top: 30px;
}

.upload-area {
  background: #f9fafb;
  border: 2px dashed #d1d5db;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #cc785c;
  background: #fff9f6;
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
  color: #6b7280;
}

.upload-hint {
  font-size: 12px;
  color: #9ca3af;
}

.submit-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #f3f4f6;
}

.submit-btn {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  border: none;
  border-radius: 16px;
  padding: 12px 32px;
  font-weight: 600;
  position: relative;
  overflow: hidden;
  z-index: 1;
  transition: all 0.3s ease;
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
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(224, 122, 95, 0.3);
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
