<template>
  <div class="login-page">
    <div class="login-visual">
      <h1>发现兴趣，加入同伴，组织更好的校园活动</h1>
      <p>学生可以轻松浏览社团、提交申请、报名活动；负责人和管理员可以在后台完成审核、发布和统计。</p>
    </div>
    <div class="login-panel">
      <div class="login-box">
        <h2>{{ mode === 'login' ? '登录系统' : '注册学生账号' }}</h2>
        <el-form :model="form" label-position="top">
          <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
          <template v-if="mode === 'register'">
            <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
            <el-form-item label="学号"><el-input v-model="form.studentNo" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          </template>
          <el-button type="primary" style="width:100%" @click="submit">{{ mode === 'login' ? '登录' : '注册' }}</el-button>
          <el-button text style="width:100%;margin-top:10px" @click="mode = mode === 'login' ? 'register' : 'login'">
            {{ mode === 'login' ? '没有账号？立即注册' : '已有账号？返回登录' }}
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api'

const router = useRouter()
const mode = ref('login')
const form = reactive({ username: '', password: '', realName: '', studentNo: '', phone: '', email: '' })

const submit = async () => {
  if (mode.value === 'register') {
    await register(form)
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
    return
  }
  const data = await login(form)
  localStorage.setItem('token', data.token)
  localStorage.setItem('user', JSON.stringify(data.user))
  localStorage.setItem('roles', JSON.stringify(data.roles))
  ElMessage.success('登录成功')
  router.push(data.roles.includes('ADMIN') || data.roles.includes('LEADER') ? '/admin' : '/')
}
</script>
