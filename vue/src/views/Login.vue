<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <img src="@/assets/logo.png" class="login-logo" />
        <h2 class="login-title">就业指导系统</h2>
        <p class="login-sub">大学生就业服务管理平台</p>
      </div>

      <!-- 锁定提示 -->
      <div v-if="locked" class="lock-alert">
        <p class="lock-text">您已连续输错5次密码，账号已锁定！</p>
        <p class="lock-hint">请点击下方"忘记密码"重置密码后再登录</p>
      </div>

      <el-form ref="loginFormRef" :model="form" :rules="rules" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" :disabled="locked" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码" :prefix-icon="Lock" show-password :disabled="locked" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button class="login-btn" :loading="loading" :disabled="locked" @click="handleLogin">登 录</el-button>
        <div class="switch-link">
          没有账号？<el-button type="primary" link @click="router.push('/register')">立即注册</el-button>
          <span class="link-divider">|</span>
          <el-button type="danger" link @click="router.push('/forgot-password')">忘记密码</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const locked = ref(false)
const loginFormRef = ref(null)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
}

async function handleLogin() {
  if (locked.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await fetch('http://localhost:9090/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: form.username, password: form.password })
    })
    const data = await res.json()
    if (data.code === 200) {
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('role', data.data.role)
      localStorage.setItem('realName', data.data.realName || '')
      localStorage.setItem('userName', data.data.userName || '')
      localStorage.setItem('userId', data.data.userId || '')
      if (data.data.companyStatus !== undefined && data.data.companyStatus !== null) {
        localStorage.setItem('companyStatus', data.data.companyStatus)
      }
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      // 检查是否被锁定
      if (data.message && data.message.startsWith('LOCKED:')) {
        locked.value = true
        ElMessage.error(data.message.replace('LOCKED:', ''))
      } else {
        ElMessage.error(data.message || '登录失败')
      }
    }
  } catch {
    ElMessage.error('网络异常，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3a4f82 0%, #4f6395 60%, #6b80b5 100%);
}
.login-box {
  width: 420px;
  background: #fff;
  border-radius: 10px;
  padding: 48px 40px 40px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.22);
}
.login-header { text-align: center; margin-bottom: 36px; }
.login-logo { width: 60px; height: 60px; margin-bottom: 14px; }
.login-title { margin: 0 0 8px; font-size: 22px; font-weight: bold; color: #4f6395; }
.login-sub { margin: 0; font-size: 13px; color: #aaa; }
.login-form .el-form-item { margin-bottom: 20px; }
.login-btn {
  width: 100%; margin-top: 6px; height: 42px;
  background-color: #4f6395; border-color: #4f6395; color: #fff;
  font-size: 16px; letter-spacing: 6px; border-radius: 4px; transition: background 0.2s;
}
.login-btn:hover, .login-btn:focus { background-color: #3a4f82; border-color: #3a4f82; color: #fff; }
.switch-link { text-align: center; margin-top: 16px; font-size: 13px; color: #999; }
.link-divider { margin: 0 6px; color: #ddd; }

/* 锁定提示样式 */
.lock-alert {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  padding: 14px 18px;
  margin-bottom: 20px;
  text-align: center;
}
.lock-text {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: bold;
  color: #ff4d4f;
}
.lock-hint {
  margin: 0;
  font-size: 13px;
  color: #ff7875;
}
</style>
