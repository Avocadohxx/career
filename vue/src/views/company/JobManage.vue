<template>
  <div>
    <el-card shadow="never" style="border-radius:8px;">
      <div style="display:flex; justify-content:flex-end; margin-bottom:16px;">
        <el-button type="primary" @click="openDialog()">发布岗位</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="岗位名称" min-width="120" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column label="薪资范围" width="120">
          <template #default="{ row }">{{ row.salaryMin }}-{{ row.salaryMax }}K</template>
        </el-table-column>
        <el-table-column prop="jobType" label="类型" width="80" />
        <el-table-column prop="education" label="学历要求" width="90" />
        <el-table-column prop="createAt" label="发布时间" width="160">
          <template #default="{ row }">{{ fmt(row.createAt) }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:12px; justify-content:flex-end;"
        layout="total, prev, pager, next"
        :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="loadList" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑岗位' : '发布岗位'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作城市" prop="city">
              <el-input v-model="form.city" placeholder="如：北京" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="薪资下限" prop="salaryMin">
              <el-input-number v-model="form.salaryMin" :min="0" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资上限" prop="salaryMax">
              <el-input-number v-model="form.salaryMax" :min="0" style="width:100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学历要求" prop="education">
              <el-select v-model="form.education" style="width:100%;">
                <el-option label="不限" value="不限" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位类型" prop="jobType">
              <el-radio-group v-model="form.jobType">
                <el-radio value="全职">全职</el-radio>
                <el-radio value="实习">实习</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="技能要求">
          <el-input v-model="form.skills" placeholder="如：Java,Spring Boot,MySQL（逗号分隔）" />
        </el-form-item>
        <el-form-item label="岗位描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请详细描述岗位职责与要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const companyId = parseInt(localStorage.getItem('userId') || '0')
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({ id: null, title: '', city: '', salaryMin: 5, salaryMax: 15, education: '不限', jobType: '全职', skills: '', description: '' })
const rules = {
  title: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  description: [{ required: true, message: '请输入岗位描述', trigger: 'blur' }],
}

onMounted(loadList)

async function loadList() {
  if (!companyId) return
  loading.value = true
  try {
    const res = await http.get('/api/job/list', { companyId, page: page.value, size: pageSize })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openDialog(row = null) {
  Object.assign(form, { id: null, title: '', city: '', salaryMin: 5, salaryMax: 15, education: '不限', jobType: '全职', skills: '', description: '' })
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) {
      await http.put('/api/job/update', { ...form })
    } else {
      await http.post('/api/job/add', { ...form, companyId })
    }
    ElMessage.success('保存成功，等待审核')
    dialogVisible.value = false
    loadList()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该岗位？', '提示', { type: 'warning' }).catch(() => { throw 0 })
  try {
    await http.delete(`/api/job/${id}`)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) { if (e) ElMessage.error(e.message) }
}

function statusType(s) { return [null, 'success', 'danger'][s] ?? 'warning' }
function statusText(s) { return ['待审核', '已通过', '已拒绝'][s] ?? '未知' }
function fmt(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
