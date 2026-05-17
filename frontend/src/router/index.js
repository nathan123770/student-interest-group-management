import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import StudentCenter from '../views/StudentCenter.vue'
import MyGroups from '../views/MyGroups.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/my-groups', component: MyGroups, meta: { auth: true } },
  { path: '/student', component: StudentCenter, meta: { auth: true } },
  { path: '/admin', component: Dashboard, meta: { auth: true, roles: ['ADMIN', 'LEADER'] } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.meta.auth && !localStorage.getItem('token')) {
    next('/login')
    return
  }
  if (to.meta.roles) {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    const allowed = to.meta.roles.some(role => roles.includes(role))
    if (!allowed) {
      next('/')
      return
    }
  }
  next()
})

export default router
