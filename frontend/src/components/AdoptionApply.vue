<template>
  <el-dialog
      v-model="visible"
      :title="`申请领养 - ${pet.name}`"
      width="600px"
      class="apply-dialog"
      align-center
  >
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

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
          type="primary"
          @click="submitApply"
          :loading="loading"
          class="submit-btn"
      >
        提交申请
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { post } from '../utils/request.js'

const props = defineProps({ pet: Object })
const emit = defineEmits(['close', 'success'])

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
    ElMessage.warning('请填写完整的领养信息')
    return
  }

  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    visible.value = false
    return
  }

  let user
  try {
    user = JSON.parse(userStr)
  } catch {
    ElMessage.error('用户信息错误')
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

    ElMessage.success('申请提交成功！请等待管理员审核')
    emit('success')
    visible.value = false
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

watch(visible, (newVal) => {
  if (!newVal) emit('close')
})
</script>


<style scoped>
.apply-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;
}

.apply-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  color: #333;
}

.apply-form {
  padding: 10px 0;
}

.horizontal-radio {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.no-wrap-label :deep(.el-form-item__label) {
  white-space: nowrap;
}

.submit-btn {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  border: none;
}

.submit-btn:hover {
  opacity: 0.9;
}
</style>
