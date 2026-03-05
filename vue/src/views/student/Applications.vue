<template>
  <div>
    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="jobId" label="岗位ID" width="80" />
        <el-table-column prop="companyId" label="企业ID" width="80" />
        <el-table-column prop="applyTime" label="投递时间" width="160">
          <template #default="{ row }">{{ formatTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="当前状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="interviewTime" label="面试时间" width="160">
          <template #default="{ row }">{{ formatTime(row.interviewTime) }}</template>
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

onMounted(loadList)

async function loadList() {
  if (!studentId) return
  loading.value = true
  try {
    const res = await http.get('/api/application/my-list', { studentId, page: page.value, size: pageSize })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function statusType(s) { const m = ['info', 'warning', 'primary', 'success', 'danger']; return m[s] || 'info' }
function statusText(s) { return ['待查看', '已查看', '面试邀请', '已录用', '已拒绝'][s] || '' }
function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
