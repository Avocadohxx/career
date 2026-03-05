<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px; border-radius: 8px;">
      <el-form :model="query" inline>
        <el-form-item label="企业名称">
          <el-input v-model="query.name" placeholder="企业名称" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="审核状态">
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
        <el-table-column prop="companyName" label="企业名称" min-width="130" />
        <el-table-column prop="industry" label="行业" width="100" />
        <el-table-column prop="scale" label="规模" width="100" />
        <el-table-column prop="phone" label="联系方式" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="createAt" label="注册时间" width="160">
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
            <el-popconfirm title="确认删除？" @confirm="deleteRow(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 12px; justify-content: flex-end;"
        layout="total, prev, pager, next"
        :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="loadList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="企业详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="企业名称">{{ detail.companyName }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ detail.industry }}</el-descriptions-item>
        <el-descriptions-item label="规模">{{ detail.scale }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(detail.createAt) }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ detail.description }}</el-descriptions-item>
      </el-descriptions>
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
const query = reactive({ name: '', status: null })
const detailVisible = ref(false)
const detail = ref({})

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/company/list', { page: page.value, size: pageSize, ...query })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function resetQuery() {
  Object.assign(query, { name: '', status: null })
  page.value = 1
  loadList()
}

async function audit(id, status) {
  try {
    await http.put('/api/company/audit/' + id + '?status=' + status)
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    loadList()
  } catch (e) { ElMessage.error(e.message) }
}

async function openDetail(row) {
  try {
    detail.value = await http.get('/api/company/' + row.id)
    detailVisible.value = true
  } catch (e) { ElMessage.error(e.message) }
}

async function deleteRow(id) {
  try {
    await http.delete('/api/company/' + id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) { ElMessage.error(e.message) }
}

function statusType(s) { return s === 0 ? 'warning' : s === 1 ? 'success' : 'danger' }
function statusText(s) { return s === 0 ? '待审核' : s === 1 ? '已通过' : '已拒绝' }
function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
