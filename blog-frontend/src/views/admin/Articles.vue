<template>
  <div class="articles-page">
    <div class="page-header">
      <h3>文章管理</h3>
      <div class="header-actions">
        <el-popconfirm
          :title="`确定删除选中的 ${selectedRows.length} 篇文章吗？`"
          @confirm="batchDeleteArticles"
        >
          <template #reference>
            <el-button type="danger" :disabled="selectedRows.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </template>
        </el-popconfirm>
        <el-button type="primary" @click="$router.push('/admin/articles/create')">
          <el-icon><Plus /></el-icon>
          新建文章
        </el-button>
      </div>
    </div>

    <div class="card">
      <el-table
        ref="tableRef"
        :data="articles"
        v-loading="loading"
        row-key="id"
        @selection-change="onSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
        <el-table-column prop="likeCount" label="点赞" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button text type="primary" @click="editArticle(row.id)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-popconfirm title="确定删除此文章吗？" @confirm="deleteArticle(row.id)">
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
        @current-change="loadArticles"
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
const articles = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await articleApi.getMyArticles({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      articles.value = res.data.list
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

const editArticle = (id) => {
  router.push(`/admin/articles/edit/${id}`)
}

const deleteArticle = async (id) => {
  const res = await articleApi.delete(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    loadArticles()
  }
}

const batchDeleteArticles = async () => {
  const ids = selectedRows.value.map((row) => row.id)
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的文章')
    return
  }

  const results = await Promise.allSettled(ids.map((id) => articleApi.delete(id)))
  const successCount = results.filter((r) => r.status === 'fulfilled' && r.value?.code === 200).length
  const failCount = ids.length - successCount

  if (successCount > 0) ElMessage.success(`删除成功 ${successCount} 条`)
  if (failCount > 0) ElMessage.warning(`删除失败 ${failCount} 条`)
  loadArticles()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped lang="scss">
.articles-page {
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
