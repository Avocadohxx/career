<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 20px; border-radius: 8px; background: linear-gradient(135deg, #4f6395, #6b80b5); color: #fff;">
      <div style="font-size: 18px; font-weight: bold;">Hi，{{ displayName }}，今天也要加油求职哦!</div>
      <div style="margin-top: 6px; font-size: 13px; opacity: 0.8;">祝你早日找到满意的工作</div>
    </el-card>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="card in quickCards" :key="card.title">
        <el-card shadow="hover" style="border-radius: 8px; cursor: pointer; text-align: center;" @click="$router.push(card.path)">
          <el-icon :size="28" :style="{ color: card.color, marginBottom: '8px' }">
            <component :is="card.icon" />
          </el-icon>
          <div style="font-size: 14px; color: #333;">{{ card.title }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="border-radius: 8px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>推荐岗位</span>
          <el-button type="primary" link @click="$router.push('/student/recommend')">查看全部</el-button>
        </div>
      </template>
      <el-row :gutter="12">
        <el-col :span="8" v-for="job in jobs" :key="job.id" style="margin-bottom: 12px;">
          <el-card shadow="hover" style="border-radius: 8px;">
            <div style="font-weight: bold; font-size: 14px; margin-bottom: 6px;">{{ job.title }}</div>
            <div style="font-size: 12px; color: #666; margin-bottom: 4px;">{{ job.city }}</div>
            <div style="color: #f56c6c; font-weight: bold; margin-bottom: 8px;">{{ job.salaryMin }}-{{ job.salaryMax }}K</div>
            <el-button size="small" type="primary" @click="$router.push('/student/search')">查看详情</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const displayName = ref(localStorage.getItem('realName') || localStorage.getItem('userName') || '同学')
const jobs = ref([])
const quickCards = [
  { title: '完善简历', path: '/student/resume', icon: 'Document', color: '#409EFF' },
  { title: '查看推荐岗位', path: '/student/recommend', icon: 'Star', color: '#E6A23C' },
  { title: '我的投递', path: '/student/applications', icon: 'Tickets', color: '#67C23A' },
  { title: '就业指导', path: '/student/guidance-posts', icon: 'Reading', color: '#909399' },
]

onMounted(async () => {
  try {
    const res = await http.get('/api/job/list', { page: 1, size: 6, status: 1 })
    jobs.value = res.records || []
  } catch (e) {
    ElMessage.error(e.message)
  }
})
</script>
