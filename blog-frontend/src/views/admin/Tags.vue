<template>
  <div class="tags-page">
    <div class="page-header">
      <h3>标签管理</h3>
      <div class="header-actions">
        <el-popconfirm
          :title="`确定删除选中的 ${selectedRows.length} 个标签吗？`"
          @confirm="batchDeleteTags"
        >
          <template #reference>
            <el-button type="danger" :disabled="selectedRows.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </template>
        </el-popconfirm>
        <el-button type="primary" @click="showDialog()">
          <el-icon><Plus /></el-icon>
          新建标签
        </el-button>
      </div>
    </div>

    <div class="card">
      <el-table
        ref="tableRef"
        :data="tags"
        v-loading="loading"
        row-key="id"
        @selection-change="onSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="150" />
        <el-table-column label="链接" min-width="200">
          <template #default="{ row }">
            <el-link
              v-if="row.link"
              type="primary"
              :href="formatLink(row.link)"
              target="_blank"
              :underline="false"
            >
              <el-icon><Link /></el-icon>
              {{ truncateLink(row.link) }}
            </el-link>
            <span v-else class="no-link">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button text type="primary" @click="showDialog(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-popconfirm title="确定删除此标签吗？" @confirm="deleteTag(row.id)">
                <template #reference>
                  <el-button text type="danger">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="标签名称" />
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="form.link" placeholder="输入链接地址，如 github.com/username">
            <template #prefix>
              <el-icon><Link /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">可选，不填则不显示链接</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTag" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { tagApi } from '@/api/tag'

const tags = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const tableRef = ref()
const selectedRows = ref([])

const form = reactive({
  id: null,
  name: '',
  link: ''
})

const loadTags = async () => {
  loading.value = true
  try {
    const res = await tagApi.getAll()
    if (res.code === 200) {
      tags.value = res.data
      selectedRows.value = []
      await nextTick()
      tableRef.value?.clearSelection()
    }
  } finally {
    loading.value = false
  }
}

const onSelectionChange = (rows) => {
  selectedRows.value = rows
}

const showDialog = (tag = null) => {
  isEdit.value = !!tag
  if (tag) {
    form.id = tag.id
    form.name = tag.name
    form.link = tag.link || ''
  } else {
    form.id = null
    form.name = ''
    form.link = ''
  }
  dialogVisible.value = true
}

const saveTag = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }

  saving.value = true
  try {
    let res
    if (isEdit.value) {
      res = await tagApi.update(form.id, form.name, form.link || null)
    } else {
      res = await tagApi.create(form.name, form.link || null)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadTags()
    }
  } finally {
    saving.value = false
  }
}

const deleteTag = async (id) => {
  const res = await tagApi.delete(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadTags()
  }
}

const batchDeleteTags = async () => {
  const ids = selectedRows.value.map((row) => row.id)
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的标签')
    return
  }

  const results = await Promise.allSettled(ids.map((id) => tagApi.delete(id)))
  const successCount = results.filter((r) => r.status === 'fulfilled' && r.value?.code === 200).length
  const failCount = ids.length - successCount

  if (successCount > 0) ElMessage.success(`删除成功 ${successCount} 条`)
  if (failCount > 0) ElMessage.warning(`删除失败 ${failCount} 条`)
  loadTags()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const formatLink = (link) => {
  if (!link) return ''
  // 如果链接没有协议前缀，自动添加 https://
  if (!/^https?:\/\//i.test(link)) {
    return 'https://' + link
  }
  return link
}

const truncateLink = (link) => {
  if (!link) return ''
  // 移除协议前缀用于显示
  const display = link.replace(/^https?:\/\//i, '')
  // 如果太长则截断
  if (display.length > 30) {
    return display.substring(0, 30) + '...'
  }
  return display
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped lang="scss">
.tags-page {
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

  .header-actions {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .card {
    border-radius: 12px;
    padding: 20px;

    .action-btns {
      display: flex;
      align-items: center;
    }

    .no-link {
      color: #c0c4cc;
    }
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>
