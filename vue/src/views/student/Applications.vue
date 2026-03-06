<template>
  <div>
    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe
        :row-style="({ row }) => row.status === 4 ? { color:'#c0c4cc' } : {}">
        <el-table-column prop="companyName" label="企业" min-width="130" />
        <el-table-column prop="jobTitle" label="应聘岗位" min-width="130" />
        <el-table-column prop="applyTime" label="投递时间" width="160">
          <template #default="{ row }">{{ fmt(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="interviewTime" label="面试时间" width="160">
          <template #default="{ row }">{{ fmt(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <!-- status=2: 企业已邀请，等待学生确认 -->
            <template v-if="row.status === 2">
              <el-button type="primary" size="small" @click="respond(row.id, 5)">确认面试</el-button>
              <el-button type="danger" size="small" @click="respond(row.id, 4)">拒绝</el-button>
            </template>
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
const studentId = parseInt(localStorage.getItem('userId') || '0')

// 0待查看 1已查看 2已邀请 3已录用 4已拒绝 5待面试 6面试不通过
const STATUS_TEXT = ['待查看', '已查看', '已邀请', '已录用', '已拒绝', '待面试', '面试不通过']
const STATUS_TYPE = ['info', 'warning', 'primary', 'success', 'danger', 'warning', 'danger']
function statusText(s) { return STATUS_TEXT[s] ?? '' }
function statusType(s) { return STATUS_TYPE[s] ?? 'info' }
function fmt(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }

onMounted(loadList)

async function loadList() {
  if (!studentId) return
  loading.value = true
  try {
    const res = await http.get('/api/application/my-list-detail', { studentId, page: page.value, size: pageSize })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

async function respond(id, status) {
  try {
    await fetch(`http://localhost:9090/api/application/status/${id}?status=${status}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    ElMessage.success(status === 5 ? '已确认面试' : '已拒绝邀请')
    loadList()
  } catch { ElMessage.error('操作失败') }
}
</script>
