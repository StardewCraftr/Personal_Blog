<template>
  <div class="category-page">
    <div class="card">
      <div class="card-title">分类: {{ category?.name }}</div>
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
import { categoryApi } from '@/api/category'

const route = useRoute()
const articles = ref([])
const category = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadArticles = async () => {
  const res = await articleApi.getList({ 
    categoryId: route.params.id, 
    pageNum: pageNum.value, 
    pageSize: pageSize.value 
  })
  if (res.code === 200) {
    articles.value = res.data.list
    total.value = res.data.total
  }
}

const loadCategory = async () => {
  const res = await categoryApi.getAll()
  if (res.code === 200) {
    category.value = res.data.find(c => c.id === Number(route.params.id))
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

watch(() => route.params.id, () => {
  pageNum.value = 1
  loadArticles()
  loadCategory()
})

onMounted(() => {
  loadArticles()
  loadCategory()
})
</script>
