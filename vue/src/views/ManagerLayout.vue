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

        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="sub-about">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>导航一</span>
          </template>
          <el-menu-item index="/one">
            <el-icon><InfoFilled /></el-icon>
            导航1-1
          </el-menu-item>
          <el-menu-item index="/about">
            <el-icon><InfoFilled /></el-icon>
            关于
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="sub-settings">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>导航二</span>
          </template>
          <el-menu-item index="/two">
            <el-icon><InfoFilled /></el-icon>
            导航2-1
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>设置</template>
        </el-menu-item>

      </el-menu>

      <!-- 拖拽把手 -->
      <div class="resize-handle" @mousedown="startResize"></div>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container style="flex-direction: column;">
      <el-header class="header">
        <span>就业指导系统</span>
        <div class="header-right">
          <el-icon><Bell /></el-icon>
          <el-icon style="margin-left: 12px;"><UserFilled /></el-icon>
          <span style="margin-left: 6px;">{{ displayName }}</span>
          <el-button type="primary" link style="margin-left: 16px; color: #fff;" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

  </el-container>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

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
const displayName = ref(localStorage.getItem('userName') || '管理员')

onMounted(() => {
  activeMenu.value = route.path
})

watch(() => route.path, (path) => {
  activeMenu.value = path
})

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
  if (newWidth.value >= minWidth && newWidth.value <= maxWidth) {
    asideWidth.value = newWidth.value
  }
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
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('realName')
  localStorage.removeItem('userName')
  router.push('/login')
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', doResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>
