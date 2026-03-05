<template>
  <div>
    <el-alert type="info" show-icon :closable="false" style="margin-bottom: 16px; border-radius: 8px;">
      <template #title>以下岗位根据您的简历与求职意向为您智能推荐</template>
    </el-alert>

    <el-row :gutter="16" v-loading="loading">
      <el-col :span="8" v-for="job in list" :key="job.id" style="margin-bottom: 16px;">
        <el-card shadow="hover" style="border-radius: 8px;">
          <div style="font-size: 16px; font-weight: bold; margin-bottom: 8px;">{{ job.title }}</div>
          <div style="font-size: 13px; color: #666; margin-bottom: 4px;">
            <el-icon><Location /></el-icon> {{ job.city }}
          </div>
          <div style="color: #f56c6c; font-size: 16px; font-weight: bold; margin: 8px 0;">{{ job.salaryMin }}-{{ job.salaryMax }}K</div>
          <div style="margin-bottom: 8px;">
            <el-tag v-for="skill in (job.skills||'').split(',').filter(Boolean)" :key="skill"
                    size="small" style="margin-right: 4px; margin-bottom: 4px;">{{ skill }}</el-tag>
          </div>
          <div style="font-size: 12px; color: #aaa; margin-bottom: 12px;">根据您的求职意向推荐</div>
          <div style="display: flex; gap: 8px;">
            <el-button size="small" @click="openDetail(job)">查看详情</el-button>
            <el-button size="small" type="primary" @click="apply(job)">立即投递</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <div v-if="!loading && list.length === 0" style="text-align: center; padding: 40px; color: #999;">暂无推荐岗位，请完善简历求职意向</div>

    <el-dialog v-model="detailVisible" title="岗位详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ detail.city }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ detail.salaryMin }}-{{ detail.salaryMax }}K</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ detail.education }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.jobType }}</el-descriptions-item>
        <el-descriptions-item label="技能要求">{{ detail.skills }}</el-descriptions-item>
        <el-descriptions-item label="岗位描述" :span="2">
          <div style="white-space: pre-wrap;">{{ detail.description }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="apply(detail); detailVisible = false">立即投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref({})
const studentId = parseInt(localStorage.getItem('userId') || '0')

onMounted(async () => {
  loading.value = true
  try {
    const res = await http.get('/api/job/list', { page: 1, size: 9, status: 1 })
    list.value = res.records || []
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
})

function openDetail(job) { detail.value = job; detailVisible.value = true }

async function apply(job) {
  if (!studentId) { ElMessage.warning('请先完善个人信息'); return }
  try {
    await http.post('/api/application/apply', { studentId, jobId: job.id, companyId: job.companyId })
    ElMessage.success('投递成功')
  } catch (e) { ElMessage.error(e.message) }
}
</script>
