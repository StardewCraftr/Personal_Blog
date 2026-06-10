<template>
  <div class="dashboard-page">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>欢迎回来，{{ userStore.userInfo?.nickname || '管理员' }} 👋</h2>
        <p>这是你的博客管理概览</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/admin/articles/create')">
        <el-icon><EditPen /></el-icon>
        写文章
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statCards" :key="item.key">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: item.bg }">
            <el-icon size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats[item.key] }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="card">
          <div class="card-title">快捷操作</div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/admin/articles')">
              <el-icon size="20" color="#409eff"><Document /></el-icon>
              <span>管理文章</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/categories')">
              <el-icon size="20" color="#67c23a"><Folder /></el-icon>
              <span>管理分类</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/tags')">
              <el-icon size="20" color="#e6a23c"><PriceTag /></el-icon>
              <span>管理标签</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/links')">
              <el-icon size="20" color="#f56c6c"><Link /></el-icon>
              <span>管理链接</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <div class="card-title">最近文章</div>
          <div v-if="recentArticles.length" class="recent-list">
            <div
              class="recent-item"
              v-for="article in recentArticles"
              :key="article.id"
              @click="$router.push(`/admin/articles/edit/${article.id}`)"
            >
              <span class="title">{{ article.title }}</span>
              <el-tag :type="article.status === 1 ? 'success' : 'info'" size="small">
                {{ article.status === 1 ? '已发布' : '草稿' }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无文章" :image-size="60" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { tagApi } from '@/api/tag'

const userStore = useUserStore()

const statCards = [
  { key: 'articleCount', label: '文章总数', icon: 'Document', bg: 'linear-gradient(135deg, #409eff 0%, #66b1ff 100%)' },
  { key: 'categoryCount', label: '分类数量', icon: 'Folder', bg: 'linear-gradient(135deg, #67c23a 0%, #85ce61 100%)' },
  { key: 'tagCount', label: '标签数量', icon: 'PriceTag', bg: 'linear-gradient(135deg, #e6a23c 0%, #ebb563 100%)' },
  { key: 'commentCount', label: '评论数量', icon: 'ChatDotRound', bg: 'linear-gradient(135deg, #f56c6c 0%, #f78989 100%)' }
]

const stats = ref({
  articleCount: 0,
  categoryCount: 0,
  tagCount: 0,
  commentCount: 0
})

const recentArticles = ref([])

const loadStats = async () => {
  const [articleRes, categoryRes, tagRes] = await Promise.all([
    articleApi.getMyArticles({ pageNum: 1, pageSize: 5 }),
    categoryApi.getAll(),
    tagApi.getAll()
  ])

  if (articleRes.code === 200) {
    stats.value.articleCount = articleRes.data.total
    recentArticles.value = articleRes.data.list || []
  }
  if (categoryRes.code === 200) {
    stats.value.categoryCount = categoryRes.data.length
  }
  if (tagRes.code === 200) {
    stats.value.tagCount = tagRes.data.length
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  .welcome-banner {
    background: linear-gradient(135deg, #409eff 0%, #6366f1 100%);
    border-radius: 12px;
    padding: 28px 32px;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .welcome-text {
      h2 {
        color: #fff;
        font-size: 22px;
        margin: 0 0 6px;
      }
      p {
        color: rgba(255, 255, 255, 0.8);
        font-size: 14px;
        margin: 0;
      }
    }

    .el-button {
      background: rgba(255, 255, 255, 0.2);
      border-color: transparent;
      backdrop-filter: blur(10px);
      font-weight: 500;

      &:hover {
        background: rgba(255, 255, 255, 0.35);
        border-color: transparent;
      }
    }
  }

  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    .stat-icon {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .stat-info {
      .stat-value {
        font-size: 26px;
        font-weight: 700;
        color: #1a1a2e;
        line-height: 1;
      }

      .stat-label {
        font-size: 13px;
        color: #909399;
        margin-top: 6px;
      }
    }
  }

  .card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #1a1a2e;
      margin-bottom: 20px;
      padding-bottom: 14px;
      border-bottom: 1px solid #f0f0f0;
    }
  }

  .quick-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;

    .action-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 14px 16px;
      background: #f8f9fa;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s;
      font-size: 14px;
      color: #333;

      &:hover {
        background: #ecf5ff;
        color: #409eff;
      }
    }
  }

  .recent-list {
    .recent-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #f5f5f5;
      cursor: pointer;
      transition: all 0.2s;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        padding-left: 6px;

        .title {
          color: #409eff;
        }
      }

      .title {
        font-size: 14px;
        color: #333;
        transition: color 0.2s;
      }
    }
  }
}
</style>
