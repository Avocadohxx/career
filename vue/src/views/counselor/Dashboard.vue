<template>
  <div>
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">我的学生总数</div>
          <div style="font-size: 32px; font-weight: bold; color: #409EFF;">{{ stats.totalStudents }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">本月发布指导帖数</div>
          <div style="font-size: 32px; font-weight: bold; color: #67C23A;">{{ stats.monthPosts }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">本月发布公开课数</div>
          <div style="font-size: 32px; font-weight: bold; color: #E6A23C;">{{ stats.monthCourses }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="border-radius: 8px;">
      <template #header>
        <span>我的学生列表</span>
        <span style="margin-left: 8px; font-size: 13px; color: #909399;">共 {{ total }} 人</span>
      </template>
      <el-table :data="students" v-loading="loading" stripe>
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="jobIntention" label="求职意向" />
      </el-table>
      <el-pagination style="margin-top: 12px; justify-content: flex-end;"
        layout="total, prev, pager, next"
        :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="loadStudents" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const counselorId = parseInt(localStorage.getItem('userId') || '0')
const stats = ref({ totalStudents: 0, monthPosts: 0, monthCourses: 0 })
const students = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)

onMounted(() => {
  loadOverview()
  loadStudents()
})

async function loadOverview() {
  try {
    const data = await http.get('/api/statistics/counselor/overview', { counselorId })
    stats.value.monthPosts = data.monthPosts || 0
    stats.value.monthCourses = data.monthCourses ?? 0
  } catch (e) { ElMessage.error(e.message) }
}

async function loadStudents() {
  loading.value = true
  try {
    const res = await http.get('/api/student/list', { counselorId, page: page.value, size: pageSize })
    students.value = res.records || []
    total.value = res.total || 0
    stats.value.totalStudents = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}
</script>
