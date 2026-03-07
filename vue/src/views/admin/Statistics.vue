<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">{{ card.title }}</div>
          <div :style="{ fontSize: '32px', fontWeight: 'bold', color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近7天新增学生 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
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

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="10">
        <el-card shadow="never" style="border-radius: 8px;">
          <template #header><span style="font-weight:600;">全校学生求职情况（是否已录用）</span></template>
          <div ref="pieRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never" style="border-radius: 8px;">
          <template #header><span style="font-weight:600;">全校学生投递简历情况（近6个月）</span></template>
          <div ref="barRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生面邀 & 入职列表 -->
    <el-card shadow="never" style="border-radius: 8px;">
      <template #header><span style="font-weight:600;">学生面邀 &amp; 入职情况</span></template>
      <el-table :data="studentList" stripe size="small" style="width: 100%;">
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="投递" width="90" align="center">
          <template #default="{ row }">
            <el-popover trigger="click" placement="top" width="160">
              <template #default>
                <div style="text-align:center;">共投递 <b>{{ row.totalApps }}</b> 份简历</div>
              </template>
              <template #reference>
                <el-tag type="info" size="small" style="cursor:pointer;">{{ row.totalApps }}</el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="面邀" align="center">
          <template #default="{ row }">
            <el-popover trigger="click" placement="right" width="220">
              <template #default>
                <div>共收到 <b>{{ row.interviewList.length }}</b> 次面试邀请</div>
                <div v-if="row.interviewList.length" style="margin-top: 6px;">
                  <el-tag
                    v-for="c in row.interviewList" :key="c"
                    size="small" type="warning"
                    style="margin: 2px;">{{ c }}</el-tag>
                </div>
              </template>
              <template #reference>
                <el-tag
                  :type="row.interviewList.length > 0 ? 'warning' : 'info'"
                  size="small" style="cursor:pointer;">{{ row.interviewList.length }}</el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="已录用" align="center">
          <template #default="{ row }">
            <el-popover trigger="click" placement="left" width="220">
              <template #default>
                <div>共录用 <b>{{ row.offerList.length }}</b> 次</div>
                <div v-if="row.offerList.length" style="margin-top: 6px;">
                  <el-tag
                    v-for="c in row.offerList" :key="c"
                    size="small" type="success"
                    style="margin: 2px;">{{ c }}</el-tag>
                </div>
              </template>
              <template #reference>
                <el-tag
                  :type="row.offerList.length > 0 ? 'success' : 'info'"
                  size="small" style="cursor:pointer;">{{ row.offerList.length }}</el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!studentList.length" style="text-align:center; color:#999; padding: 24px;">暂无数据</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const statCards = ref([
  { title: '学生总数', value: 0, color: '#409EFF' },
  { title: '企业总数', value: 0, color: '#67C23A' },
  { title: '今日新增岗位', value: 0, color: '#E6A23C' },
  { title: '待审核企业', value: 0, color: '#F56C6C' },
])
const dailyStudents = ref([])
const studentList = ref([])
const appStatusCounts = ref([])
const monthlyStats = ref([])

const pieRef = ref(null)
const barRef = ref(null)

onMounted(async () => {
  try {
    const data = await http.get('/api/statistics/admin/overview')
    statCards.value[0].value = data.totalStudents
    statCards.value[1].value = data.totalCompanies
    statCards.value[2].value = data.todayJobs
    statCards.value[3].value = data.pendingCompanies
    dailyStudents.value = data.dailyStudents || []
    appStatusCounts.value = data.appStatusCounts || []
    monthlyStats.value = data.monthlyStats || []
    studentList.value = data.studentList || []
    initCharts()
  } catch (e) {
    ElMessage.error(e.message)
  }
})

function initCharts() {
  const pie = echarts.init(pieRef.value)
  pie.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, orient: 'horizontal' },
    color: ['#67C23A', '#909399'],
    series: [{
      type: 'pie', radius: ['40%', '65%'], center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: appStatusCounts.value.map(i => ({ name: i.name, value: i.value }))
    }]
  })

  const bar = echarts.init(barRef.value)
  bar.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: { type: 'category', data: monthlyStats.value.map(i => i.month) },
    yAxis: { type: 'value', minInterval: 1 },
    color: ['#409EFF'],
    series: [{
      name: '投递数', type: 'bar', barWidth: '50%',
      data: monthlyStats.value.map(i => i.applications),
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }]
  })

  window.addEventListener('resize', () => { pie.resize(); bar.resize() })
}
</script>
