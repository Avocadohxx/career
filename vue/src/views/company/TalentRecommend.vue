<template>
  <div>
    <p style="color:#909399; margin-bottom:20px;">以下求职者根据您发布的岗位需求为您智能推荐</p>

    <div v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无推荐求职者" />
      <el-row :gutter="16">
        <el-col v-for="s in list" :key="s.id" :xs="24" :sm="12" :lg="8" style="margin-bottom:16px;">
          <el-card shadow="hover" style="border-radius:8px; height:100%;">
            <div style="display:flex; align-items:center; gap:12px; margin-bottom:12px;">
              <el-avatar :size="48" icon="UserFilled" style="background:#4f6395;" />
              <div>
                <div style="font-weight:600; font-size:15px;">{{ maskName(s.realName) }}</div>
                <div style="color:#909399; font-size:13px;">{{ s.major }} · {{ s.department }}</div>
              </div>
            </div>
            <div style="margin-bottom:10px;">
              <span style="color:#606266; font-size:13px;">求职意向：</span>
              <span style="font-size:13px;">{{ s.jobIntention || '未填写' }}</span>
            </div>
            <div style="margin-bottom:12px; display:flex; flex-wrap:wrap; gap:6px;">
              <el-tag v-for="skill in splitSkills(s.jobIntention)" :key="skill" size="small" type="info">{{ skill }}</el-tag>
            </div>
            <div style="display:flex; gap:8px;">
              <el-button size="small" @click="viewResume(s.id)">查看简历</el-button>
              <el-button size="small" type="primary" @click="inviteInterview(s.id)">发送面试邀请</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 简历弹窗 -->
    <el-dialog v-model="resumeVisible" title="学生简历" width="600px">
      <div v-if="resume">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ resume.realName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ resume.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ resume.email }}</el-descriptions-item>
          <el-descriptions-item label="求职意向">{{ resume.jobIntention }}</el-descriptions-item>
          <el-descriptions-item label="意向城市">{{ resume.cityIntention }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ resume.school }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:12px;">
          <div style="font-weight:600; margin-bottom:6px;">自我评价</div>
          <div style="color:#606266;">{{ resume.selfEval || '暂无' }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无简历" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const resumeVisible = ref(false)
const resume = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await http.get('/api/student/all')
    list.value = res || []
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
})

async function viewResume(studentId) {
  resumeVisible.value = true
  resume.value = null
  try {
    const data = await http.get(`/api/resume/by-student/${studentId}`)
    // merge student info
    const student = list.value.find(s => s.id === studentId) || {}
    resume.value = { ...student, ...data }
  } catch (e) { ElMessage.error(e.message) }
}

async function inviteInterview(studentId) {
  ElMessage.info('请前往应聘管理页面进行面试邀请操作')
}

function maskName(name) {
  if (!name || name.length < 2) return name
  return name[0] + '**'
}

function splitSkills(intention) {
  if (!intention) return []
  return intention.split(/[,，、\s]+/).filter(Boolean).slice(0, 3)
}
</script>
