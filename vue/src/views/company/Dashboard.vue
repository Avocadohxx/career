<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="never" style="border-radius:8px;">
          <div class="stat-card">
            <div class="stat-icon" style="background:#e8f4ff;"><el-icon size="24" color="#409EFF"><Briefcase /></el-icon></div>
            <div>
              <div class="stat-label">发布岗位数</div>
              <div class="stat-value">{{ overview.totalJobs ?? '-' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" style="border-radius:8px;">
          <div class="stat-card">
            <div class="stat-icon" style="background:#f0fff0;"><el-icon size="24" color="#67C23A"><Document /></el-icon></div>
            <div>
              <div class="stat-label">收到简历数</div>
              <div class="stat-value">{{ overview.totalApplications ?? '-' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" style="border-radius:8px;">
          <div class="stat-card">
            <div class="stat-icon" style="background:#fff8e1;"><el-icon size="24" color="#E6A23C"><Bell /></el-icon></div>
            <div>
              <div class="stat-label">待处理面试邀请</div>
              <div class="stat-value">{{ overview.pendingApplications ?? '-' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新投递 -->
    <el-card shadow="never" style="border-radius:8px;">
      <template #header><span style="font-weight:600;">最新投递记录</span></template>
      <el-table :data="recentApps" v-loading="loading" stripe>
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="jobTitle" label="应聘岗位" min-width="150" />
        <el-table-column prop="applyTime" label="投递时间" width="160">
          <template #default="{ row }">{{ fmt(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="80">
          <template #default="{ row }">
            <el-button type="primary" link @click="openFlow(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详细流程弹窗 -->
    <el-dialog v-model="flowVisible" title="投递详细流程" width="480px">
      <div v-if="flowRow">
        <el-descriptions :column="1" border style="margin-bottom:20px;">
          <el-descriptions-item label="学生">{{ flowRow.studentName }}</el-descriptions-item>
          <el-descriptions-item label="应聘岗位">{{ flowRow.jobTitle }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="statusType(flowRow.status)" size="small">{{ statusText(flowRow.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div style="font-weight:600; margin-bottom:12px; color:#303133;">流程记录</div>
        <el-timeline>
          <el-timeline-item
            :timestamp="'投递时间：' + fmt(flowRow.applyTime)"
            placement="top" color="#409EFF">
            提交申请
          </el-timeline-item>
          <el-timeline-item v-if="flowRow.status >= 1" placement="top" color="#909399">
            企业已查看
          </el-timeline-item>
          <el-timeline-item
            v-if="flowRow.interviewTime"
            :timestamp="'面试时间：' + fmt(flowRow.interviewTime)"
            placement="top" color="#409EFF">
            收到面试邀请
          </el-timeline-item>
          <el-timeline-item
            v-if="flowRow.status === 5 || flowRow.status === 3 || flowRow.status === 6"
            placement="top" color="#E6A23C">
            学生已确认面试
          </el-timeline-item>
          <el-timeline-item v-if="flowRow.status === 4" placement="top" color="#F56C6C">
            {{ flowRow.interviewTime ? '学生拒绝面试邀请' : '企业已拒绝' }}
          </el-timeline-item>
          <el-timeline-item v-if="flowRow.status === 3" placement="top" color="#67C23A">
            面试通过，已录用
          </el-timeline-item>
          <el-timeline-item v-if="flowRow.status === 6" placement="top" color="#F56C6C">
            面试未通过
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Briefcase, Document, Bell } from '@element-plus/icons-vue'

const companyId = parseInt(localStorage.getItem('userId') || '0')
const overview = ref({})
const recentApps = ref([])
const loading = ref(false)
const flowVisible = ref(false)
const flowRow = ref(null)

const STATUS_TEXT = ['待查看', '已查看', '已邀请', '已录用', '已拒绝', '待面试', '面试不通过']
const STATUS_TYPE = ['info', 'warning', 'primary', 'success', 'danger', 'warning', 'danger']
function statusText(s) { return STATUS_TEXT[s] ?? '' }
function statusType(s) { return STATUS_TYPE[s] ?? 'info' }
function fmt(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }

function openFlow(row) {
  flowRow.value = row
  flowVisible.value = true
}

onMounted(async () => {
  if (!companyId) return
  loading.value = true
  try {
    const data = await http.get('/api/statistics/company/overview', { companyId })
    overview.value = data
    recentApps.value = data.recentApplications || []
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 4px; }
.stat-value { font-size: 26px; font-weight: 700; color: #303133; }
</style>
