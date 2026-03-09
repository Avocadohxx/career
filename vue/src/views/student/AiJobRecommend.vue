<template>
  <div style="padding: 24px; display: flex; flex-direction: column; height: calc(100vh - 120px);">
    <div style="margin-bottom: 16px;">
      <div style="font-size: 18px; font-weight: bold;">AI 岗位推荐</div>
      <div style="color: #909399; font-size: 13px; margin-top: 4px;">AI 已读取您的简历，将为您推荐最匹配的岗位</div>
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
              <div style="font-size: 15px; font-weight: bold;">{{ card.job_title }}</div>
              <div style="color: #909399; font-size: 13px; margin: 6px 0;">{{ card.reason }}</div>
              <el-button size="small" type="primary" @click="applyByTitle(card.job_title)">立即投递</el-button>
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
                placeholder="继续提问，例如：有没有上海的岗位？" @keydown.enter.ctrl="send" />
      <el-button type="primary" :loading="thinking" :disabled="thinking || !input.trim()" @click="send"
                 style="height: 56px;">发送</el-button>
    </div>
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
const studentId = parseInt(localStorage.getItem('userId') || '0')
const allJobs = ref([])

const SYSTEM_PROMPT = `你是一个专业的就业推荐助手。
你会收到一名学生的技能标签和自我评价，以及当前系统中所有可投递岗位的信息。
请根据学生信息与岗位信息的匹配程度，推荐最合适的3~5个岗位。
每个岗位推荐需包含：岗位名称、推荐理由（1~2句话，结合学生技能与岗位要求说明匹配点）。
输出格式为 JSON 数组，不要输出其他内容，格式如下：
[
  {
    "job_title": "岗位名称",
    "reason": "推荐理由"
  }
]`

onMounted(async () => {
  if (!studentId) {
    messages.value.push({ role: 'assistant', content: '您的简历信息不完整，请先完善简历后再使用 AI 推荐' })
    return
  }
  try {
    const [resume, jobs] = await Promise.all([
      http.get('/api/resume/my', { studentId }),
      http.get('/api/job/approved')
    ])
    allJobs.value = jobs || []
    if (!resume || (!resume.skills && !resume.selfEval)) {
      messages.value.push({ role: 'assistant', content: '您的简历信息不完整，请先完善简历后再使用 AI 推荐' })
      return
    }
    if (!jobs || jobs.length === 0) {
      messages.value.push({ role: 'assistant', content: '当前暂无可投递岗位，请稍后再试' })
      return
    }
    const jobsText = jobs.map(j => `岗位名称：${j.title}；技能要求：${j.skills || '无'}；岗位描述：${j.description || '无'}`).join('\n')
    const userPrompt = `学生技能标签：${resume.skills || '无'}\n学生自我评价：${resume.selfEval || '无'}\n\n当前可投递岗位列表：\n${jobsText}`

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
      if (Array.isArray(arr) && arr.length > 0 && arr[0].job_title) return arr
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

async function applyByTitle(title) {
  const job = allJobs.value.find(j => j.title === title)
  if (!job) { ElMessage.warning('未找到该岗位'); return }
  try {
    await http.post('/api/application/apply', { studentId, jobId: job.id, companyId: job.companyId })
    ElMessage.success('投递成功')
  } catch (e) { ElMessage.error(e.message) }
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
