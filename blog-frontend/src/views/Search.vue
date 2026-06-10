<template>
  <div class="search-page">
    <div class="card">
      <div class="card-title">搜索结果: {{ keyword }}</div>
      <div class="article-list" v-if="articles.length">
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
      <el-empty v-else description="没有找到相关文章" />
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadArticles"
        v-if="articles.length"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '@/api/article'

const route = useRoute()
const articles = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadArticles = async () => {
  keyword.value = route.query.keyword || ''
  if (!keyword.value) return
  
  const res = await articleApi.getList({ 
    keyword: keyword.value, 
    pageNum: pageNum.value, 
    pageSize: pageSize.value 
  })
  if (res.code === 200) {
    articles.value = res.data.list
    total.value = res.data.total
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

watch(() => route.query.keyword, () => {
  pageNum.value = 1
  loadArticles()
})

onMounted(() => {
  loadArticles()
})
</script>
