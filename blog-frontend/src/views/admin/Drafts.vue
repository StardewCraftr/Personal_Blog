<template>
  <div class="drafts-page">
    <div class="page-header">
      <h3>草稿管理</h3>
      <div class="header-actions">
        <el-popconfirm
          :title="`确定删除选中的 ${selectedRows.length} 条草稿吗？`"
          @confirm="batchDeleteDrafts"
        >
          <template #reference>
            <el-button type="danger" :disabled="selectedRows.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <div class="card">
      <el-table
        ref="tableRef"
        :data="drafts"
        v-loading="loading"
        row-key="id"
        @selection-change="onSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button text type="primary" @click="editDraft(row.id)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button text type="success" @click="publishDraft(row)">
                <el-icon><Promotion /></el-icon>
                发布
              </el-button>
              <el-popconfirm title="确定删除此草稿吗？" @confirm="deleteDraft(row.id)">
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

      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        :total-text="`共 ${total} 条`"
        @current-change="loadDrafts"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { articleApi } from '@/api/article'

const router = useRouter()
const drafts = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])

const loadDrafts = async () => {
  loading.value = true
  try {
    const res = await articleApi.getDrafts({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      drafts.value = res.data.list
      total.value = res.data.total
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

const editDraft = (id) => {
  router.push(`/admin/articles/edit/${id}`)
}

const publishDraft = async (row) => {
  try {
    const res = await articleApi.update({
      id: row.id,
      title: row.title,
      summary: row.summary,
      content: row.content,
      categoryId: row.categoryId,
      tagIds: row.tags?.map(t => t.id) || [],
      status: 1,
      isPrivate: row.isPrivate || 0
    })
    if (res.code === 200) {
      ElMessage.success('发布成功')
      loadDrafts()
    }
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const deleteDraft = async (id) => {
  const res = await articleApi.delete(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadDrafts()
  }
}

const batchDeleteDrafts = async () => {
  const ids = selectedRows.value.map((row) => row.id)
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的草稿')
    return
  }

  const results = await Promise.allSettled(ids.map((id) => articleApi.delete(id)))
  const successCount = results.filter((r) => r.status === 'fulfilled' && r.value?.code === 200).length
  const failCount = ids.length - successCount

  if (successCount > 0) ElMessage.success(`删除成功 ${successCount} 条`)
  if (failCount > 0) ElMessage.warning(`删除失败 ${failCount} 条`)
  loadDrafts()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadDrafts()
})
</script>

<style scoped lang="scss">
.drafts-page {
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

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
    display: flex;
  }
}
</style>
