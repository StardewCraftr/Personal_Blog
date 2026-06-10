<template>
  <div class="article-detail-page">
    <div class="main-content">
      <div class="content-area">
        <div class="card article-card" v-if="article">
          <div class="article-header">
            <h1 class="article-title">{{ article.title }}</h1>
            <el-button 
              v-if="canEdit" 
              type="primary" 
              size="small"
              @click="$router.push(`/admin/articles/edit/${article.id}`)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
          </div>
          <div class="article-meta">
            <span><el-icon><User /></el-icon> {{ article.authorName }}</span>
            <span><el-icon><Calendar /></el-icon> {{ formatDate(article.createTime) }}</span>
            <span><el-icon><View /></el-icon> {{ article.viewCount }} 阅读</span>
          </div>
          <div class="article-tags" v-if="article.tags?.length">
            <template v-for="tag in article.tags" :key="tag.id">
              <el-tag
                v-if="tag.link"
                size="small"
                @click="openTagLink(tag.link)"
                class="tag-with-link"
              >
                <el-icon><Link /></el-icon>
                {{ tag.name }}
              </el-tag>
              <el-tag
                v-else
                size="small"
                @click="$router.push(`/tag/${tag.id}`)"
              >
                {{ tag.name }}
              </el-tag>
            </template>
          </div>
          <div class="article-content rich-text" v-html="sanitizedContent"></div>
        </div>
      </div>
      
      <div class="sidebar">
        <div class="card">
          <div class="card-title">作者</div>
          <div class="author-info">
            <el-avatar :size="50" :src="article?.authorAvatar">
              {{ article?.authorName?.charAt(0) }}
            </el-avatar>
            <div class="author-name">{{ article?.authorName }}</div>
            <div class="author-intro" v-if="article?.authorIntro">{{ article?.authorIntro }}</div>
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
                <div class="link-desc">{{ link.description }}</div>
              </div>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '@/api/article'
import { linkApi } from '@/api/link'
import { useUserStore } from '@/stores/user'
import DOMPurify from 'dompurify'

const route = useRoute()
const userStore = useUserStore()
const article = ref(null)
const links = ref([])

const canEdit = computed(() => {
  if (!userStore.isLoggedIn || !article.value) return false
  return userStore.userInfo?.id === article.value.userId || userStore.isAdmin
})

// XSS 防护：对文章内容进行 DOMPurify 清理
const sanitizedContent = computed(() => {
  if (!article.value?.content) return ''
  return DOMPurify.sanitize(article.value.content, {
    ADD_TAGS: ['iframe'],
    ADD_ATTR: ['target', 'allow', 'allowfullscreen', 'frameborder', 'scrolling']
  })
})

const loadArticle = async () => {
  const res = await articleApi.getDetail(route.params.id)
  if (res.code === 200) {
    article.value = res.data
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
  return new Date(dateStr).toLocaleString('zh-CN')
}

const formatLink = (link) => {
  if (!link) return ''
  if (!/^https?:\/\//i.test(link)) {
    return 'https://' + link
  }
  return link
}

const openTagLink = (link) => {
  window.open(formatLink(link), '_blank')
}

onMounted(() => {
  loadArticle()
  loadLinks()
})
</script>

<style scoped lang="scss">
.article-card {
  .article-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 15px;
  }
  
  .article-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
  
  .article-meta {
    display: flex;
    gap: 20px;
    color: #999;
    font-size: 13px;
    margin-bottom: 15px;
    
    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
  
  .article-tags {
    margin-bottom: 20px;

    .el-tag {
      margin-right: 8px;
      cursor: pointer;

      &.tag-with-link {
        .el-icon {
          margin-right: 2px;
        }
      }
    }
  }
  
  .article-content {
    padding: 20px 0;
    border-top: 1px solid #f0f0f0;
    line-height: 1.8;
    overflow-x: auto;

    :deep(img) {
      max-width: 100%;
      border-radius: 4px;
    }

    :deep(pre) {
      background: #f6f8fa;
      padding: 15px;
      border-radius: 6px;
      overflow-x: auto;
    }

    :deep(code) {
      font-family: 'SFMono-Regular', Consolas, monospace;
      font-size: 13px;
      word-break: break-all;
    }

    :deep(blockquote) {
      border-left: 4px solid #ddd;
      padding-left: 15px;
      margin: 15px 0;
      color: #666;
    }

    :deep(table) {
      display: block;
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 15px;
      overflow-x: auto;

      th, td {
        border: 1px solid #ddd;
        padding: 8px 12px;
        white-space: nowrap;
      }

      th {
        background: #f6f8fa;
      }
    }
  }
}

.author-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  
  .author-name {
    font-weight: 500;
    font-size: 16px;
  }
  
  .author-intro {
    font-size: 13px;
    color: #666;
    text-align: center;
    line-height: 1.6;
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

@media (max-width: 768px) {
  .article-card {
    .article-title {
      font-size: 18px;
    }

    .article-meta {
      gap: 12px;
      flex-wrap: wrap;
    }

    .article-content {
      padding: 14px 0;
    }
  }
}
</style>
