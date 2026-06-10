<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>登录你的账户继续访问</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-input">
              <el-input
                v-model="form.code"
                placeholder="请输入验证码"
                prefix-icon="Key"
                size="large"
              />
              <el-button
                size="large"
                :disabled="countdown > 0"
                :loading="sendingCode"
                @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              @click="handleLogin"
              :loading="loading"
              class="login-btn"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <el-button text type="primary" @click="showResetDialog = true">
            忘记密码？
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showResetDialog" title="重置密码" width="420px" class="reset-dialog">
      <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef">
        <el-form-item prop="email">
          <el-input
            v-model="resetForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-input">
            <el-input
              v-model="resetForm.code"
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
            />
            <el-button
              size="large"
              :disabled="resetCountdown > 0"
              :loading="sendingResetCode"
              @click="sendResetCode"
            >
              {{ resetCountdown > 0 ? `${resetCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" size="large" @click="handleResetPassword" :loading="resetting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const resetFormRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const showResetDialog = ref(false)
const sendingResetCode = ref(false)
const resetCountdown = ref(0)
const resetting = ref(false)

const form = reactive({
  email: '',
  code: ''
})

const resetForm = reactive({
  email: '',
  code: '',
  newPassword: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const resetRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ]
}

const sendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  sendingCode.value = true
  try {
    const res = await authApi.sendCode({ email: form.email, type: 'login' })
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
  } finally {
    sendingCode.value = false
  }
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await userStore.login(form)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}

const sendResetCode = async () => {
  if (!resetForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  sendingResetCode.value = true
  try {
    const res = await authApi.sendCode({ email: resetForm.email, type: 'reset' })
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      resetCountdown.value = 60
      const timer = setInterval(() => {
        resetCountdown.value--
        if (resetCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
  } finally {
    sendingResetCode.value = false
  }
}

const handleResetPassword = async () => {
  await resetFormRef.value.validate()
  resetting.value = true
  try {
    const res = await authApi.resetPassword(resetForm)
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
      showResetDialog.value = false
      resetForm.email = ''
      resetForm.code = ''
      resetForm.newPassword = ''
    }
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.login-bg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(64, 158, 255, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(64, 158, 255, 0.06) 0%, transparent 50%),
    #f5f5f5;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px 36px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);

  .login-header {
    text-align: center;
    margin-bottom: 36px;

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: #999;
    }
  }

  .login-form {
    .code-input {
      display: flex;
      gap: 12px;
      width: 100%;

      .el-input {
        flex: 1;
      }
    }

    .login-btn {
      width: 100%;
      font-size: 15px;
      letter-spacing: 2px;
    }
  }

  .login-footer {
    text-align: center;
    margin-top: 8px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}

:deep(.el-dialog) {
  border-radius: 12px;

  .el-dialog__header {
    padding: 20px 24px 16px;
    border-bottom: 1px solid #f0f0f0;
    margin-right: 0;

    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px 20px;
    border-top: 1px solid #f0f0f0;
  }

  .code-input {
    display: flex;
    gap: 12px;
    width: 100%;

    .el-input {
      flex: 1;
    }
  }
}

@media (max-width: 768px) {
  .login-card {
    padding: 32px 20px 24px;

    .login-header {
      margin-bottom: 24px;

      h2 {
        font-size: 20px;
      }
    }
  }
}
</style>
