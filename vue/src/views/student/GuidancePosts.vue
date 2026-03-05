<template>
  <div>
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="8" v-for="post in list" :key="post.id" style="margin-bottom: 16px;">
        <el-card shadow="hover" style="border-radius: 8px; height: 200px; display: flex; flex-direction: column; justify-content: space-between;">
          <div>
            <div style="font-weight: bold; font-size: 15px; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ post.title }}</div>
            <div style="font-size: 12px; color: #999; margin-bottom: 8px;">发布时间：{{ formatTime(post.createAt) }}</div>
            <div style="font-size: 13px; color: #666; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;">{{ post.content }}</div>
          </div>
          <el-button size="small" type="primary" link @click="openDetail(post)" style="align-self: flex-end;">查看全文</el-button>
        </el-card>
      </el-col>
    </el-row>
    <div v-if="!loading && list.length === 0" style="text-align: center; padding: 40px; color: #999;">暂无帖子</div>

    <el-pagination style="margin-top: 12px; justify-content: flex-end;"
      layout="total, prev, pager, next"
      :total="total" :page-size="pageSize" v-model:current-page="page"
      @current-change="loadList" />

    <el-dialog v-model="detailVisible" :title="currentPost.title" width="700px">
      <div style="font-size: 13px; color: #999; margin-bottom: 16px;">{{ formatTime(currentPost.createAt) }}</div>
      <div style="white-space: pre-wrap; line-height: 1.8; font-size: 14px;">{{ currentPost.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 9
const loading = ref(false)
const detailVisible = ref(false)
const currentPost = ref({})

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/guidance/post/list', { page: page.value, size: pageSize })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

function openDetail(post) { currentPost.value = post; detailVisible.value = true }
function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>
