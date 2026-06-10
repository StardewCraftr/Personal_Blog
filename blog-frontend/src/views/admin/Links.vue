<template>
  <div class="links-page">
    <div class="page-header">
      <h3>友情链接</h3>
      <div class="header-actions">
        <el-popconfirm
          :title="`确定删除选中的 ${selectedRows.length} 个链接吗？`"
          @confirm="batchDeleteLinks"
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
          新建链接
        </el-button>
      </div>
    </div>

    <div class="card">
      <el-table
        ref="tableRef"
        :data="links"
        v-loading="loading"
        row-key="id"
        @selection-change="onSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="Logo" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.logo"
              :src="row.logo"
              style="width: 40px; height: 40px; border-radius: 8px;"
              fit="cover"
            />
            <div v-else class="logo-placeholder">
              <el-icon size="18"><Link /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="网站名称" min-width="120" />
        <el-table-column prop="url" label="链接" min-width="180" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button text type="primary" @click="showDialog(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-popconfirm title="确定删除此链接吗？" @confirm="deleteLink(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑链接' : '新建链接'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="网站名称">
          <el-input v-model="form.name" placeholder="网站名称" />
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="form.url" placeholder="https://example.com" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-input v-model="form.logo" placeholder="Logo图片地址" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="网站描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"
            active-text="显示" inactive-text="隐藏" inline-prompt />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveLink" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { linkApi } from '@/api/link'

const links = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const tableRef = ref()
const selectedRows = ref([])

const form = reactive({
  id: null,
  name: '',
  url: '',
  logo: '',
  description: '',
  sort: 0,
  status: 1
})

const loadLinks = async () => {
  loading.value = true
  try {
    const res = await linkApi.getAll()
    if (res.code === 200) {
      links.value = res.data
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

const showDialog = (link = null) => {
  isEdit.value = !!link
  if (link) {
    form.id = link.id
    form.name = link.name
    form.url = link.url
    form.logo = link.logo
    form.description = link.description
    form.sort = link.sort
    form.status = link.status
  } else {
    form.id = null
    form.name = ''
    form.url = ''
    form.logo = ''
    form.description = ''
    form.sort = 0
    form.status = 1
  }
  dialogVisible.value = true
}

const saveLink = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入网站名称')
    return
  }
  if (!form.url.trim()) {
    ElMessage.warning('请输入链接地址')
    return
  }

  saving.value = true
  try {
    let res
    if (isEdit.value) {
      res = await linkApi.update(form.id, form.name, form.url, form.logo, form.description, form.sort, form.status)
    } else {
      res = await linkApi.create(form.name, form.url, form.logo, form.description, form.sort)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadLinks()
    }
  } finally {
    saving.value = false
  }
}

const deleteLink = async (id) => {
  const res = await linkApi.delete(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadLinks()
  }
}

const batchDeleteLinks = async () => {
  const ids = selectedRows.value.map((row) => row.id)
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的链接')
    return
  }

  const results = await Promise.allSettled(ids.map((id) => linkApi.delete(id)))
  const successCount = results.filter((r) => r.status === 'fulfilled' && r.value?.code === 200).length
  const failCount = ids.length - successCount

  if (successCount > 0) ElMessage.success(`删除成功 ${successCount} 条`)
  if (failCount > 0) ElMessage.warning(`删除失败 ${failCount} 条`)
  loadLinks()
}

onMounted(() => {
  loadLinks()
})
</script>

<style scoped lang="scss">
.links-page {
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
  }

  .logo-placeholder {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c0c4cc;
  }
}
</style>
