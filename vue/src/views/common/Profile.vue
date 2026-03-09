<template>
  <div style="max-width: 700px; margin: 0 auto;">
    <el-card shadow="never" style="border-radius: 8px;" v-loading="loading">
      <template #header>
        <span style="font-weight: 600;">个人信息</span>
      </template>

      <!-- 学生 -->
      <el-form v-if="role === 'student'" ref="formRef" :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="学号"><el-input v-model="form.userName" disabled /></el-form-item>
        <el-form-item label="学校"><el-input v-model="form.school" /></el-form-item>
        <el-form-item label="院系"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="求职意向"><el-input v-model="form.jobIntention" /></el-form-item>
        <el-form-item label="意向城市"><el-input v-model="form.cityIntention" /></el-form-item>
        <el-form-item label="指导老师">
          <el-select v-model="form.counselorId" placeholder="选择指导老师" style="width:100%;" clearable>
            <el-option
              v-for="c in counselorOptions" :key="c.id"
              :label="c.realName + (c.department ? ' · ' + c.department : '')"
              :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" placeholder="不修改请留空" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>

      <!-- 辅导员 -->
      <el-form v-else-if="role === 'counselor'" ref="formRef" :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="账号"><el-input v-model="form.userName" disabled /></el-form-item>
        <el-form-item label="院系"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" placeholder="不修改请留空" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>

      <!-- 企业 -->
      <el-form v-else-if="role === 'company'" ref="formRef" :model="form" label-width="110px">
        <el-form-item label="企业名称"><el-input v-model="form.companyName" /></el-form-item>
        <el-form-item label="账号"><el-input v-model="form.userName" disabled /></el-form-item>
        <el-form-item label="行业"><el-input v-model="form.industry" /></el-form-item>
        <el-form-item label="规模"><el-input v-model="form.scale" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="企业介绍"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" placeholder="不修改请留空" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>

      <!-- 管理员 -->
      <el-form v-else ref="formRef" :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="账号"><el-input v-model="form.userName" disabled /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" placeholder="不修改请留空" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const role = localStorage.getItem('role') || ''
const userId = parseInt(localStorage.getItem('userId') || '0')
const counselorOptions = ref([])

const loading = ref(false)
const saving = ref(false)
const form = reactive({ newPassword: '' })

const apiMap = {
  student: { get: `/api/student/${userId}`, put: '/api/student/update' },
  counselor: { get: `/api/counselor/${userId}`, put: '/api/counselor/update' },
  company: { get: `/api/company/${userId}`, put: '/api/company/update' },
  admin: { get: `/api/admin/${userId}`, put: '/api/admin/update' },
}

onMounted(async () => {
  if (role === 'student') {
    try {
      const list = await http.get('/api/counselor/all')
      counselorOptions.value = list || []
    } catch { /* ignore */ }
  }
  const api = apiMap[role]
  if (!api || !userId) return
  loading.value = true
  try {
    const data = await http.get(api.get)
    Object.assign(form, data)
    form.newPassword = ''
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
})

async function handleSave() {
  const api = apiMap[role]
  if (!api) return
  saving.value = true
  try {
    const payload = { ...form }
    if (!payload.newPassword) delete payload.newPassword
    else payload.password = payload.newPassword
    delete payload.newPassword
    await http.put(api.put, payload)
    // 更新 localStorage 显示名
    if (form.realName) localStorage.setItem('realName', form.realName)
    if (form.companyName) localStorage.setItem('realName', form.companyName)
    ElMessage.success('保存成功')
    form.newPassword = ''
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}
</script>
