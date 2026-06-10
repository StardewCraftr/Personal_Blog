<template>
  <div class="profile-page">
    <div class="page-header">
      <h3>个人信息</h3>
    </div>

    <div class="profile-card card">
      <div class="avatar-section">
        <el-avatar :size="90" :src="form.avatar">
          {{ form.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="avatar-info">
          <el-upload
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="uploadAvatar"
            accept="image/*"
          >
            <el-button type="primary" plain size="small">
              <el-icon><Upload /></el-icon>
              更换头像
            </el-button>
          </el-upload>
          <p>支持 JPG、PNG 格式，大小不超过 2MB</p>
        </div>
      </div>

      <el-divider />

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="profile-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="作者简介" prop="author">
          <el-input
            v-model="form.author"
            type="textarea"
            :rows="4"
            placeholder="请输入作者简介，展示在文章详情页"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">
            <el-icon><Check /></el-icon>
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)

const form = reactive({
  username: '',
  email: '',
  nickname: '',
  avatar: '',
  author: ''
})

const rules = {
  nickname: [
    { max: 50, message: '昵称不能超过50个字符', trigger: 'blur' }
  ],
  author: [
    { max: 500, message: '作者简介不能超过500个字符', trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  const res = await userApi.getProfile()
  if (res.code === 200) {
    form.username = res.data.username
    form.email = res.data.email
    form.nickname = res.data.nickname
    form.avatar = res.data.avatar
    form.author = res.data.author || ''
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const uploadAvatar = async (options) => {
  try {
    const res = await userApi.uploadAvatar(options.file)
    if (res.code === 200) {
      form.avatar = res.data
      userStore.setUserInfo({
        ...userStore.userInfo,
        avatar: res.data
      })
      ElMessage.success('头像上传成功')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const saveProfile = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const res = await userApi.updateProfile({
      nickname: form.nickname,
      avatar: form.avatar,
      author: form.author
    })
    if (res.code === 200) {
      userStore.setUserInfo({
        ...userStore.userInfo,
        nickname: form.nickname,
        avatar: form.avatar,
        author: form.author
      })
      ElMessage.success('保存成功')
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.profile-page {
  .page-header {
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #1a1a2e;
    }
  }

  .profile-card {
    max-width: 640px;
    border-radius: 12px;
    padding: 32px;
  }

  .avatar-section {
    display: flex;
    align-items: center;
    gap: 24px;

    .el-avatar {
      flex-shrink: 0;
    }

    .avatar-info {
      p {
        margin: 8px 0 0;
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .profile-form {
    margin-top: 8px;

    :deep(.el-form-item) {
      margin-bottom: 22px;
    }

    :deep(.el-input.is-disabled .el-input__inner) {
      color: #606266;
      -webkit-text-fill-color: #606266;
    }
  }
}
</style>
