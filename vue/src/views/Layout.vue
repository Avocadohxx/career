<template>
  <el-container style="height: 100vh;">

    <!-- 左侧侧边栏 -->
    <el-aside :width="asideWidth + 'px'" :class="['aside', { 'no-transition': isResizing }]">
      <div class="logo">
        <div class="logo-main">
          <img src="@/assets/logo.png" class="logo-icon" />
          <transition name="logo-fade">
            <span v-if="!isCollapsed" class="logo-text">就业指导系统</span>
          </transition>
        </div>
        <el-icon class="collapse-btn" @click="toggleCollapse">
          <Expand v-if="isCollapsed" />
          <Fold v-else />
        </el-icon>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        :collapse="isCollapsed"
        :collapse-transition="false"
        background-color="#4f6395"
        text-color="#fff"
        active-text-color="#ffd04b"
        class="side-menu">

        <!-- 管理员菜单 -->
        <template v-if="role === 'admin'">
          <el-menu-item index="/admin/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-sub-menu index="sub-user">
            <template #title><el-icon><User /></el-icon><span>用户管理</span></template>
            <el-menu-item index="/admin/student"><el-icon><UserFilled /></el-icon>学生管理</el-menu-item>
            <el-menu-item index="/admin/company"><el-icon><OfficeBuilding /></el-icon>企业管理</el-menu-item>
            <el-menu-item index="/admin/counselor"><el-icon><Avatar /></el-icon>辅导员管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="sub-biz">
            <template #title><el-icon><Briefcase /></el-icon><span>业务管理</span></template>
            <el-menu-item index="/admin/job-audit"><el-icon><Document /></el-icon>岗位审核</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/statistics">
            <el-icon><DataLine /></el-icon>
            <template #title>就业统计</template>
          </el-menu-item>
          <el-sub-menu index="sub-sys">
            <template #title><el-icon><Setting /></el-icon><span>系统设置</span></template>
            <el-menu-item index="/admin/dict"><el-icon><List /></el-icon>数据字典</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>
        </template>

        <!-- 辅导员菜单 -->
        <template v-if="role === 'counselor'">
          <el-menu-item index="/counselor/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-sub-menu index="sub-students">
            <template #title><el-icon><User /></el-icon><span>学生管理</span></template>
            <el-menu-item index="/counselor/students"><el-icon><UserFilled /></el-icon>我的学生</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="sub-guidance">
            <template #title><el-icon><Document /></el-icon><span>就业指导</span></template>
            <el-menu-item index="/counselor/posts"><el-icon><ChatDotRound /></el-icon>指导帖子</el-menu-item>
            <el-menu-item index="/counselor/courses"><el-icon><VideoPlay /></el-icon>公开课管理</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/counselor/statistics">
            <el-icon><DataLine /></el-icon>
            <template #title>就业统计</template>
          </el-menu-item>
          <el-menu-item index="/counselor/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>
        </template>

        <!-- 学生菜单 -->
        <template v-if="role === 'student'">
          <el-menu-item index="/student/home">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/student/resume">
            <el-icon><Document /></el-icon>
            <template #title>我的简历</template>
          </el-menu-item>
          <el-menu-item index="/student/recommend">
            <el-icon><Star /></el-icon>
            <template #title>岗位推荐</template>
          </el-menu-item>
          <el-menu-item index="/student/search">
            <el-icon><Search /></el-icon>
            <template #title>岗位搜索</template>
          </el-menu-item>
          <el-menu-item index="/student/applications">
            <el-icon><Tickets /></el-icon>
            <template #title>应聘记录</template>
          </el-menu-item>
          <el-sub-menu index="sub-guidance">
            <template #title><el-icon><Reading /></el-icon><span>就业指导</span></template>
            <el-menu-item index="/student/guidance-posts"><el-icon><ChatDotRound /></el-icon>指导帖子</el-menu-item>
            <el-menu-item index="/student/guidance-courses"><el-icon><VideoPlay /></el-icon>公开课</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/student/forum">
            <el-icon><ChatLineRound /></el-icon>
            <template #title>交流中心</template>
          </el-menu-item>
          <el-menu-item index="/student/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>
        </template>

        <!-- 企业菜单 -->
        <template v-if="role === 'company'">
          <el-menu-item index="/company/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-sub-menu index="sub-jobs">
            <template #title><el-icon><Briefcase /></el-icon><span>岗位管理</span></template>
            <el-menu-item index="/company/jobs"><el-icon><Document /></el-icon>我发布的岗位</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/company/talent">
            <el-icon><UserFilled /></el-icon>
            <template #title>求职者推荐</template>
          </el-menu-item>
          <el-sub-menu index="sub-apps">
            <template #title><el-icon><Tickets /></el-icon><span>应聘管理</span></template>
            <el-menu-item index="/company/applications"><el-icon><List /></el-icon>投递我的简历</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/company/profile">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>企业信息</template>
          </el-menu-item>
        </template>

      </el-menu>

      <!-- 拖拽把手 -->
      <div class="resize-handle" @mousedown="startResize"></div>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container style="flex-direction: column;">
      <el-header class="header">
        <span style="font-weight: 600; color: #4f6395;">就业指导系统</span>
        <div class="header-right">
          <el-icon><Bell /></el-icon>
          <el-icon style="margin-left: 12px;"><UserFilled /></el-icon>
          <span style="margin-left: 6px;">{{ displayName }}</span>
          <el-tag v-if="roleLabel" size="small" style="margin-left: 8px;" :type="roleTagType">{{ roleLabel }}</el-tag>
          <el-button type="primary" link style="margin-left: 16px; color: #f56c6c;" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const role = ref(localStorage.getItem('role') || '')
const displayName = ref(localStorage.getItem('userName') || localStorage.getItem('realName') || '用户')

const roleLabel = computed(() => {
  const map = { admin: '管理员', counselor: '辅导员', student: '学生', company: '企业' }
  return map[role.value] || ''
})
const roleTagType = computed(() => {
  const map = { admin: 'danger', counselor: 'warning', student: '', company: 'success' }
  return map[role.value] || 'info'
})

const asideWidth = ref(250)
const minWidth = 60
const maxWidth = 500
const isResizing = ref(false)
const startX = ref(0)
const startWidth = ref(0)
const isCollapsed = ref(false)
const collapseWidth = 80
const normalWidth = 250
const collapseTrigger = 100
const newWidth = ref(0)
const activeMenu = ref('')

onMounted(() => { activeMenu.value = route.path })
watch(() => route.path, (path) => { activeMenu.value = path })

function startResize(e) {
  isResizing.value = true
  startX.value = e.clientX
  startWidth.value = asideWidth.value
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'
  document.addEventListener('mousemove', doResize)
  document.addEventListener('mouseup', stopResize)
}

function doResize(e) {
  if (!isResizing.value) return
  const dx = e.clientX - startX.value
  newWidth.value = startWidth.value + dx
  if (newWidth.value < collapseTrigger) {
    isCollapsed.value = true
    asideWidth.value = collapseWidth
    return
  }
  isCollapsed.value = false
  if (newWidth.value >= minWidth && newWidth.value <= maxWidth) asideWidth.value = newWidth.value
}

function stopResize() {
  isResizing.value = false
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
  document.removeEventListener('mousemove', doResize)
  document.removeEventListener('mouseup', stopResize)
}

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  const current = newWidth.value === 0 ? normalWidth : newWidth.value
  asideWidth.value = isCollapsed.value ? collapseWidth : current
}

function handleLogout() {
  localStorage.clear()
  router.push('/login')
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', doResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>
