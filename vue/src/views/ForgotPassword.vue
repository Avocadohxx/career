<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <img src="@/assets/logo.png" class="login-logo" />
        <h2 class="login-title">重置密码</h2>
        <p class="login-sub">请输入您注册时填写的信息进行身份验证</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="login-form">

        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="辅导员" value="counselor" />
            <el-option label="企业" value="company" />
          </el-select>
        </el-form-item>

        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="登录账号" :prefix-icon="User" />
        </el-form-item>

        <!-- 学生/辅导员验证：真实姓名 -->
        <template v-if="form.role === 'student' || form.role === 'counselor'">
          <el-form-item>
            <el-input v-model="form.realName" placeholder="真实姓名" :prefix-icon="Avatar" />
          </el-form-item>
        </template>

        <!-- 企业验证：企业名称 -->
        <template v-if="form.role === 'company'">
          <el-form-item prop="companyName">
            <el-input v-model="form.companyName" placeholder="企业名称" :prefix-icon="OfficeBuilding" />
          </el-form-item>
        </template>

        <el-form-item>
          <el-input v-model="form.email" placeholder="注册邮箱" :prefix-icon="Message" />
        </el-form-item>

        <el-form-item>
          <el-input v-model="form.phone" placeholder="注册手机号" :prefix-icon="Phone" />
        </el-form-item>

        <el-divider content-position="center">设置新密码</el-divider>

        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="新密码（至少6位）" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" placeholder="确认新密码" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-button class="login-btn" :loading="loading" @click="handleReset">确认重置</el-button>

        <div class="switch-link">
          <el-button type="primary" link @click="router.push('/login')">返回登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar, Message, Phone, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  role: '',
  username: '',
  realName: '',
  companyName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPwd = (_rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
  password: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

async function handleReset() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await fetch('http://localhost:9090/api/auth/reset-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        role: form.role,
        username: form.username,
        realName: form.realName,
        companyName: form.companyName,
        email: form.email,
        phone: form.phone,
        password: form.password
      })
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('密码重置成功，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(data.message || '重置失败')
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3a4f82 0%, #4f6395 60%, #6b80b5 100%);
  padding: 24px 0;
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
.login-form .el-form-item { margin-bottom: 16px; }
.login-btn {
  width: 100%; margin-top: 6px; height: 42px;
  background-color: #4f6395; border-color: #4f6395; color: #fff;
  font-size: 16px; letter-spacing: 6px; border-radius: 4px; transition: background 0.2s;
}
.login-btn:hover, .login-btn:focus { background-color: #3a4f82; border-color: #3a4f82; color: #fff; }
.switch-link { text-align: center; margin-top: 16px; font-size: 13px; color: #999; }
</style>
