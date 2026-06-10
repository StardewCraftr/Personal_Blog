<template>
  <div class="main-layout">
    <header class="header">
      <div class="container header-content">
        <div class="header-left">
          <div class="logo" @click="$router.push('/')">
            <el-icon size="24"><Notebook /></el-icon>
            <span>Personal Blog</span>
          </div>

          <nav class="nav" :class="{ open: menuOpen }">
            <router-link to="/" class="nav-item" @click="menuOpen = false">首页</router-link>
            <router-link to="/admin" class="nav-item" v-if="userStore.isLoggedIn" @click="menuOpen = false">管理</router-link>
            <div class="search-box mobile-search">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索文章..."
                @keyup.enter="handleSearch"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </nav>

          <div class="search-box pc-search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文章..."
              @keyup.enter="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>

        <div class="header-right">
          <div class="user-area">
            <template v-if="userStore.isLoggedIn">
              <el-dropdown>
                <span class="user-info">
                  <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                    {{ userStore.userInfo?.nickname?.charAt(0) }}
                  </el-avatar>
                  <span class="username">{{ userStore.userInfo?.nickname }}</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="$router.push('/admin')">后台管理</el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            </template>
          </div>
          <el-icon class="menu-toggle" @click="menuOpen = !menuOpen">
            <Fold v-if="menuOpen" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </header>
    
    <main class="main">
      <div class="container">
        <router-view />
      </div>
    </main>
    
    <footer class="footer">
      <div class="container">
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener" class="icp">皖ICP备2021016767号</a>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const menuOpen = ref(false)

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { keyword: searchKeyword.value } })
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped lang="scss">
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 30px;
    flex: 1;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #409eff;
    cursor: pointer;
    white-space: nowrap;
  }

  .nav {
    display: flex;
    gap: 20px;

    .nav-item {
      padding: 8px 16px;
      color: #333;
      font-weight: 500;
      border-radius: 4px;

      &:hover, &.router-link-active {
        color: #409eff;
        background: #ecf5ff;
      }
    }
  }

  .search-box {
    flex: 1;
    max-width: 300px;
  }

  .mobile-search {
    display: none;
  }

  .menu-toggle {
    display: none;
    font-size: 22px;
    cursor: pointer;
    color: #333;
  }

  .user-area {
    display: flex;
    align-items: center;
    gap: 10px;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

.main {
  flex: 1;
  padding: 20px 0;
}

.footer {
  background: #fff;
  padding: 20px 0;
  text-align: center;
  color: #999;
  font-size: 13px;

  p {
    margin: 0 0 4px;
  }

  .icp {
    color: #999;
    text-decoration: none;
    transition: color 0.2s;

    &:hover {
      color: #409eff;
    }
  }
}

.el-tooltip__trigger {
  outline: none !important;
}

@media (max-width: 768px) {
  .header {
    .header-left {
      gap: 0;
    }

    .pc-search {
      display: none;
    }

    .mobile-search {
      display: block;
    }

    .nav {
      display: none;
      flex-direction: column;
      position: absolute;
      top: 60px;
      left: 0;
      right: 0;
      background: #fff;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      padding: 12px 16px;
      gap: 4px;
      z-index: 99;

      &.open {
        display: flex;
      }

      .nav-item {
        padding: 10px 12px;
      }
    }

    .menu-toggle {
      display: block;
    }

    .user-area .username {
      display: none;
    }
  }

  .main {
    padding: 12px 0;
  }
}
</style>
