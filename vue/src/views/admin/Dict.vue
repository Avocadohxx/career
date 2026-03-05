<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px; border-radius: 8px;">
      <el-form inline>
        <el-form-item label="字典类型">
          <el-input v-model="filterType" placeholder="输入类型过滤" clearable style="width: 180px;" @change="loadList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="openAdd">+ 新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="border-radius: 8px;">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="dictType" label="字典类型" width="150" />
        <el-table-column prop="dictKey" label="字典键" width="150" />
        <el-table-column prop="dictValue" label="字典值" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除？" @confirm="deleteRow(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px" @closed="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="form.dictType" placeholder="例如: job_type" />
        </el-form-item>
        <el-form-item label="字典键" prop="dictKey">
          <el-input v-model="form.dictKey" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="form.dictValue" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
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
const loading = ref(false)
const filterType = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增字典')
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, dictType: '', dictKey: '', dictValue: '', description: '', sort: 0 })
const rules = {
  dictType: [{ required: true, message: '必填', trigger: 'blur' }],
  dictKey: [{ required: true, message: '必填', trigger: 'blur' }],
  dictValue: [{ required: true, message: '必填', trigger: 'blur' }],
}

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    list.value = await http.get('/api/dict/list', { dictType: filterType.value || undefined })
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openAdd() { dialogTitle.value = '新增字典'; dialogVisible.value = true }
function openEdit(row) { dialogTitle.value = '编辑字典'; Object.assign(form, row); dialogVisible.value = true }
function resetForm() { Object.assign(form, { id: null, dictType: '', dictKey: '', dictValue: '', description: '', sort: 0 }) }

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) await http.put('/api/dict/update', form)
    else await http.post('/api/dict/add', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function deleteRow(id) {
  try {
    await http.delete('/api/dict/' + id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) { ElMessage.error(e.message) }
}
</script>
