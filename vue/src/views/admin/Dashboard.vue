<template>
  <div>
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" style="border-radius: 8px;">
          <div style="display: flex; align-items: center; gap: 16px;">
            <el-icon :size="36" :style="{ color: card.color }">
              <component :is="card.icon" />
            </el-icon>
            <div>
              <div style="font-size: 13px; color: #999;">{{ card.title }}</div>
              <div style="font-size: 28px; font-weight: bold; color: #333;">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="24">
        <el-card shadow="hover" style="border-radius: 8px;">
          <template #header><span>近7天新增学生数</span></template>
          <div style="height: 200px; display: flex; align-items: flex-end; gap: 8px; padding: 0 8px;">
            <div v-for="item in dailyStudents" :key="item.date"
                 style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px;">
              <span style="font-size: 12px; color: #333;">{{ item.count }}</span>
              <div :style="{ height: Math.max(item.count * 20, 4) + 'px', background: '#4f6395', borderRadius: '3px 3px 0 0', width: '100%' }"></div>
              <span style="font-size: 11px; color: #999;">{{ item.date.slice(5) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="border-radius: 8px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>最近注册企业</span>
        </div>
      </template>
      <el-table :data="recentCompanies" size="small">
        <el-table-column prop="companyName" label="企业名称" />
        <el-table-column prop="industry" label="行业" />
        <el-table-column prop="createAt" label="注册时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="goAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const statCards = ref([
  { title: '总学生数', value: 0, icon: 'UserFilled', color: '#409EFF' },
  { title: '总企业数', value: 0, icon: 'OfficeBuilding', color: '#67C23A' },
  { title: '今日新增岗位', value: 0, icon: 'Briefcase', color: '#E6A23C' },
  { title: '待审核企业', value: 0, icon: 'Warning', color: '#F56C6C' },
])
const dailyStudents = ref([])
const recentCompanies = ref([])

onMounted(async () => {
  try {
    const data = await http.get('/api/statistics/admin/overview')
    statCards.value[0].value = data.totalStudents
    statCards.value[1].value = data.totalCompanies
    statCards.value[2].value = data.todayJobs
    statCards.value[3].value = data.pendingCompanies
    dailyStudents.value = data.dailyStudents || []
    recentCompanies.value = data.recentCompanies || []
  } catch (e) {
    ElMessage.error(e.message)
  }
})

function statusType(s) { return s === 0 ? 'warning' : s === 1 ? 'success' : 'danger' }
function statusText(s) { return s === 0 ? '待审核' : s === 1 ? '已通过' : '已拒绝' }
function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
function goAudit() { router.push('/admin/company') }
</script>
