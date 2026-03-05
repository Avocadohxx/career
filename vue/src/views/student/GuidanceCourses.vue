<template>
  <div>
    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="课程名称" min-width="150" />
        <el-table-column prop="startTime" label="开课时间" width="160">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="剩余名额" width="100">
          <template #default="{ row }">{{ row.maxStudents - row.enrolled }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '报名中' : '已结束' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1 && !enrolledIds.has(row.id)"
                       size="small" type="primary" link @click="enroll(row)">报名</el-button>
            <el-button v-if="enrolledIds.has(row.id)"
                       size="small" type="danger" link @click="unenroll(row)">取消报名</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 12px; justify-content: flex-end;"
        layout="total, prev, pager, next"
        :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="loadList" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const enrolledIds = ref(new Set())
const studentId = parseInt(localStorage.getItem('userId') || '0')

onMounted(async () => {
  await loadList()
  if (studentId) {
    try {
      const enrolled = await http.get('/api/course/my-enrollments', { studentId })
      enrolledIds.value = new Set((enrolled || []).map(e => e.courseId))
    } catch (e) { /* ignore */ }
  }
})

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/course/list', { page: page.value, size: pageSize })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

async function enroll(course) {
  try {
    await http.post('/api/course/enroll/' + course.id + '?studentId=' + studentId)
    enrolledIds.value.add(course.id)
    course.enrolled++
    ElMessage.success('报名成功')
  } catch (e) { ElMessage.error(e.message) }
}

async function unenroll(course) {
  try {
    await http.delete('/api/course/unenroll/' + course.id + '?studentId=' + studentId)
    enrolledIds.value.delete(course.id)
    course.enrolled--
    ElMessage.success('已取消报名')
  } catch (e) { ElMessage.error(e.message) }
}

function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
