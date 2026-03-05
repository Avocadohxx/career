<template>
  <div style="display: flex; gap: 16px; height: calc(100vh - 130px);">
    <!-- 左侧帖子列表 -->
    <div style="width: 360px; flex-shrink: 0;">
      <el-card shadow="never" style="border-radius: 8px; height: 100%;">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>交流中心</span>
            <el-button size="small" type="primary" @click="openNewPost">发帖</el-button>
          </div>
        </template>
        <div v-loading="listLoading" style="overflow-y: auto; height: calc(100% - 40px);">
          <div v-for="post in posts" :key="post.id"
               :class="['post-item', { active: currentPost?.id === post.id }]"
               @click="loadPost(post)">
            <div style="font-weight: bold; font-size: 14px; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ post.title }}</div>
            <div style="font-size: 12px; color: #999;">
              {{ formatTime(post.createAt) }} · {{ post.replyCount }} 条回复
            </div>
          </div>
          <div v-if="posts.length === 0" style="text-align: center; padding: 20px; color: #999;">暂无帖子</div>
        </div>
      </el-card>
    </div>

    <!-- 右侧帖子详情 -->
    <div style="flex: 1;">
      <el-card v-if="currentPost" shadow="never" style="border-radius: 8px; height: 100%; display: flex; flex-direction: column;">
        <template #header><span style="font-weight: bold;">{{ currentPost.title }}</span></template>
        <div style="flex: 1; overflow-y: auto;">
          <div style="padding: 12px; background: #f8f9fa; border-radius: 6px; margin-bottom: 16px; white-space: pre-wrap;">{{ currentPost.content }}</div>
          <div style="font-size: 13px; color: #999; margin-bottom: 16px; border-bottom: 1px solid #f0f0f0; padding-bottom: 8px;">
            {{ replies.length }} 条回复
          </div>
          <div v-for="reply in replies" :key="reply.id" style="margin-bottom: 12px; padding: 10px; border-radius: 6px; background: #fafafa;">
            <div style="font-size: 12px; color: #666; margin-bottom: 4px;">
              <strong>{{ reply.userName || '用户' }}</strong>
              <span style="margin-left: 8px; color: #aaa;">{{ formatTime(reply.createAt) }}</span>
            </div>
            <div style="font-size: 13px;">{{ reply.content }}</div>
          </div>
        </div>
        <div style="padding-top: 12px; border-top: 1px solid #f0f0f0; display: flex; gap: 8px;">
          <el-input v-model="replyContent" placeholder="写下你的回复..." style="flex: 1;" />
          <el-button type="primary" :loading="replying" @click="submitReply">回复</el-button>
        </div>
      </el-card>
      <el-empty v-else description="选择一个帖子查看详情" style="height: 100%;" />
    </div>

    <el-dialog v-model="newPostVisible" title="发布帖子" width="500px">
      <el-form :model="newPostForm" :rules="postRules" ref="postFormRef" label-width="70px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newPostForm.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="newPostForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="newPostVisible = false">取消</el-button>
        <el-button type="primary" :loading="posting" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage } from 'element-plus'

const posts = ref([])
const listLoading = ref(false)
const currentPost = ref(null)
const replies = ref([])
const replyContent = ref('')
const replying = ref(false)
const newPostVisible = ref(false)
const posting = ref(false)
const postFormRef = ref(null)
const newPostForm = reactive({ title: '', content: '' })
const postRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
}
const studentId = parseInt(localStorage.getItem('userId') || '0')
const userName = localStorage.getItem('realName') || localStorage.getItem('userName') || '学生'

onMounted(loadPosts)

async function loadPosts() {
  listLoading.value = true
  try {
    const res = await http.get('/api/forum/posts', { page: 1, size: 50 })
    posts.value = res.records || []
  } catch (e) { ElMessage.error(e.message) }
  finally { listLoading.value = false }
}

async function loadPost(post) {
  currentPost.value = post
  try {
    replies.value = await http.get('/api/forum/replies/' + post.id)
  } catch (e) { ElMessage.error(e.message) }
}

async function submitReply() {
  if (!replyContent.value.trim()) return
  replying.value = true
  try {
    await http.post('/api/forum/reply/add', {
      postId: currentPost.value.id, userId: studentId,
      userRole: 'student', userName, content: replyContent.value
    })
    replyContent.value = ''
    await loadPost(currentPost.value)
    loadPosts()
  } catch (e) { ElMessage.error(e.message) }
  finally { replying.value = false }
}

function openNewPost() { newPostVisible.value = true }

async function submitPost() {
  const valid = await postFormRef.value.validate().catch(() => false)
  if (!valid) return
  posting.value = true
  try {
    await http.post('/api/forum/post/add', { ...newPostForm, studentId })
    ElMessage.success('发布成功')
    newPostVisible.value = false
    Object.assign(newPostForm, { title: '', content: '' })
    loadPosts()
  } catch (e) { ElMessage.error(e.message) }
  finally { posting.value = false }
}

function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '' }
</script>

<style scoped>
.post-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: background 0.2s;
}
.post-item:hover { background: #f5f7fa; }
.post-item.active { background: #ecf5ff; }
</style>
