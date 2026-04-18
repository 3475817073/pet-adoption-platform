<template>
  <el-dialog
      :model-value="modelValue"
      width="520px"
      @close="handleClose"
      class="login-dialog"
      :show-close="false">
    <div class="login-header">
      <svg class="header-bg" viewBox="0 0 520 200" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <pattern id="paw-pattern" x="0" y="0" width="60" height="60" patternUnits="userSpaceOnUse">
            <text x="10" y="30" font-size="20" opacity="0.15">🐾</text>
          </pattern>
        </defs>

        <rect width="520" height="200" fill="#E07A5F"/>
        <rect width="520" height="200" fill="url(#paw-pattern)"/>

        <circle cx="80" cy="100" r="120" fill="#F2CC8F" opacity="0.3"/>
        <circle cx="450" cy="50" r="80" fill="#81B29A" opacity="0.25"/>
        <circle cx="300" cy="180" r="100" fill="#FDF8F3" opacity="0.15"/>

        <ellipse cx="150" cy="120" rx="80" ry="40" fill="#81B29A" opacity="0.2" transform="rotate(-15 150 120)"/>
        <ellipse cx="400" cy="140" rx="60" ry="30" fill="#F2CC8F" opacity="0.25" transform="rotate(20 400 140)"/>

        <text x="50" y="60" font-size="40" opacity="0.3">🐱</text>
        <text x="380" y="90" font-size="35" opacity="0.25">🐶</text>
        <text x="250" y="170" font-size="30" opacity="0.2">❤️</text>

        <text x="260" y="105" text-anchor="middle" font-size="32" font-weight="bold" fill="white" style="text-shadow: 0 2px 8px rgba(0,0,0,0.2)">
          {{ tab === 'login' ? '欢迎回来' : '加入我们' }}
        </text>
        <text x="260" y="135" text-anchor="middle" font-size="14" fill="rgba(255,255,255,0.95)">
          {{ tab === 'login' ? '登录你的账户，继续领养之旅' : '创建一个新账户，开始你的宠物之旅' }}
        </text>
      </svg>
    </div>

    <el-tabs v-model="tab" class="login-tabs">
      <el-tab-pane label="登录" name="login">
        <el-form :model="loginForm" label-width="0px" class="login-form">
          <el-form-item>
            <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                prefix-icon="User"
                class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                prefix-icon="Lock"
                show-password
                class="custom-input" />
          </el-form-item>
          <el-button style="width:100%" type="primary" @click="doLogin" :loading="loginLoading" class="submit-btn">
            登录
          </el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="注册" name="register">
        <el-form :model="registerForm" label-width="0px" class="login-form">
          <el-form-item>
            <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                prefix-icon="User"
                class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（至少6位）"
                size="large"
                prefix-icon="Lock"
                show-password
                class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                prefix-icon="Lock"
                show-password
                class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                size="large"
                prefix-icon="Avatar"
                class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号（选填）"
                size="large"
                prefix-icon="Phone"
                class="custom-input" />
          </el-form-item>
          <el-button style="width:100%" type="primary" @click="doRegister" :loading="registerLoading" class="submit-btn">
            注册
          </el-button>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <div class="login-footer">
      <span>{{ tab === 'login' ? '还没有账户？' : '已有账户？' }}</span>
      <el-link type="primary" @click="tab = tab === 'login' ? 'register' : 'login'" :underline="false">
        {{ tab === 'login' ? '立即注册' : '去登录' }}
      </el-link>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * 登录/注册弹窗组件
 * 提供用户登录和注册功能，支持表单校验与后端 API 交互
 */
import { ref, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import { post } from '../utils/request.js'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'loginSuccess'])

const tab = ref('login')
const loginForm = ref({ username: '', password: '' })

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
})

const loginLoading = ref(false)
const registerLoading = ref(false)

/**
 * 处理弹窗关闭事件，向父组件同步显示状态
 */
const handleClose = () => {
  emit('update:modelValue', false)
}

/**
 * 执行用户登录操作
 * 校验输入后请求后端登录接口，成功后触发登录成功事件
 */
const doLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }

  loginLoading.value = true
  try {
    const data = await post('/api/user/login', loginForm.value)

    ElMessage.success('登录成功！')
    emit('loginSuccess', data)
    emit('update:modelValue', false)
    loginForm.value = { username: '', password: '' }

  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loginLoading.value = false
  }
}

/**
 * 执行用户注册操作
 */
const doRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.realName) {
    ElMessage.warning('请填写必填项')
    return
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  if (registerForm.value.password.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }

  registerLoading.value = true
  try {
    //使用解构赋值剔除 confirmPassword，避免提交给后端
    const { confirmPassword, ...submitData } = registerForm.value
    await post('/api/user/register', submitData)

    ElMessage.success('注册成功！')
    registerForm.value = {
      username: '',
      password: '',
      confirmPassword: '',
      realName: '',
      phone: ''
    }
    tab.value = 'login'

  } catch (error) {
    // 增强错误提示，方便调试
    console.error("注册失败详情:", error)
    ElMessage.error(error.message || '注册失败，请检查输入或联系管理员')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
}

.login-dialog :deep(.el-dialog__header) {
  display: none;
}

.login-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.login-header {
  position: relative;
  overflow: hidden;
}

.header-bg {
  width: 100%;
  height: auto;
  display: block;
}

.login-tabs {
  padding: 30px 40px 20px;
}

.login-tabs :deep(.el-tabs__header) {
  margin: 0 0 24px;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  color: #6B7280;
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: #E07A5F;
}

.login-tabs :deep(.el-tabs__active-bar) {
  background-color: #E07A5F;
}

.login-form {
  padding: 0 4px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(61,64,91,0.08);
  padding: 8px 16px;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(224, 122, 95, 0.2);
}

.custom-select :deep(.el-select__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(61,64,91,0.08);
}

.submit-btn {
  margin-top: 8px;
  background: #E07A5F !important;
  border-color: #E07A5F !important;
  border-radius: 12px !important;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 500 !important;
}

.submit-btn:hover {
  background: #D06A4F !important;
  border-color: #D06A4F !important;
}

.login-footer {
  text-align: center;
  padding: 20px 40px 30px;
  color: #6B7280;
  font-size: 14px;
}

.login-footer .el-link {
  margin-left: 8px;
  color: #E07A5F;
  font-weight: 500;
}
</style>
