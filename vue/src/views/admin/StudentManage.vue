<template>
  <div>
    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px; border-radius: 8px;">
      <el-form :model="query" inline>
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="学生姓名" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="query.department" placeholder="院系" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="query.major" placeholder="专业" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button type="primary" @click="openAdd">+ 新增学生</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="realName" label="姓名" width="90" />
        <el-table-column prop="userName" label="账号" width="110" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="jobIntention" label="求职意向" />
        <el-table-column prop="phone" label="联系方式" width="120" />
        <el-table-column prop="createAt" label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该学生？" @confirm="deleteRow(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 12px; justify-content: flex-end;"
        layout="total, prev, pager, next"
        :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="loadList" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @closed="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号" prop="userName">
              <el-input v-model="form.userName" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="院系">
              <el-input v-model="form.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业">
              <el-input v-model="form.major" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="求职意向">
              <el-input v-model="form.jobIntention" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="意向城市">
              <el-input v-model="form.cityIntention" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const query = reactive({ name: '', department: '', major: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, userName: '', realName: '', department: '', major: '', phone: '', email: '', jobIntention: '', cityIntention: '' })
const rules = {
  userName: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  realName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
}

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/student/list', { page: page.value, size: pageSize, ...query })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function resetQuery() {
  Object.assign(query, { name: '', department: '', major: '' })
  page.value = 1
  loadList()
}

function openAdd() {
  dialogTitle.value = '新增学生'
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑学生'
  Object.assign(form, row)
  dialogVisible.value = true
}

function resetForm() {
  Object.assign(form, { id: null, userName: '', realName: '', department: '', major: '', phone: '', email: '', jobIntention: '', cityIntention: '' })
}

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) await http.put('/api/student/update', form)
    else await http.post('/api/student/add', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function deleteRow(id) {
  try {
    await http.delete('/api/student/' + id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) { ElMessage.error(e.message) }
}

function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
