<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">我的学生总数</div>
          <div style="font-size: 30px; font-weight: bold; color: #409EFF;">{{ stats.totalStudents }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">本月发布指导帖数</div>
          <div style="font-size: 30px; font-weight: bold; color: #67C23A;">{{ stats.monthPosts }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">本月发布公开课数</div>
          <div style="font-size: 30px; font-weight: bold; color: #E6A23C;">{{ stats.monthCourses }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="border-radius: 8px; text-align: center;">
          <div style="font-size: 13px; color: #999; margin-bottom: 8px;">学生总投递数</div>
          <div style="font-size: 30px; font-weight: bold; color: #F56C6C;">{{ totalApps }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <!-- 环形饼图：已面试通过 / 未面试通过 -->
      <el-col :span="10">
        <el-card shadow="never" style="border-radius: 8px;">
          <template #header><span style="font-weight:600;">学生求职情况（是否已录用）</span></template>
          <div ref="pieRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <!-- 条形图：月度投递量 -->
      <el-col :span="14">
        <el-card shadow="never" style="border-radius: 8px;">
          <template #header><span style="font-weight:600;">学生投递简历情况（近6个月）</span></template>
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

const counselorId = parseInt(localStorage.getItem('userId') || '0')
const stats = ref({ totalStudents: 0, monthPosts: 0, monthCourses: 0 })
const totalApps = ref(0)
const monthlyStats = ref([])
const studentList = ref([])
const appStatusCounts = ref([])

const pieRef = ref(null)
const barRef = ref(null)

onMounted(async () => {
  try {
    const data = await http.get('/api/statistics/counselor/overview', { counselorId })
    stats.value.totalStudents = data.totalStudents || 0
    stats.value.monthPosts = data.monthPosts || 0
    stats.value.monthCourses = data.monthCourses ?? 0
    totalApps.value = data.totalApps || 0
    appStatusCounts.value = data.appStatusCounts || []
    monthlyStats.value = data.monthlyStats || []
    studentList.value = data.studentList || []
    initCharts()
  } catch (e) { ElMessage.error(e.message) }
})

function initCharts() {
  // 环形饼图：已面试通过 / 未面试通过
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

  // 条形图：近6个月投递数
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
