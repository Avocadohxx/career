import Vue from 'vue'
import VueRouter from 'vue-router'
import ManagerLayout from '../views/ManagerLayout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: ManagerLayout,
    children: [
      { path: '', name: 'home', component: () => import('../views/manager/HomeView.vue') },
      { path: 'about', name: 'about', component: () => import('../views/manager/AboutView.vue') },
      { path: 'settings', name: 'settings', component: () => import('../views/manager/SettingsView.vue') },
      { path: 'one', name: 'one', component: () => import('../views/manager/One.vue') },
      { path: 'two', name: 'two', component: () => import('../views/manager/Two.vue') },
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.path === '/') {
    next('/login')
  } else {
    next()
  }
})

export default router
