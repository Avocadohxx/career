<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <img src="@/assets/logo.png" class="login-logo" />
        <h2 class="login-title">注册账号</h2>
        <p class="login-sub">大学生就业服务管理平台</p>
      </div>

      <el-form ref="formRef" :model="regForm" :rules="rules" label-width="0" class="login-form">

        <el-form-item prop="role">
          <el-select v-model="regForm.role" placeholder="请选择注册角色" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="辅导员" value="counselor" />
            <el-option label="企业" value="company" />
          </el-select>
        </el-form-item>

        <el-form-item prop="username">
          <el-input v-model="regForm.username" placeholder="登录账号" :prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="regForm.password" placeholder="密码（至少6位）" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item>
          <el-input v-model="regForm.realName" placeholder="真实姓名（选填）" :prefix-icon="Avatar" />
        </el-form-item>

        <el-form-item>
          <el-input v-model="regForm.email" placeholder="邮箱（选填）" :prefix-icon="Message" />
        </el-form-item>

        <el-form-item>
          <el-input v-model="regForm.phone" placeholder="手机号（选填）" :prefix-icon="Phone" />
        </el-form-item>

        <!-- 学生字段 -->
        <template v-if="regForm.role === 'student'">
          <el-form-item>
            <el-input v-model="regForm.school" placeholder="所在学校（选填）" :prefix-icon="School" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="regForm.department" placeholder="所在院系（选填）" :prefix-icon="OfficeBuilding" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="regForm.major" placeholder="所学专业（选填）" :prefix-icon="Reading" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="regForm.counselorId" placeholder="选择指导老师（选填）" style="width: 100%;" clearable>
              <el-option
                v-for="c in counselorOptions" :key="c.id"
                :label="c.realName + (c.department ? ' · ' + c.department : '')"
                :value="c.id" />
            </el-select>
          </el-form-item>
        </template>

        <!-- 辅导员字段 -->
        <template v-if="regForm.role === 'counselor'">
          <el-form-item>
            <el-input v-model="regForm.department" placeholder="所属院系（选填）" :prefix-icon="OfficeBuilding" />
          </el-form-item>
        </template>

        <!-- 企业字段 -->
        <template v-if="regForm.role === 'company'">
          <el-form-item prop="companyName">
            <el-input v-model="regForm.companyName" placeholder="企业名称（必填）" :prefix-icon="OfficeBuilding" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="regForm.industry" placeholder="所属行业（选填）" :prefix-icon="Briefcase" />
          </el-form-item>
        </template>

        <el-button class="login-btn" :loading="loading" @click="handleRegister">注 册</el-button>

        <div class="switch-link">
          已有账号？<el-button type="primary" link @click="router.push('/login')">返回登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar, Message, Phone, School, OfficeBuilding, Reading, Briefcase } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const formRef = ref(null)
const counselorOptions = ref([])

const regForm = reactive({
  role: '',
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  school: '',
  department: '',
  major: '',
  counselorId: null,
  companyName: '',
  industry: ''
})

onMounted(async () => {
  try {
    const res = await fetch('http://localhost:9090/api/counselor/all')
    const json = await res.json()
    if (json.code === 200) counselorOptions.value = json.data || []
  } catch { /* ignore */ }
})

const rules = {
  role: [{ required: true, message: '请选择注册角色', trigger: 'change' }],
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await fetch('http://localhost:9090/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(regForm)
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(data.message || '注册失败')
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

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  width: 60px;
  height: 60px;
  margin-bottom: 14px;
}

.login-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: bold;
  color: #4f6395;
}

.login-sub {
  margin: 0;
  font-size: 13px;
  color: #aaa;
}

.login-form .el-form-item {
  margin-bottom: 16px;
}

.login-btn {
  width: 100%;
  margin-top: 6px;
  height: 42px;
  background-color: #4f6395;
  border-color: #4f6395;
  color: #fff;
  font-size: 16px;
  letter-spacing: 6px;
  border-radius: 4px;
  transition: background 0.2s;
}

.login-btn:hover,
.login-btn:focus {
  background-color: #3a4f82;
  border-color: #3a4f82;
  color: #fff;
}

.switch-link {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #999;
}
</style>
