<template>
    <div>
        <el-container style="height: 100vh;">
        
        <!-- 左侧顶部标识 -->
        <el-aside :width="asideWidth + 'px'" :class="['aside', { 'no-transition': isResizing }]">
            <div class="logo">
            <div class="logo-main">
                <img src="@/assets/logo.png" class="logo-icon" />
                <transition name="logo-fade">
                <span v-if="!isCollapsed" class="logo-text">就业指导系统</span>
                </transition>
            </div>
            <i 
                :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'" 
                class="collapse-btn"
                @click="toggleCollapse">
            </i>
            </div>

            <!-- 左侧侧边栏 -->
            <el-menu
            :default-active="$route.path"
            router
            :collapse="isCollapsed"
            :collapse-transition="false"
            background-color="#4f6395"
            text-color="#fff"
            active-text-color="#ffd04b"
            class="side-menu">
            <el-menu-item index="/">
                <i class="el-icon-s-home"></i>
                <transition name="logo-fade"><span v-if="!isCollapsed">首页</span></transition>
            </el-menu-item>
            <el-submenu index="/about">
                <template slot="title">
                <i class="el-icon-s-order"></i>
                <transition name="logo-fade"><span v-if="!isCollapsed">导航一</span></transition>
                </template>
                <el-menu-item index="/about">关于</el-menu-item>
            </el-submenu>
            <el-menu-item index="/settings">
                <i class="el-icon-setting"></i>
                <transition name="logo-fade"><span v-if="!isCollapsed">设置</span></transition>
            </el-menu-item>

            </el-menu>

            <!-- 拖拽把手 -->
            <div class="resize-handle" @mousedown="startResize"></div>
        </el-aside>

        <!-- 右侧区域 -->
        <el-container style="flex-direction: column;">
            <el-header class="header">
            <span>系统标题</span>
            <div class="header-right">
                <i class="el-icon-bell"></i>
                <span>管理员</span>
            </div>
            </el-header>

            <el-main class="main">
              <router-view></router-view>
            </el-main>
        </el-container>

        </el-container>
    </div>
</template>


<script>
export default {
  data() {
    return {
      asideWidth: 250,       // 当前侧边栏宽度
      minWidth: 60,          // 最小宽度
      maxWidth: 500,         // 最大宽度
      isResizing: false,     // 是否正在拖拽
      startX: 0,             // 鼠标按下时的X坐标
      startWidth: 0,         // 开始拖拽时的宽度
      isCollapsed: false,
      collapseWidth: 80,   // 折叠后宽度
      normalWidth: 250,    // 展开宽度
      collapseTrigger: 100, // 拖到这个宽度以下自动折叠
      newWidth: 0,
    }
  },

  methods: {
    // 开始拖拽
    startResize(e) {
      this.isResizing = true
      this.startX = e.clientX
      this.startWidth = this.asideWidth

      // 拖拽时禁止文字选中
      document.body.style.userSelect = 'none'
      document.body.style.cursor = 'col-resize'

      document.addEventListener('mousemove', this.doResize)
      document.addEventListener('mouseup', this.stopResize)
    },

    
    // 拖拽中
    doResize(e) {
      if (!this.isResizing) return
      const dx = e.clientX - this.startX
      this.newWidth = this.startWidth + dx

      // 限制最小最大宽度
      if (this.newWidth >= this.minWidth && this.newWidth <= this.maxWidth) {
        this.asideWidth = this.newWidth
      }

      // 拖到阈值以下自动折叠
      if (this.newWidth < this.collapseTrigger) {
        this.isCollapsed = true
        this.asideWidth = this.collapseWidth
        return
      }

      this.isCollapsed = false
      if (this.newWidth >= this.minWidth && this.newWidth <= this.maxWidth) {
        this.asideWidth = this.newWidth
      }
    },

    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed
      const correntWidth = this.newWidth == 0 ? this.normalWidth : this.newWidth
      this.asideWidth = this.isCollapsed ? this.collapseWidth : correntWidth
    },

    // 停止拖拽
    stopResize() {
      this.isResizing = false
      document.body.style.userSelect = ''
      document.body.style.cursor = ''
      document.removeEventListener('mousemove', this.doResize)
      document.removeEventListener('mouseup', this.stopResize)
    }
  },

  // 组件销毁时清除事件监听，防止内存泄漏
  beforeDestroy() {
    document.removeEventListener('mousemove', this.doResize)
    document.removeEventListener('mouseup', this.stopResize)
  }
}
</script>