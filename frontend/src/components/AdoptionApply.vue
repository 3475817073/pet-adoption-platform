<template>
  <el-dialog
      v-model="visible"
      width="600px"
      class="apply-dialog"
      align-center
      :show-close="false"
  >
    <template #header></template>

    <div class="dialog-stack-wrapper">
      <div class="dialog-custom-header">
        <span class="dialog-title">申请领养 - {{ pet.name }}</span>
        <button class="dialog-close" @click="visible = false">×</button>
      </div>

      <div class="dialog-custom-body">
        <el-form :model="form" label-width="110px" class="apply-form">
          <el-form-item label="领养理由" required>
            <el-input
                type="textarea"
                v-model="form.reason"
                rows="4"
                placeholder="请详细说明为什么想领养这只宠物"
            />
          </el-form-item>

          <el-form-item label="家庭情况">
            <el-input
                type="textarea"
                v-model="form.family"
                rows="3"
                placeholder="家庭成员、住房情况等（选填）"
            />
          </el-form-item>

          <el-form-item label="联系方式" required>
            <el-input v-model="form.contact" placeholder="手机号或微信" />
          </el-form-item>

          <el-form-item label="居住类型" required>
            <el-radio-group v-model="form.residenceType" class="horizontal-radio">
              <el-radio label="自有住房">自有住房</el-radio>
              <el-radio label="租房">租房</el-radio>
              <el-radio label="宿舍">宿舍</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="养宠经验" required>
            <el-radio-group v-model="form.petExperience" class="horizontal-radio">
              <el-radio label="无经验">无经验，第一次养</el-radio>
              <el-radio label="有过经验">有过养宠经验</el-radio>
              <el-radio label="丰富经验">丰富经验（养过 3 只以上）</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="是否有其他宠物" class="no-wrap-label">
            <el-switch v-model="form.hasOtherPets" />
          </el-form-item>

          <el-form-item v-if="form.hasOtherPets" label="其他宠物情况">
            <el-input
                v-model="form.otherPetsInfo"
                type="textarea"
                rows="2"
                placeholder="例如：1 只猫咪，已绝育"
            />
          </el-form-item>
        </el-form>
      </div>

      <div class="dialog-custom-footer">
        <el-button @click="visible = false" class="cancel-btn">取消</el-button>
        <el-button
            type="primary"
            @click="submitApply"
            :loading="loading"
            class="submit-btn"
        >
          提交申请
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch, inject } from 'vue'
import { post } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'

const props = defineProps({ pet: Object })
const emit = defineEmits(['close', 'success'])

const triggerLogin = inject('triggerLogin')

const visible = ref(true)
const loading = ref(false)

const form = ref({
  reason: '',
  family: '',
  contact: '',
  residenceType: '',
  petExperience: '',
  hasOtherPets: false,
  otherPetsInfo: ''
})

const submitApply = async () => {
  if (!form.value.reason.trim() || !form.value.contact.trim() || !form.value.residenceType || !form.value.petExperience) {
    warning('请填写完整的领养信息')
    return
  }

  const userStr = localStorage.getItem('user')
  if (!userStr) {
    warning('请先登录')
    visible.value = false
    triggerLogin()
    return
  }

  let user
  try {
    user = JSON.parse(userStr)
  } catch {
    error('用户信息错误')
    return
  }

  loading.value = true

  try {
    await post('/api/adoption/apply', {
      username: user.username,
      petId: props.pet.id,
      reason: form.value.reason,
      family: form.value.family,
      contact: form.value.contact,
      residenceType: form.value.residenceType,
      petExperience: form.value.petExperience,
      hasOtherPets: form.value.hasOtherPets,
      otherPetsInfo: form.value.otherPetsInfo
    })

    success('申请提交成功！请等待管理员审核')
    emit('success')
    visible.value = false
  } catch (err) {
    error(err.message)
  } finally {
    loading.value = false
  }
}

watch(visible, (newVal) => {
  if (!newVal) emit('close')
})
</script>


<style scoped>
/* ===== 隐藏 Element Plus 默认外壳 ===== */
.apply-dialog :deep(.el-overlay) {
  background: rgba(0, 0, 0, 0.5);
}

