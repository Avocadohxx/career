<template>
  <div>
    <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
      <el-button type="primary" @click="openAdd">+ 发布帖子</el-button>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :span="8" v-for="post in list" :key="post.id" style="margin-bottom: 16px;">
        <el-card shadow="hover" style="border-radius: 8px; height: 180px; display: flex; flex-direction: column; justify-content: space-between;">
          <div>
            <div style="font-weight: bold; font-size: 15px; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ post.title }}</div>
            <div style="font-size: 12px; color: #999; margin-bottom: 8px;">{{ formatTime(post.createAt) }}</div>
            <div style="font-size: 13px; color: #666; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">{{ post.content }}</div>
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px;">
            <el-button size="small" type="primary" link @click="openEdit(post)">编辑</el-button>
            <el-popconfirm title="确认删除？" @confirm="deletePost(post.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination style="margin-top: 12px; justify-content: flex-end;"
      layout="total, prev, pager, next"
      :total="total" :page-size="pageSize" v-model:current-page="page"
      @current-change="loadList" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="60vw" @closed="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="70px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="帖子标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="14" placeholder="帖子内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">发布</el-button>
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
const pageSize = 9
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('发布帖子')
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, title: '', content: '', counselorId: null })
const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
}
const counselorId = parseInt(localStorage.getItem('userId') || '0')

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/guidance/post/list', { page: page.value, size: pageSize, counselorId: counselorId || undefined })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openAdd() {
  dialogTitle.value = '发布帖子'
  form.counselorId = counselorId
  dialogVisible.value = true
}

function openEdit(post) {
  dialogTitle.value = '编辑帖子'
  Object.assign(form, post)
  dialogVisible.value = true
}

function resetForm() { Object.assign(form, { id: null, title: '', content: '', counselorId: counselorId }) }

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) await http.put('/api/guidance/post/update', form)
    else await http.post('/api/guidance/post/add', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function deletePost(id) {
  try {
    await http.delete('/api/guidance/post/' + id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) { ElMessage.error(e.message) }
}

function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
