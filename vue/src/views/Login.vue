<template>
  <div class="login-page">
    <div class="login-box">
      <!-- 顶部 Logo + 标题 -->
      <div class="login-header">
        <img src="@/assets/logo.png" class="login-logo" />
        <h2 class="login-title">就业指导系统</h2>
        <p class="login-sub">大学生就业服务管理平台</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginForm"
        :model="form"
        :rules="rules"
        label-width="0"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="账号"
            prefix-icon="el-icon-user"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            placeholder="密码"
            prefix-icon="el-icon-lock"
            show-password
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>

        <el-button
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await fetch('http://localhost:9090/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: this.form.username, password: this.form.password })
          })
          const data = await res.json()
          if (data.code === 200) {
            localStorage.setItem('token', data.data.token)
            localStorage.setItem('role', data.data.role)
            localStorage.setItem('realName', data.data.realName || '')
            this.$message.success('登录成功')
            this.$router.push('/')
          } else {
            this.$message.error(data.message || '登录失败')
          }
        } catch (e) {
          this.$message.error('网络异常，请检查后端服务是否启动')
        } finally {
          this.loading = false
        }
      })
    }
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
  margin-bottom: 20px;
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
</style>
