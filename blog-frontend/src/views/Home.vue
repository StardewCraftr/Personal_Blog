<template>
  <div class="home-page">
    <div class="main-content">
      <div class="content-area">
        <div class="card">
          <div class="card-title">最新文章</div>
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
                <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
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
      
      <div class="sidebar">
        <CalendarCard />
        
        <div class="card">
          <div class="card-title">分类</div>
          <div class="category-list">
            <div 
              class="category-item" 
              v-for="category in categories" 
              :key="category.id"
              @click="$router.push(`/category/${category.id}`)"
            >
              {{ category.name }}
            </div>
          </div>
        </div>
        
        <div class="card">
          <div class="card-title">标签</div>
          <div class="tag-list">
            <template v-for="tag in tags" :key="tag.id">
              <a
                v-if="tag.link"
                :href="formatLink(tag.link)"
                target="_blank"
                class="tag-item tag-link"
              >
                <el-icon><Link /></el-icon>
                {{ tag.name }}
              </a>
              <span
                v-else
                class="tag-item"
                @click="$router.push(`/tag/${tag.id}`)"
              >
                {{ tag.name }}
              </span>
            </template>
          </div>
        </div>
        
        <div class="card" v-if="links.length">
          <div class="card-title">友情链接</div>
          <div class="link-list">
            <a 
              v-for="link in links" 
              :key="link.id" 
              :href="link.url" 
              target="_blank"
              class="link-item"
            >
              <img :src="link.logo" :alt="link.name" class="link-logo" v-if="link.logo">
              <div class="link-info">
                <div class="link-name">{{ link.name }}</div>
                <div class="link-desc" v-if="link.description">{{ link.description }}</div>
              </div>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'
import { linkApi } from '@/api/link'
import CalendarCard from '@/components/CalendarCard.vue'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const links = ref([])
const pageNum = ref(1)
const pageSize = ref(7)
const total = ref(0)

const loadArticles = async () => {
  const res = await articleApi.getList({ pageNum: pageNum.value, pageSize: pageSize.value })
  if (res.code === 200) {
    articles.value = res.data.list
    total.value = res.data.total
  }
}

const loadCategories = async () => {
  const res = await categoryApi.getAll()
  if (res.code === 200) {
    categories.value = res.data
  }
}

const loadTags = async () => {
  const res = await tagApi.getAll()
  if (res.code === 200) {
    tags.value = res.data
  }
}

const loadLinks = async () => {
  const res = await linkApi.getAll()
  if (res.code === 200) {
    links.value = res.data
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

onMounted(() => {
  loadArticles()
  loadCategories()
  loadTags()
  loadLinks()
})
</script>

<style scoped lang="scss">
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .tag-item {
    padding: 4px 12px;
    background: #f0f2f5;
    border-radius: 4px;
    font-size: 13px;
    color: #606266;
    cursor: pointer;
    transition: all 0.2s;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 4px;

    &:hover {
      background: #e8f4fd;
      color: #409eff;
    }

    &.tag-link {
      color: #409eff;
      background: #e8f4fd;
    }
  }
}

.link-list {
  .link-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
    color: inherit;
    text-decoration: none;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      .link-name {
        color: #409eff;
      }
    }
    
    .link-logo {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      object-fit: cover;
    }
    
    .link-info {
      flex: 1;
      min-width: 0;
      
      .link-name {
        font-weight: 500;
        font-size: 14px;
        margin-bottom: 4px;
        transition: color 0.2s;
      }
      
      .link-desc {
        font-size: 12px;
        color: #999;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
