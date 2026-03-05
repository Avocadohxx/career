<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px; border-radius: 8px;">
      <el-form :model="query" inline>
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="学生姓名" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="userName" label="账号" width="120" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="jobIntention" label="求职意向" />
        <el-table-column prop="phone" label="联系方式" width="130" />
      </el-table>
      <el-pagination style="margin-top: 12px; justify-content: flex-end;"
        layout="total, prev, pager, next"
        :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="loadList" />
    </el-card>
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
const query = reactive({ name: '' })
const counselorId = localStorage.getItem('userId') || ''

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/student/list', {
      page: page.value, size: pageSize, name: query.name, counselorId
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function resetQuery() {
  query.name = ''
  page.value = 1
  loadList()
}
</script>
