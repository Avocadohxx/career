<template>
  <div style="padding: 24px; display: flex; flex-direction: column; height: calc(100vh - 120px);">
    <div style="margin-bottom: 16px;">
      <div style="font-size: 18px; font-weight: bold;">AI 求职者推荐</div>
      <div style="color: #909399; font-size: 13px; margin-top: 4px;">AI 将根据您发布的岗位需求，为您推荐最匹配的求职者</div>
    </div>

    <div ref="chatBox" style="flex: 1; overflow-y: auto; padding: 12px 0;">
      <div v-for="(msg, idx) in messages" :key="idx"
           :style="{ display: 'flex', justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start', marginBottom: '16px' }">
        <div :style="{
          maxWidth: '70%', padding: '12px 16px', borderRadius: '8px',
          background: msg.role === 'user' ? '#ecf5ff' : '#ffffff',
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
        }">
          <!-- AI 推荐卡片 -->
          <template v-if="msg.role === 'assistant' && msg.cards && msg.cards.length">
            <div v-for="(card, ci) in msg.cards" :key="ci"
                 style="border: 1px solid #ebeef5; border-radius: 8px; padding: 12px; margin-bottom: 10px;">
              <div style="font-size: 15px; font-weight: bold;">{{ card.student_name }}</div>
              <div style="color: #909399; font-size: 13px; margin: 6px 0;">{{ card.reason }}</div>
              <div style="display: flex; gap: 8px;">
                <el-button size="small" @click="viewResume(card.student_name)">查看简历</el-button>
                <el-button size="small" type="primary" @click="inviteInterview(card.student_name)">发送面试邀请</el-button>
              </div>
            </div>
          </template>
          <!-- 纯文字 -->
          <template v-else>
            <div style="white-space: pre-wrap; line-height: 1.6;">{{ msg.content }}</div>
          </template>
        </div>
      </div>
      <!-- thinking bubble -->
      <div v-if="thinking" style="display: flex; justify-content: flex-start; margin-bottom: 16px;">
        <div style="padding: 12px 20px; border-radius: 8px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
          <span class="dot-animation">AI 正在思考中</span>
        </div>
      </div>
    </div>

    <div style="display: flex; gap: 12px; align-items: flex-end;">
      <el-input v-model="input" type="textarea" :rows="2" resize="none"
                placeholder="继续提问，例如：有没有会 Python 的？" @keydown.enter.ctrl="send" />
      <el-button type="primary" :loading="thinking" :disabled="thinking || !input.trim()" @click="send"
                 style="height: 56px;">发送</el-button>
    </div>

    <!-- 简历弹窗 -->
    <el-dialog v-model="resumeVisible" title="学生简历" width="600px">
      <div v-if="resumeData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="技能标签">{{ resumeData.skills }}</el-descriptions-item>
          <el-descriptions-item label="自我评价">{{ resumeData.selfEval }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无简历信息" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const chatBox = ref(null)
const input = ref('')
const thinking = ref(false)
const messages = ref([])
const chatHistory = ref([])
const companyId = parseInt(localStorage.getItem('userId') || '0')
const allStudents = ref([])
const resumeVisible = ref(false)
const resumeData = ref(null)

const SYSTEM_PROMPT = `你是一个专业的招聘推荐助手。
你会收到一家企业发布的所有岗位信息，以及当前系统中所有学生的技能标签和自我评价。
请根据岗位需求与学生信息的匹配程度，推荐最合适的3~5名求职者。
每名推荐需包含：学生姓名（脱敏）、推荐理由（1~2句话，结合岗位需求与学生技能说明匹配点）。
输出格式为 JSON 数组，不要输出其他内容，格式如下：
[
  {
    "student_name": "张**",
    "reason": "推荐理由"
  }
]`

onMounted(async () => {
  if (!companyId) {
    messages.value.push({ role: 'assistant', content: '无法获取企业信息' })
    return
  }
  try {
    const [jobs, students] = await Promise.all([
      http.get('/api/job/approved-by-company', { companyId }),
      http.get('/api/resume/all')
    ])
    allStudents.value = students || []
    if (!jobs || jobs.length === 0) {
      messages.value.push({ role: 'assistant', content: '您暂无已通过审核的岗位，请先发布岗位' })
      return
    }
    if (!students || students.length === 0) {
      messages.value.push({ role: 'assistant', content: '当前暂无求职者信息' })
      return
    }
    const jobsText = jobs.map(j => `岗位名称：${j.title}；技能要求：${j.skills || '无'}；岗位描述：${j.description || '无'}`).join('\n')
    const studentsText = students.map(s => `姓名（脱敏）：${s.studentName}；技能标签：${s.skills || '无'}；自我评价：${s.selfEval || '无'}`).join('\n')
    const userPrompt = `企业岗位需求：\n${jobsText}\n\n当前求职者列表：\n${studentsText}`

    chatHistory.value = [
      { role: 'system', content: SYSTEM_PROMPT },
      { role: 'user', content: userPrompt }
    ]
    await callAi()
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '数据加载失败：' + e.message })
  }
})

async function callAi() {
  thinking.value = true
  scrollToBottom()
  try {
    const res = await http.post('/api/ai/chat', { messages: chatHistory.value })
    const content = res?.choices?.[0]?.message?.content || ''
    chatHistory.value.push({ role: 'assistant', content })
    const parsed = tryParseCards(content)
    messages.value.push({ role: 'assistant', content, cards: parsed })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，推荐服务暂时不可用：' + e.message })
  } finally {
    thinking.value = false
    scrollToBottom()
  }
}

function tryParseCards(text) {
  try {
    const match = text.match(/\[[\s\S]*\]/)
    if (match) {
      const arr = JSON.parse(match[0])
      if (Array.isArray(arr) && arr.length > 0 && arr[0].student_name) return arr
    }
  } catch {}
  return null
}

async function send() {
  const text = input.value.trim()
  if (!text || thinking.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: text })
  chatHistory.value.push({ role: 'user', content: text })
  await callAi()
}

function viewResume(name) {
  const stu = allStudents.value.find(s => s.studentName === name)
  if (stu) {
    resumeData.value = stu
    resumeVisible.value = true
  } else {
    ElMessage.warning('未找到该求职者信息')
  }
}

function inviteInterview(name) {
  ElMessage.info('请前往应聘管理页面进行面试邀请操作')
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
  })
}
</script>

<style scoped>
.dot-animation::after {
  content: '';
  animation: dots 1.5s steps(3, end) infinite;
}
@keyframes dots {
  0% { content: ''; }
  33% { content: '.'; }
  66% { content: '..'; }
  100% { content: '...'; }
}
</style>
