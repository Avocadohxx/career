<template>
  <div>
    <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
      <el-button type="primary" @click="openAdd">+ 新增公开课</el-button>
    </div>

    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="课程名称" min-width="150" />
        <el-table-column prop="startTime" label="开课时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="enrolled" label="报名人数" width="100" />
        <el-table-column prop="maxStudents" label="上限" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '进行中' : '已结束' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="info" link @click="viewEnroll(row)">名单</el-button>
            <el-popconfirm title="确认删除？" @confirm="deleteCourse(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="60vw" @closed="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="课程名称" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="课程介绍">
          <el-input v-model="form.description" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="开课时间">
          <el-date-picker v-model="form.startTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="报名上限">
          <el-input-number v-model="form.maxStudents" :min="1" :max="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 报名名单弹窗 -->
    <el-dialog v-model="enrollVisible" :title="`「${currentCourse.title}」报名名单`" width="700px">
      <el-table :data="enrollList" v-loading="enrollLoading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="phone" label="联系方式" width="130" />
        <el-table-column prop="enrollTime" label="报名时间" width="160">
          <template #default="{ row }">{{ formatTime(row.enrollTime) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="!enrollLoading && enrollList.length === 0" style="text-align:center; color:#999; padding:24px;">暂无报名学生</div>
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
const dialogVisible = ref(false)
const dialogTitle = ref('新增公开课')
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, title: '', description: '', startTime: null, maxStudents: 50, counselorId: null, status: 1 })
const rules = { title: [{ required: true, message: '课程名称不能为空', trigger: 'blur' }] }
const counselorId = parseInt(localStorage.getItem('userId') || '0')

const enrollVisible = ref(false)
const enrollLoading = ref(false)
const enrollList = ref([])
const currentCourse = ref({ title: '' })

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/course/list', { page: page.value, size: pageSize, counselorId: counselorId || undefined })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openAdd() {
  dialogTitle.value = '新增公开课'
  form.counselorId = counselorId
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑公开课'
  Object.assign(form, row)
  dialogVisible.value = true
}

function resetForm() { Object.assign(form, { id: null, title: '', description: '', startTime: null, maxStudents: 50, counselorId: counselorId, status: 1 }) }

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) await http.put('/api/course/update', form)
    else await http.post('/api/course/add', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function deleteCourse(id) {
  try {
    await http.delete('/api/course/' + id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) { ElMessage.error(e.message) }
}

async function viewEnroll(row) {
  currentCourse.value = row
  enrollVisible.value = true
  enrollLoading.value = true
  enrollList.value = []
  try {
    enrollList.value = await http.get(`/api/course/enrolled-students/${row.id}`) || []
  } catch (e) { ElMessage.error(e.message) }
  finally { enrollLoading.value = false }
}

function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
