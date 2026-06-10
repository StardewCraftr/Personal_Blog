import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/ArticleDetail.vue')
      },
      {
        path: 'category/:id',
        name: 'Category',
        component: () => import('@/views/CategoryArticles.vue')
      },
      {
        path: 'tag/:id',
        name: 'Tag',
        component: () => import('@/views/TagArticles.vue')
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'articles',
        name: 'ManageArticles',
        component: () => import('@/views/admin/Articles.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'articles/create',
        name: 'CreateArticle',
        component: () => import('@/views/admin/ArticleEdit.vue'),
        meta: { title: '新建文章' }
      },
      {
        path: 'articles/edit/:id',
        name: 'EditArticle',
        component: () => import('@/views/admin/ArticleEdit.vue'),
        meta: { title: '编辑文章' }
      },
      {
        path: 'drafts',
        name: 'ManageDrafts',
        component: () => import('@/views/admin/Drafts.vue'),
        meta: { title: '草稿管理' }
      },
      {
        path: 'categories',
        name: 'ManageCategories',
        component: () => import('@/views/admin/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'tags',
        name: 'ManageTags',
        component: () => import('@/views/admin/Tags.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: 'links',
        name: 'ManageLinks',
        component: () => import('@/views/admin/Links.vue'),
        meta: { title: '友情链接' }
      },
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('@/views/admin/Upload.vue'),
        meta: { title: '文件上传' }
      },
      {
        path: 'clipboard',
        name: 'Clipboard',
        component: () => import('@/views/admin/Clipboard.vue'),
        meta: { title: '粘贴板' }
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('@/views/admin/Devices.vue'),
        meta: { title: '登录设备' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
