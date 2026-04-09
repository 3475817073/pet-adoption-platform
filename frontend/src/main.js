import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入中文语言包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import axios from 'axios'

const app = createApp(App)

// 配置 ElementPlus 使用中文
app.use(ElementPlus, { locale: zhCn })
app.config.globalProperties.$axios = axios

app.mount('#app')
