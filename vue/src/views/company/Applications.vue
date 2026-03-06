<template>
  <div>
    <el-tabs v-model="mainTab" @tab-change="onMainTabChange">

      <!-- ===== 投递管理 ===== -->
      <el-tab-pane label="投递管理" name="apply">
        <el-card shadow="never" style="border-radius:8px; margin-top:8px;" v-loading="loading">
          <el-tabs v-if="jobs.length > 0" v-model="activeJob" @tab-change="loadApps">
            <el-tab-pane v-for="job in jobs" :key="job.id" :label="job.title" :name="String(job.id)">
              <el-table :data="appList" v-loading="appLoading" stripe style="margin-top:12px;"
                :row-style="({ row }) => row.status === 4 ? { color:'#c0c4cc' } : {}">
                <el-table-column prop="studentId" label="学生ID" width="80" />
                <el-table-column prop="applyTime" label="投递时间" width="160">
                  <template #default="{ row }">{{ fmt(row.applyTime) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="110">
                  <template #default="{ row }">
                    <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="interviewTime" label="面试时间" width="160">
                  <template #default="{ row }">{{ fmt(row.interviewTime) }}</template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="230">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="viewResume(row.studentId)">查看简历</el-button>
                    <template v-if="row.status <= 1">
                      <el-button type="warning" link @click="openInvite(row)">邀请面试</el-button>
                      <el-button type="danger" link @click="doUpdateStatus(row.id, 4, false)">拒绝</el-button>
                    </template>
                    <span v-else-if="row.status === 2"
                      style="font-size:12px; color:#909399; margin-left:4px;">待学生确认</span>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination style="margin-top:12px; justify-content:flex-end;"
                layout="total, prev, pager, next"
                :total="appTotal" :page-size="10" v-model:current-page="appPage"
                @current-change="loadApps" />
            </el-tab-pane>
          </el-tabs>
          <el-empty v-else description="暂无发布的岗位" />
        </el-card>
      </el-tab-pane>

      <!-- ===== 面试管理 ===== -->
      <el-tab-pane label="面试管理" name="interview">
        <el-card shadow="never" style="border-radius:8px; margin-top:8px;" v-loading="interviewLoading">
          <el-table :data="interviewList" stripe
            :row-style="({ row }) => row.status === 6 ? { color:'#c0c4cc' } : {}">
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="jobTitle" label="应聘岗位" min-width="150" />
            <el-table-column prop="interviewTime" label="面试时间" width="160">
              <template #default="{ row }">{{ fmt(row.interviewTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="220">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewResume(row.studentId)">查看简历</el-button>
                <template v-if="row.status === 5">
                  <el-button type="success" link @click="doUpdateStatus(row.id, 3, true)">通过</el-button>
                  <el-button type="danger" link @click="doUpdateStatus(row.id, 6, true)">不通过</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!interviewLoading && !interviewList.length" description="暂无面试记录" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 简历弹窗 -->
    <el-dialog v-model="resumeVisible" title="查看简历" width="600px">
      <div v-if="resume">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="求职意向">{{ resume.jobIntention }}</el-descriptions-item>
          <el-descriptions-item label="意向城市">{{ resume.cityIntention }}</el-descriptions-item>
          <el-descriptions-item label="技能">{{ resume.skills }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ resume.phone }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:12px;">
          <div style="font-weight:600; margin-bottom:4px;">自我评价</div>
          <div style="color:#606266;">{{ resume.selfEval || '暂无' }}</div>
        </div>
      </div>
      <el-empty v-else description="该学生暂无简历" />
    </el-dialog>

    <!-- 邀请面试弹窗 -->
    <el-dialog v-model="inviteVisible" title="邀请面试" width="400px">
      <el-form label-width="90px">
        <el-form-item label="面试时间">
          <el-date-picker v-model="inviteTime" type="datetime" placeholder="选择面试时间" style="width:100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmInvite">确认邀请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const companyId = parseInt(localStorage.getItem('userId') || '0')
const mainTab = ref('apply')
const loading = ref(false)
const jobs = ref([])
const activeJob = ref('')
const appList = ref([])
const appTotal = ref(0)
const appPage = ref(1)
const appLoading = ref(false)
const interviewList = ref([])
const interviewLoading = ref(false)
const resumeVisible = ref(false)
const resume = ref(null)
const inviteVisible = ref(false)
const inviteTime = ref(null)
const currentAppId = ref(null)
const saving = ref(false)

// 0待查看 1已查看 2已邀请 3已录用 4已拒绝 5待面试 6面试不通过
const STATUS_TEXT = ['待查看', '已查看', '已邀请', '已录用', '已拒绝', '待面试', '面试不通过']
const STATUS_TYPE = ['info', 'warning', 'primary', 'success', 'danger', 'warning', 'danger']
function statusText(s) { return STATUS_TEXT[s] ?? '' }
function statusType(s) { return STATUS_TYPE[s] ?? 'info' }

onMounted(async () => {
  if (!companyId) return
  loading.value = true
  try {
    const res = await http.get('/api/job/list', { companyId, page: 1, size: 100 })
    jobs.value = res.records || []
    if (jobs.value.length > 0) {
      activeJob.value = String(jobs.value[0].id)
      await loadApps()
    }
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
})

function onMainTabChange(tab) {
  if (tab === 'interview') loadInterviews()
}

async function loadApps() {
  const jobId = parseInt(activeJob.value)
  if (!jobId) return
  appLoading.value = true
  try {
    // 投递管理只显示 status 0,1,2,4（不含已进入面试管理的5,3,6）
    const res = await http.get('/api/application/job-list', {
      jobId, page: appPage.value, size: 10, statusIn: '0,1,2,4'
    })
    appList.value = res.records || []
    appTotal.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { appLoading.value = false }
}

async function loadInterviews() {
  interviewLoading.value = true
  try {
    interviewList.value = await http.get('/api/application/interview-list', { companyId }) || []
  } catch (e) { ElMessage.error(e.message) }
  finally { interviewLoading.value = false }
}

async function viewResume(studentId) {
  resumeVisible.value = true
  resume.value = null
  try {
    resume.value = await http.get(`/api/resume/by-student/${studentId}`)
  } catch (e) { ElMessage.error(e.message) }
}

function openInvite(row) {
  currentAppId.value = row.id
  inviteTime.value = null
  inviteVisible.value = true
}

async function confirmInvite() {
  if (!inviteTime.value) { ElMessage.warning('请选择面试时间'); return }
  saving.value = true
  try {
    const t = new Date(inviteTime.value).toISOString().replace('Z', '').slice(0, 19)
    await fetch(`http://localhost:9090/api/application/status/${currentAppId.value}?status=2&interviewTime=${encodeURIComponent(t)}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    ElMessage.success('面试邀请已发送')
    inviteVisible.value = false
    loadApps()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

async function doUpdateStatus(id, status, isInterview) {
  try {
    await fetch(`http://localhost:9090/api/application/status/${id}?status=${status}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    ElMessage.success('操作成功')
    if (isInterview) loadInterviews()
    else loadApps()
  } catch { ElMessage.error('操作失败') }
}

function fmt(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
