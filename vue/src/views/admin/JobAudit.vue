<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px; border-radius: 8px;">
      <el-form :model="query" inline>
        <el-form-item label="岗位名称">
          <el-input v-model="query.title" placeholder="岗位名称" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="query.city" placeholder="城市" clearable style="width: 120px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="岗位名称" min-width="130" />
        <el-table-column prop="companyId" label="企业ID" width="80" />
        <el-table-column prop="city" label="城市" width="120" />
        <el-table-column label="薪资范围" width="120">
          <template #default="{ row }">{{ row.salaryMin }}~{{ row.salaryMax }}K</template>
        </el-table-column>
        <el-table-column prop="jobType" label="类型" width="80" />
        <el-table-column prop="createAt" label="发布时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createAt) }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="success" link @click="audit(row.id, 1)">通过</el-button>
            <el-button v-if="row.status === 0" size="small" type="danger" link @click="audit(row.id, 2)">拒绝</el-button>
            <el-button size="small" type="primary" link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 12px; justify-content: flex-end;"
        layout="total, prev, pager, next"
        :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="loadList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="岗位详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ detail.city }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ detail.salaryMin }}~{{ detail.salaryMax }}K</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ detail.education }}</el-descriptions-item>
        <el-descriptions-item label="岗位类型">{{ detail.jobType }}</el-descriptions-item>
        <el-descriptions-item label="技能要求">{{ detail.skills }}</el-descriptions-item>
        <el-descriptions-item label="岗位描述" :span="2">
          <div style="white-space: pre-wrap;">{{ detail.description }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer v-if="detail.status === 0">
        <el-button type="success" @click="audit(detail.id, 1); detailVisible = false">审核通过</el-button>
        <el-button type="danger" @click="audit(detail.id, 2); detailVisible = false">审核拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const query = reactive({ title: '', city: '', status: null })
const detailVisible = ref(false)
const detail = ref({})

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/job/list', { page: page.value, size: pageSize, ...query })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function resetQuery() {
  Object.assign(query, { title: '', city: '', status: null })
  page.value = 1
  loadList()
}

async function audit(id, status) {
  try {
    await http.put('/api/job/audit/' + id + '?status=' + status)
    ElMessage.success(status === 1 ? '审核通过' : '审核拒绝')
    loadList()
  } catch (e) { ElMessage.error(e.message) }
}

async function openDetail(row) {
  try {
    detail.value = await http.get('/api/job/' + row.id)
    detailVisible.value = true
  } catch (e) { ElMessage.error(e.message) }
}

function statusType(s) { return s === 0 ? 'warning' : s === 1 ? 'success' : s === 2 ? 'danger' : 'info' }
function statusText(s) { return ['待审核', '已通过', '已拒绝', '已下线'][s] || '' }
function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
