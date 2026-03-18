import { createApp } from 'vue'
import ElementPlus from 'element-plus'

import App from './App.vue'
import router from './router'
import pinia from './stores'
import 'element-plus/dist/index.css'
import './styles/reset.css'
import './styles/index.css'

createApp(App).use(pinia).use(router).use(ElementPlus).mount('#app')
