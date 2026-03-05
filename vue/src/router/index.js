import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },

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
      { path: 'recommend', name: 'studentRecommend', component: () => import('../views/student/JobRecommend.vue') },
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
      { path: 'talent', name: 'companyTalent', component: () => import('../views/company/TalentRecommend.vue') },
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
  const publicPaths = ['/login', '/register']
  if (!token && !publicPaths.includes(to.path)) {
    next('/login')
  } else if (token && publicPaths.includes(to.path)) {
    next(roleHome())
  } else {
    next()
  }
})

export default router