.apply-dialog :deep(.el-dialog) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  padding: 0 !important;
  margin: 0 !important;
}

.apply-dialog :deep(.el-dialog__header),
.apply-dialog :deep(.el-dialog__body),
.apply-dialog :deep(.el-dialog__footer) {
  display: none !important;
  padding: 0 !important;
  margin: 0 !important;
}

/* ===== 堆叠包装容器  ===== */
.dialog-stack-wrapper {
  position: relative;
  background: #ffffff;
  border: 4px solid #1f2937;
  border-radius: 12px;
  transition: all 0.15s ease;
}

.dialog-stack-wrapper::before,
.dialog-stack-wrapper::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 4px solid #1f2937;
  border-radius: 12px;
  background: #ffffff;
  z-index: -1;
  transition: all 0.15s ease;
}

/* 默认状态：前后层旋转 ±6° */
.dialog-stack-wrapper::before {
  transform: translateY(-2%) rotate(-6deg);
}
.dialog-stack-wrapper::after {
  transform: translateY(2%) rotate(6deg);
}

/* Hover 状态：外壳旋转幅度变大 */
.dialog-stack-wrapper:hover::before {
  transform: translateY(-2%) rotate(-8deg);
}
.dialog-stack-wrapper:hover::after {
  transform: translateY(2%) rotate(8deg);
}

/* ===== 自定义标题栏 ===== */
.dialog-custom-header {
  background: #cc785c;
  border-bottom: 3px solid #1f2937;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 2;
}

.dialog-title {
  font-weight: 800;
  color: #ffffff;
  font-size: 18px;
  letter-spacing: 0.5px;
}

