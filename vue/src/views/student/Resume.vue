<template>
  <div>
    <el-card shadow="never" style="border-radius: 8px; max-width: 760px; margin: 0 auto;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 16px; font-weight: bold;">我的简历</span>
          <el-button type="primary" :loading="saving" @click="save">保存简历</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="90px" label-position="right">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="求职意向">
              <el-input v-model="form.jobIntention" placeholder="例如：Java后端开发" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="意向城市">
              <el-input v-model="form.cityIntention" placeholder="例如：北京、上海" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高学历">
              <el-select v-model="form.education" placeholder="请选择" style="width: 100%;">
                <el-option label="专科" value="专科" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">技能标签</el-divider>
        <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px;">
          <el-tag v-for="skill in skills" :key="skill" closable @close="removeSkill(skill)">{{ skill }}</el-tag>
          <el-input v-if="addingSkill" v-model="newSkill" size="small" style="width: 100px;"
                    @keyup.enter="confirmSkill" @blur="confirmSkill" ref="skillInput" />
          <el-button v-else size="small" @click="startAddSkill">+ 添加技能</el-button>
        </div>

        <el-divider content-position="left">教育背景</el-divider>
        <el-input v-model="form.eduHistory" type="textarea" :rows="3"
                  placeholder="请描述您的教育经历，例如：2020-2024 XX大学 计算机科学与技术 本科" />

        <el-divider content-position="left">工作/实习经历</el-divider>
        <el-input v-model="form.workHistory" type="textarea" :rows="4"
                  placeholder="请描述您的工作或实习经历" />

        <el-divider content-position="left">自我评价</el-divider>
        <el-input v-model="form.selfEval" type="textarea" :rows="4"
                  placeholder="请简要介绍自己的优势和特点..." />
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const saving = ref(false)
const addingSkill = ref(false)
const newSkill = ref('')
const skillInput = ref(null)
const skills = ref([])
const studentId = parseInt(localStorage.getItem('userId') || '0')

const form = reactive({
  studentId: studentId,
  jobIntention: '',
  cityIntention: '',
  education: '',
  skills: '',
  eduHistory: '',
  workHistory: '',
  selfEval: '',
})

onMounted(async () => {
  if (!studentId) return
  try {
    const data = await http.get('/api/resume/my', { studentId })
    if (data) {
      Object.assign(form, data)
      skills.value = (data.skills || '').split(',').filter(Boolean)
    }
  } catch (e) { /* 无简历则新建 */ }
})

function startAddSkill() {
  addingSkill.value = true
  nextTick(() => skillInput.value?.focus())
}

function confirmSkill() {
  if (newSkill.value.trim()) skills.value.push(newSkill.value.trim())
  newSkill.value = ''
  addingSkill.value = false
}

function removeSkill(skill) {
  skills.value = skills.value.filter(s => s !== skill)
}

async function save() {
  form.skills = skills.value.join(',')
  saving.value = true
  try {
    await http.post('/api/resume/save', form)
    ElMessage.success('简历保存成功')
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}
</script>
