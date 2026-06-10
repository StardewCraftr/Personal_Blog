<template>
  <div class="clipboard-page">
    <div class="page-header">
      <h3>粘贴板</h3>
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon>
        新建条目
      </el-button>
    </div>

    <div class="clipboard-list">
      <div
        class="clipboard-item"
        v-for="item in clipboards"
        :key="item.id"
      >
        <div class="item-header">
          <span class="item-title">{{ item.title || '未命名' }}</span>
          <span class="item-time">{{ formatDate(item.updateTime) }}</span>
        </div>
        <div class="item-content">{{ item.content }}</div>
        <div class="item-actions">
          <el-button text type="primary" size="small" @click="copyContent(item.content)">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
          <el-button text type="primary" size="small" @click="showDialog(item)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-popconfirm title="确定删除此条目吗？" @confirm="deleteItem(item.id)">
            <template #reference>
              <el-button text type="danger" size="small">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
      <el-empty v-if="!loading && clipboards.length === 0" description="暂无粘贴板条目" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑条目' : '新建条目'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="可选，方便识别" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="粘贴内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { clipboardApi } from '@/api/clipboard'

const clipboards = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  title: '',
  content: ''
})

const loadClipboards = async () => {
  loading.value = true
  try {
    const res = await clipboardApi.getAll()
    if (res.code === 200) {
      clipboards.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const showDialog = (item = null) => {
  isEdit.value = !!item
  if (item) {
    form.id = item.id
    form.title = item.title || ''
    form.content = item.content || ''
  } else {
    form.id = null
    form.title = ''
    form.content = ''
  }
  dialogVisible.value = true
}

const saveItem = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  saving.value = true
  try {
    let res
    if (isEdit.value) {
      res = await clipboardApi.update(form.id, form.title, form.content)
    } else {
      res = await clipboardApi.create(form.title, form.content)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadClipboards()
    }
  } finally {
    saving.value = false
  }
}

const deleteItem = async (id) => {
  const res = await clipboardApi.delete(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadClipboards()
  }
}

const copyContent = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadClipboards()
})
</script>

<style scoped lang="scss">
.clipboard-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #1a1a2e;
    }
  }

  .clipboard-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .clipboard-item {
    background: #fff;
    border-radius: 12px;
    padding: 20px 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: box-shadow 0.2s;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .item-title {
        font-size: 15px;
        font-weight: 600;
        color: #1a1a2e;
      }

      .item-time {
        font-size: 12px;
        color: #c0c4cc;
      }
    }

    .item-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 120px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 5;
      -webkit-box-orient: vertical;
      margin-bottom: 12px;
      padding: 12px 16px;
      background: #f8f9fa;
      border-radius: 8px;
    }

    .item-actions {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}
</style>
