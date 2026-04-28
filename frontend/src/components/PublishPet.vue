<template>
  <div class="publish-container">
    <!-- 顶部装饰区 -->
    <div class="header-decoration">
      <div class="handwritten-title">
        <span class="emoji-decoration">🐱</span>
        <h1>为小生命找个家</h1>
        <span class="emoji-decoration">🐶</span>
      </div>
      <p class="subtitle">填写宠物信息，让更多人看到它</p>
      <div class="doodle-line"></div>
    </div>

    <!-- 主要内容区：左侧发布指南与右侧表单 -->
    <div class="content-wrapper">
      <!-- 左侧：发布指南 -->
      <div class="guide-section">
        <div class="guide-card">
          <h3>💡 小贴士</h3>
          <ul class="guide-list">
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

      <!-- 右侧：表单区 -->
      <div class="form-section">
        <div class="form-card">
          <h2>🐾 宠物信息</h2>

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
            <!-- 新增：健康状态勾选 -->
            <div class="form-row">
              <el-form-item label="健康状态">
                <div class="health-checkboxes">
                  <el-checkbox v-model="form.isVaccinated" border size="large">💉 已疫苗</el-checkbox>
                  <el-checkbox v-model="form.isNeutered" border size="large">✂️ 已绝育</el-checkbox>
                  <el-checkbox v-model="form.isDewormed" border size="large">🦠 已驱虫</el-checkbox>
                </div>
              </el-form-item>
            </div>

            <!-- 性格标签选择 -->
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
}

/* 顶部装饰区 */
.header-decoration {
  text-align: center;
  padding: 40px 20px 30px;
  position: relative;
}

.handwritten-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin-bottom: 10px;
}

.handwritten-title h1 {
  font-size: 36px;
  font-weight: 600;
  color: #3D405B;
  margin: 0;
  font-family: 'Comic Sans MS', '楷体', cursive;
}

.emoji-decoration {
  font-size: 32px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.subtitle {
  font-size: 16px;
  color: #6B7280;
  margin: 0;
}

.doodle-line {
  width: 200px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #E07A5F, #F2CC8F, #81B29A, transparent);
  margin: 20px auto 0;
  border-radius: 2px;
}

/* 主要内容区 */
.content-wrapper {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 30px;
  margin-top: 30px;
}

/* 左侧发布指南 */
.guide-section {
  position: sticky;
  top: 20px;
  height: fit-content;
  max-width: 280px;
}


.guide-card {
  background: #F9FAFB;
  border-radius: 12px;
  padding: 18px;
  border: 1px solid #E5E7EB;
}

.guide-card h3 {
  font-size: 14px;
  color: #6B7280;
  margin: 0 0 12px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #E5E7EB;
  font-weight: 500;
}

.guide-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.guide-list li {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  padding: 10px;
  background: white;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.guide-list li:last-child {
  margin-bottom: 0;
}

.tip-icon {
  width: 22px;
  height: 22px;
  background: #D1D5DB;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 12px;
  flex-shrink: 0;
}

.guide-list li strong {
  display: block;
  color: #6B7280;
  margin-bottom: 3px;
  font-size: 13px;
  font-weight: 500;
}

.guide-list li p {
  margin: 0;
  color: #9CA3AF;
  font-size: 12px;
  line-height: 1.4;
}


/* 右侧表单区 */
.form-section {
  min-height: 600px;
}

.form-card {
  background: white;
  border-radius: 24px;
  padding: 35px;
  box-shadow: 0 8px 30px rgba(61, 64, 91, 0.1);
}

.form-card h2 {
  font-size: 24px;
  color: #3D405B;
  margin: 0 0 30px 0;
  padding-bottom: 15px;
  border-bottom: 3px solid #E07A5F;
}

.custom-form {
  margin-top: 20px;
}

.form-row {
  margin-bottom: 5px;
}

.styled-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 10px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 2px solid #E5E7EB;
  transition: all 0.3s ease;
}

.styled-input :deep(.el-input__wrapper:hover) {
  border-color: #F2CC8F;
}

.styled-input :deep(.el-input__wrapper.is-focus) {
  border-color: #E07A5F;
  box-shadow: 0 0 0 3px rgba(224, 122, 95, 0.1);
}

.styled-input-number :deep(.el-input-number) {
  width: 200px;
}

.styled-input-number :deep(.el-input-number .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #E5E7EB;
}

.unit-text {
  margin-left: 10px;
  color: #6B7280;
  font-size: 14px;
}

.styled-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #E5E7EB;
  padding: 12px 16px;
  transition: all 0.3s ease;
}

.styled-textarea :deep(.el-textarea__inner:focus) {
  border-color: #E07A5F;
  box-shadow: 0 0 0 3px rgba(224, 122, 95, 0.1);
}

/* 照片上传区 */
.photo-upload-section {
  margin-top: 25px;
}

.upload-area {
  background: #F9FAFB;
  border: 2px dashed #D1D5DB;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #E07A5F;
  background: #FFF9F6;
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
  color: #6B7280;
}

.upload-hint {
  font-size: 12px;
  color: #9CA3AF;
}

/* 提交按钮 */
.submit-section {
  margin-top: 35px;
  padding-top: 25px;
  border-top: 2px solid #F3F4F6;
}

.submit-btn {
  width: 100%;
  height: 56px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 16px;
  background: #E07A5F !important;
  border-color: #E07A5F !important;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: #D06A4F !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(224, 122, 95, 0.3);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .guide-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .handwritten-title h1 {
    font-size: 24px;
  }

  .form-card {
    padding: 20px;
  }
}.health-checkboxes {
   display: flex;
   gap: 15px;
   flex-wrap: wrap;
 }

</style>
