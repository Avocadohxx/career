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
          <div style="font-size: 32px; font-weight: bold; color: '#67C23A';">{{ stats.monthPosts }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="border-radius: 8px;">
      <template #header><span>我的学生列表</span></template>
      <el-table :data="students" size="small" stripe>
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="jobIntention" label="求职意向" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const stats = ref({ totalStudents: 0, monthPosts: 0 })
const students = ref([])

onMounted(async () => {
  const counselorId = localStorage.getItem('userId') || 1
  try {
    const data = await http.get('/api/statistics/counselor/overview', { counselorId })
    stats.value.totalStudents = data.totalStudents
    stats.value.monthPosts = data.monthPosts
    students.value = data.myStudents || []
  } catch (e) {
    ElMessage.error(e.message)
  }
})
</script>
