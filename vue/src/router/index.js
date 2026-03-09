import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '../views/Layout.vue'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
  { path: '/forgot-password', name: 'forgotPassword', component: () => import('../views/ForgotPassword.vue') },

  // 管理员
  {
    path: '/admin',
    component: Layout,
    children: [
      { path: 'dashboard', name: 'adminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'student', name: 'adminStudent', component: () => import('../views/admin/StudentManage.vue') },
      { path: 'company', name: 'adminCompany', component: () => import('../views/admin/CompanyManage.vue') },
      { path: 'counselor', name: 'adminCounselor', component: () => import('../views/admin/CounselorManage.vue') },
      { path: 'job-audit', name: 'adminJobAudit', component: () => import('../views/admin/JobAudit.vue') },
      { path: 'statistics', name: 'adminStatistics', component: () => import('../views/admin/Statistics.vue') },
      { path: 'dict', name: 'adminDict', component: () => import('../views/admin/Dict.vue') },
      { path: 'profile', name: 'adminProfile', component: () => import('../views/common/Profile.vue') },
    ]
  },

  // 辅导员
  {
    path: '/counselor',
    component: Layout,
    children: [
      { path: 'dashboard', name: 'counselorDashboard', component: () => import('../views/counselor/Dashboard.vue') },
      { path: 'students', name: 'counselorStudents', component: () => import('../views/counselor/MyStudents.vue') },
      { path: 'posts', name: 'counselorPosts', component: () => import('../views/counselor/Posts.vue') },
      { path: 'courses', name: 'counselorCourses', component: () => import('../views/counselor/Courses.vue') },
      { path: 'statistics', name: 'counselorStatistics', component: () => import('../views/counselor/Statistics.vue') },
      { path: 'profile', name: 'counselorProfile', component: () => import('../views/common/Profile.vue') },
    ]
  },

  // 学生
  {
    path: '/student',
    component: Layout,
    children: [
      { path: 'home', name: 'studentHome', component: () => import('../views/student/Home.vue') },
      { path: 'resume', name: 'studentResume', component: () => import('../views/student/Resume.vue') },
      { path: 'recommend', redirect: '/student/recommend/smart' },
      { path: 'recommend/smart', name: 'studentRecommendSmart', component: () => import('../views/student/JobRecommend.vue') },
      { path: 'recommend/ai', name: 'studentRecommendAi', component: () => import('../views/student/AiJobRecommend.vue') },
      { path: 'search', name: 'studentSearch', component: () => import('../views/student/JobSearch.vue') },
      { path: 'applications', name: 'studentApplications', component: () => import('../views/student/Applications.vue') },
      { path: 'guidance-posts', name: 'studentPosts', component: () => import('../views/student/GuidancePosts.vue') },
      { path: 'guidance-courses', name: 'studentCourses', component: () => import('../views/student/GuidanceCourses.vue') },
      { path: 'forum', name: 'studentForum', component: () => import('../views/student/Forum.vue') },
      { path: 'profile', name: 'studentProfile', component: () => import('../views/common/Profile.vue') },
    ]
  },

  // 企业
  {
    path: '/company',
    component: Layout,
    children: [
      { path: 'dashboard', name: 'companyDashboard', component: () => import('../views/company/Dashboard.vue') },
      { path: 'jobs', name: 'companyJobs', component: () => import('../views/company/JobManage.vue') },
      { path: 'talent', redirect: '/company/talent/smart' },
      { path: 'talent/smart', name: 'companyTalentSmart', component: () => import('../views/company/TalentRecommend.vue') },
      { path: 'talent/ai', name: 'companyTalentAi', component: () => import('../views/company/AiTalentRecommend.vue') },
      { path: 'applications', name: 'companyApplications', component: () => import('../views/company/Applications.vue') },
      { path: 'profile', name: 'companyProfile', component: () => import('../views/common/Profile.vue') },
    ]
  },

  { path: '/', redirect: () => roleHome() },
  { path: '/:pathMatch(.*)*', redirect: '/login' },
]

function roleHome() {
  const role = localStorage.getItem('role')
  if (role === 'admin') return '/admin/dashboard'
  if (role === 'counselor') return '/counselor/dashboard'
  if (role === 'student') return '/student/home'
  if (role === 'company') return '/company/dashboard'
  return '/login'
}

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const publicPaths = ['/login', '/register', '/forgot-password']
  console.log("status",localStorage.getItem('status'));
  if (!token && !publicPaths.includes(to.path)) {
    next('/login')
  } else if (token && publicPaths.includes(to.path)) {
    next(roleHome())
  } else if (
    localStorage.getItem('role') === 'company' &&
    parseInt(localStorage.getItem('status') || '1') !== 1 &&
    to.path.startsWith('/company/') &&
    to.path !== '/company/dashboard'
  ) {
    ElMessage.warning('您的企业账号正在审核中，暂时无法使用该功能，请耐心等待管理员审核通过。')
    next(false)
  } else {
    next()
  }
})

export default router
