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
<<<<<<< HEAD
// 路由和请求拦截器依赖同一个 Pinia 会话实例。
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
installAuthGuards(pinia)
installApiInterceptors(pinia, router)
app.use(router)
app.component('ElAlert', ElAlert)
app.component('ElButton', ElButton)
app.component('ElForm', ElForm)
app.component('ElFormItem', ElFormItem)
app.component('ElInput', ElInput)
app.mount('#app')
