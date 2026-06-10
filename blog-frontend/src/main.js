import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/main.scss'

const app = createApp(App)

app.config.globalProperties.$baseUrl = import.meta.env.VITE_BASE_URL

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}




app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// Vue.prototype.$baseUrl = process.env.VUE_APP_BASEURL

app.mount('#app')
