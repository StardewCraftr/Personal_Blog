<template>
  <div class="categories-page">
    <div class="page-header">
      <h3>分类管理</h3>
      <div class="header-actions">
        <el-popconfirm
          :title="`确定删除选中的 ${selectedRows.length} 个分类吗？`"
          @confirm="batchDeleteCategories"
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
          新建分类
        </el-button>
      </div>
    </div>

    <div class="card">
      <el-table
        ref="tableRef"
        :data="categories"
        v-loading="loading"
        row-key="id"
        @selection-change="onSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="sort" label="排序" width="100" align="center" />
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
              <el-popconfirm title="确定删除此分类吗？" @confirm="deleteCategory(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新建分类'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { categoryApi } from '@/api/category'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const tableRef = ref()
const selectedRows = ref([])

const form = reactive({
  id: null,
  name: '',
  sort: 0
})

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.getAll()
    if (res.code === 200) {
      categories.value = res.data
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

const showDialog = (category = null) => {
  isEdit.value = !!category
  if (category) {
    form.id = category.id
    form.name = category.name
    form.sort = category.sort
  } else {
    form.id = null
    form.name = ''
    form.sort = 0
  }
  dialogVisible.value = true
}

const saveCategory = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  saving.value = true
  try {
    let res
    if (isEdit.value) {
      res = await categoryApi.update(form.id, form.name, null, form.sort)
    } else {
      res = await categoryApi.create(form.name, null, form.sort)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadCategories()
    }
  } finally {
    saving.value = false
  }
}

const deleteCategory = async (id) => {
  const res = await categoryApi.delete(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadCategories()
  }
}

const batchDeleteCategories = async () => {
  const ids = selectedRows.value.map((row) => row.id)
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的分类')
    return
  }

  const results = await Promise.allSettled(ids.map((id) => categoryApi.delete(id)))
  const successCount = results.filter((r) => r.status === 'fulfilled' && r.value?.code === 200).length
  const failCount = ids.length - successCount

  if (successCount > 0) ElMessage.success(`删除成功 ${successCount} 条`)
  if (failCount > 0) ElMessage.warning(`删除失败 ${failCount} 条`)
  loadCategories()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.categories-page {
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
}
</style>