.dialog-close {
  background: transparent;
  border: none;
  color: #ffffff;
  font-size: 28px;
  font-weight: 900;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.dialog-close:hover {
  transform: scale(1.2);
}

/* ===== 自定义内容区 ===== */
.dialog-custom-body {
  padding: 24px;
  background: #ffffff;
  position: relative;
  z-index: 2;
}

/* ===== 自定义底部区 ===== */
.dialog-custom-footer {
  padding: 20px 24px;
  border-top: 2px dashed #e5e7eb;
  background: #f9fafb;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  position: relative;
  z-index: 2;
}

/* ===== 输入框 ===== */
.apply-form {
  padding: 10px 0;
}

.apply-form :deep(.el-textarea__inner),
.apply-form :deep(.el-input__wrapper) {
  border: 3px solid #1f2937 !important;
  border-radius: 4px !important;
  box-shadow: 5px 5px 0px #1f2937, 10px 10px 0px #cc785c !important;
  background: #ffffff !important;
  padding: 12px 16px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  color: #1f2937 !important;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
}

.apply-form :deep(.el-textarea__inner:focus),
.apply-form :deep(.el-input__wrapper.is-focus) {
  animation: focus-pulse 4s cubic-bezier(0.25, 0.8, 0.25, 1) infinite !important;
  box-shadow: 3px 3px 0px #1f2937, 6px 6px 0px #cc785c !important;
}

@keyframes focus-pulse {
  0%, 100% { border-color: #1f2937 !important; }
  50% { border-color: #cc785c !important; }
}

.apply-form :deep(.el-input__inner::placeholder),
.apply-form :deep(.el-textarea__inner::placeholder) {
  color: #9ca3af !important;
  font-weight: 400 !important;
}

.apply-form :deep(.el-input__inner:focus::placeholder),
.apply-form :deep(.el-textarea__inner:focus::placeholder) {
  color: transparent !important;
}

/* ===== 单选框 ===== */
.horizontal-radio {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.horizontal-radio :deep(.el-radio) {
  margin-right: 0 !important;
  position: relative;
}

.horizontal-radio :deep(.el-radio__input) {
  width: 24px !important;
  height: 24px !important;
}

.horizontal-radio :deep(.el-radio__inner) {
  width: 24px !important;
  height: 24px !important;
  border: 4px solid #1f2937 !important;
  border-radius: 20% 80% 10% 90% / 90% 10% 80% 20% !important;
  background: #fdfcf0 !important;
  box-shadow: 5px 5px 0px #1f2937 !important;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275) !important;
}

.horizontal-radio :deep(.el-radio__inner::after) {
  width: 10px !important;
  height: 10px !important;
  background: #1f2937 !important;
  border-radius: 50% !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) scale(0) !important;
  transition: all 0.2s !important;
}

.horizontal-radio :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #ff5722 !important;
  border-radius: 80% 20% 90% 10% / 10% 90% 20% 80% !important;
  transform: scale(1.1) rotate(-2deg) !important;
  box-shadow: 3px 3px 0px #1f2937 !important;
}

.horizontal-radio :deep(.el-radio__input.is-checked .el-radio__inner::after) {
  transform: translate(-50%, -50%) scale(1) !important;
  animation: splash 0.3s forwards !important;
}

.horizontal-radio :deep(.el-radio__label) {
  font-weight: 600 !important;
  color: #1f2937 !important;
  font-size: 14px !important;
}

.horizontal-radio :deep(.el-radio:hover .el-radio__inner) {
  transform: scale(1.05) rotate(2deg) !important;
}

.horizontal-radio :deep(.el-radio:active .el-radio__inner) {
  transform: scale(0.9) translateY(4px) !important;
  box-shadow: 0px 0px 0px #1f2937 !important;
}

@keyframes splash {
  0% { transform: scale(0) translate(-50%, -50%); opacity: 0; }
  70% { transform: scale(1.2) translate(-50%, -50%); }
  100% { transform: scale(1) translate(-50%, -50%); opacity: 1; }
}

/* ===== 开关 ===== */
.apply-form :deep(.el-switch) {
  --el-switch-on-color: #28096b;
  --el-switch-off-color: #28096b;
  width: 56px !important;
  height: 32px !important;
}

.apply-form :deep(.el-switch__core) {
  background-color: #28096b !important;
  border: 2px solid #1f2937 !important;
  border-radius: 30px !important;
  height: 32px !important;
  width: 56px !important;
  transition: all 0.5s !important;
}

.apply-form :deep(.el-switch__action) {
  width: 24px !important;
  height: 24px !important;
  left: 2px !important;
  background: #28096b !important;
  box-shadow: inset 8px -4px 0px 0px #fff000 !important;
  border: none !important;
  transition: all 0.5s !important;
}

.apply-form :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #522ba7 !important;
}

.apply-form :deep(.el-switch.is-checked .el-switch__action) {
  left: calc(100% - 26px) !important;
  box-shadow: inset 15px -4px 0px 15px #fff000 !important;
}

/* ===== 表单项 ===== */
.apply-form :deep(.el-form-item__label) {
  font-weight: 700 !important;
  color: #1f2937 !important;
  font-size: 14px !important;
}

.no-wrap-label :deep(.el-form-item__label) {
  white-space: nowrap !important;
}

/* ===== 提交按钮 ===== */
.submit-btn {
  background: #cc785c !important;
  border: 3px solid #1f2937 !important;
  color: #ffffff !important;
  border-radius: 12px !important;
  font-weight: 800 !important;
  padding: 12px 32px !important;
  box-shadow: 5px 5px 0px #1f2937 !important;
  transition: all 0.2s ease !important;
  font-size: 15px !important;
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

/* ===== 取消按钮 ===== */
.cancel-btn {
  background: #ffffff !important;
  border: 3px solid #1f2937 !important;
  color: #1f2937 !important;
  border-radius: 12px !important;
  font-weight: 800 !important;
  padding: 12px 32px !important;
  box-shadow: 5px 5px 0px #1f2937 !important;
  transition: all 0.2s ease !important;
  font-size: 15px !important;
}

.cancel-btn:hover {
  background: #f3f4f6 !important;
  transform: translate(-2px, -2px) !important;
  box-shadow: 7px 7px 0px #1f2937 !important;
}

.cancel-btn:active {
  transform: translate(0, 0) !important;
  box-shadow: 3px 3px 0px #1f2937 !important;
}
</style>
