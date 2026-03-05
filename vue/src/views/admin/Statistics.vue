<template>
  <div>
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">{{ card.title }}</div>
          <div :style="{ fontSize: '32px', fontWeight: 'bold', color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="hover" style="border-radius: 8px;">
          <template #header><span>近7天新增学生（每日）</span></template>
          <div style="height: 220px; display: flex; align-items: flex-end; gap: 12px; padding: 16px;">
            <div v-for="item in dailyStudents" :key="item.date"
                 style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px;">
              <span style="font-size: 13px; font-weight: bold; color: #4f6395;">{{ item.count }}</span>
              <div :style="{ minHeight: '4px', height: Math.max(item.count * 24, 4) + 'px', background: 'linear-gradient(180deg, #6b80b5, #4f6395)', borderRadius: '4px 4px 0 0', width: '100%' }"></div>
              <span style="font-size: 11px; color: #999; white-space: nowrap;">{{ item.date.slice(5) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const statCards = ref([
  { title: '学生总数', value: 0, color: '#409EFF' },
  { title: '企业总数', value: 0, color: '#67C23A' },
  { title: '今日新增岗位', value: 0, color: '#E6A23C' },
  { title: '待审核企业', value: 0, color: '#F56C6C' },
])
const dailyStudents = ref([])

onMounted(async () => {
  try {
    const data = await http.get('/api/statistics/admin/overview')
    statCards.value[0].value = data.totalStudents
    statCards.value[1].value = data.totalCompanies
    statCards.value[2].value = data.todayJobs
    statCards.value[3].value = data.pendingCompanies
    dailyStudents.value = data.dailyStudents || []
  } catch (e) {
    ElMessage.error(e.message)
  }
})
</script>
