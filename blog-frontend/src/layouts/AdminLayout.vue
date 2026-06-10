<template>
  <div class="admin-layout">
    <!-- 移动端遮罩 -->
    <div class="sidebar-mask" v-if="sidebarOpen" @click="sidebarOpen = false"></div>

    <el-container>
      <el-aside :width="asideWidth" :class="{ open: sidebarOpen }">
        <div class="logo">
          <el-icon size="22"><Monitor /></el-icon>
          <span>博客管理</span>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="transparent"
          text-color="rgba(255,255,255,0.85)"
          active-text-color="#fff"
          @select="sidebarOpen = false"
        >
          <el-menu-item index="/admin">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/devices">
            <el-icon><Platform /></el-icon>
            <span>登录设备</span>
          </el-menu-item>
          <el-menu-item index="/admin/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/admin/articles">
            <el-icon><Document /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/drafts">
            <el-icon><Files /></el-icon>
            <span>草稿管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Folder /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/tags">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/links">
            <el-icon><Link /></el-icon>
            <span>友情链接</span>
          </el-menu-item>
          <el-menu-item index="/admin/clipboard">
            <el-icon><CopyDocument /></el-icon>
            <span>粘贴板</span>
          </el-menu-item>
          <el-menu-item index="/admin/upload">
            <el-icon><UploadFilled /></el-icon>
            <span>文件上传</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <div class="header-left">
              <el-icon class="sidebar-toggle" @click="sidebarOpen = !sidebarOpen">
                <Fold v-if="sidebarOpen" />
                <Expand v-else />
              </el-icon>
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item v-if="$route.meta.title && $route.path !== '/admin'">
                  {{ $route.meta.title }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="header-right">
              <el-button text @click="$router.push('/')">
                <el-icon><House /></el-icon>
                <span class="btn-text">访问前台</span>
              </el-button>
              <el-dropdown>
                <span class="user-info">
                  <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                    {{ userStore.userInfo?.nickname?.charAt(0) }}
                  </el-avatar>
                  <span class="user-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="$router.push('/admin/profile')">个人信息</el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const sidebarOpen = ref(false)
const isMobile = ref(window.innerWidth <= 768)

const asideWidth = computed(() => isMobile.value ? '0px' : '220px')

// 路由切换时关闭侧边栏
watch(() => route.path, () => {
  sidebarOpen.value = false
})

window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) sidebarOpen.value = false
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  overflow: hidden;

  .el-container {
    height: 100%;
  }

  .sidebar-mask {
    display: none;
  }

  .el-aside {
    background: linear-gradient(180deg, #1a1f36 0%, #252b48 100%);
    overflow-y: auto;
    transition: transform 0.25s;
    z-index: 200;

    &::-webkit-scrollbar {
      width: 0;
    }

    .logo {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      color: #fff;
      font-size: 17px;
      font-weight: 700;
      letter-spacing: 1px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }

    .el-menu {
      border-right: none;
      padding: 8px 0;

      :deep(.el-menu-item) {
        height: 46px;
        line-height: 46px;
        margin: 2px 10px;
        border-radius: 8px;
        transition: all 0.2s;

        &:hover {
          background: rgba(255, 255, 255, 0.06) !important;
        }

        &.is-active {
          background: rgba(64, 158, 255, 0.2) !important;
          color: #fff !important;
          font-weight: 500;
        }
      }
    }
  }

  .el-header {
    background: #fff;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
    padding: 0 24px;
    z-index: 1;

    .header-content {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .sidebar-toggle {
      display: none;
      font-size: 20px;
      cursor: pointer;
      color: #333;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .user-name {
        font-size: 14px;
        color: #333;
      }
    }
  }

  .el-main {
    background: #f0f2f5;
    padding: 24px;
    overflow-y: auto;
  }
}

.el-tooltip__trigger {
  outline: none !important;
}

@media (max-width: 768px) {
  .admin-layout {
    .sidebar-mask {
      display: block;
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.4);
      z-index: 199;
    }

    .el-aside {
      position: fixed;
      top: 0;
      left: 0;
      bottom: 0;
      width: 220px !important;
      transform: translateX(-100%);

      &.open {
        transform: translateX(0);
      }
    }

    .el-header {
      padding: 0 12px;

      .sidebar-toggle {
        display: block;
      }

      .user-name {
        display: none;
      }

      .btn-text {
        display: none;
      }
    }

    .el-main {
      padding: 12px;
    }
  }
}
</style>
