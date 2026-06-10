<template>
  <div class="tag-page">
    <div class="card">
      <div class="card-title">
        标签: {{ tag?.name }}
        <el-link
          v-if="tag?.link"
          :href="formatLink(tag.link)"
          target="_blank"
          type="primary"
          :underline="false"
          class="tag-link"
        >
          <el-icon><Link /></el-icon>
          访问链接
        </el-link>
      </div>
      <div class="article-list">
        <div 
          class="article-item" 
          v-for="article in articles" 
          :key="article.id"
          @click="$router.push(`/article/${article.id}`)"
        >
          <h3 class="title">{{ article.title }}</h3>
          <p class="summary">{{ article.summary }}</p>
          <div class="meta">
            <span><el-icon><User /></el-icon> {{ article.authorName }}</span>
            <span><el-icon><Calendar /></el-icon> {{ formatDate(article.createTime) }}</span>
          </div>
        </div>
      </div>
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadArticles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '@/api/article'
import { tagApi } from '@/api/tag'

const route = useRoute()
const articles = ref([])
const tag = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadArticles = async () => {
  const res = await articleApi.getList({ 
    tagId: route.params.id, 
    pageNum: pageNum.value, 
    pageSize: pageSize.value 
  })
  if (res.code === 200) {
    articles.value = res.data.list
    total.value = res.data.total
  }
}

const loadTag = async () => {
  const res = await tagApi.getAll()
  if (res.code === 200) {
    tag.value = res.data.find(t => t.id === Number(route.params.id))
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatLink = (link) => {
  if (!link) return ''
  if (!/^https?:\/\//i.test(link)) {
    return 'https://' + link
  }
  return link
}

watch(() => route.params.id, () => {
  pageNum.value = 1
  loadArticles()
  loadTag()
})

onMounted(() => {
  loadArticles()
  loadTag()
})
</script>

<style scoped lang="scss">
.tag-page {
  .card {
    border-radius: 12px;
    padding: 24px;
  }

  .card-title {
    font-size: 20px;
    font-weight: 600;
    color: #1a1a2e;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 12px;

    .tag-link {
      font-size: 14px;
      font-weight: 400;
    }
  }

  .article-list {
    .article-item {
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f5f7fa;
        border-radius: 8px;
        padding-left: 12px;
        padding-right: 12px;
        margin: 0 -12px;
      }

      &:last-child {
        border-bottom: none;
      }

      .title {
        font-size: 16px;
        font-weight: 500;
        margin: 0 0 8px;
        color: #303133;
      }

      .summary {
        font-size: 14px;
        color: #606266;
        margin: 0 0 8px;
        line-height: 1.6;
      }

      .meta {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: #909399;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: center;
  }
}
</style>
