import { createPinia } from 'pinia'
import { createApp } from 'vue'
import { ElAlert, ElButton, ElForm, ElFormItem, ElInput } from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router, { installAuthGuards } from './router'
import { installApiInterceptors } from './services/installApiInterceptors'
import './styles/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
installAuthGuards(pinia)
installApiInterceptors(pinia, router)
app.use(router)
app.component('ElAlert', ElAlert)
app.component('ElButton', ElButton)
app.component('ElForm', ElForm)
app.component('ElFormItem', ElFormItem)
app.component('ElInput', ElInput)
app.mount('#app')
