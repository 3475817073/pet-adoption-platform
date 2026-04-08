<template>
  <el-dialog v-model="visible" :title="`申请领养 - ${pet.name}`" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="领养理由" required>
        <el-input type="textarea" v-model="form.reason" rows="4" placeholder="请详细说明为什么想领养这只宠物" />
      </el-form-item>
      <el-form-item label="家庭情况">
        <el-input type="textarea" v-model="form.family" rows="3" placeholder="家庭成员、住房情况等（选填）" />
      </el-form-item>
      <el-form-item label="联系方式" required>
        <el-input v-model="form.contact" placeholder="手机号或微信" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submitApply" :loading="loading">提交申请</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({ pet: Object })
const emit = defineEmits(['close', 'success'])

const visible = ref(true)
const loading = ref(false)

const form = ref({
  reason: '',
  family: '',
  contact: ''
})

const submitApply = async () => {
  if (!form.value.reason.trim() || !form.value.contact.trim()) {
    ElMessage.warning('请填写领养理由和联系方式')
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
    const res = await fetch('http://localhost:8080/api/adoption/apply', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: user.username,
        petId: props.pet.id,
        reason: form.value.reason,
        family: form.value.family,
        contact: form.value.contact
      })
    })

    if (res.ok) {
      const result = await res.text()
      ElMessage.success(result)
      emit('success')
      visible.value = false
    } else {
      const error = await res.text()
      ElMessage.error(error)
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

watch(visible, (newVal) => {
  if (!newVal) emit('close')
})
</script>
