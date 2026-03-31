import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import MainLayout from '@/layout/MainLayout.vue'
import pinia, { useUserStore } from '@/stores'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/system/LoginView.vue'),
    meta: { title: 'Login' }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: 'Dashboard' }
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/order/OrderListView.vue'),
        meta: { title: 'Orders' }
      },
      {
        path: 'tasks',
        name: 'tasks',
        component: () => import('@/views/task/TaskListView.vue'),
        meta: { title: 'Tasks' }
      },
      {
        path: 'warehouse',
        name: 'warehouse',
        redirect: '/warehouse/analysis',
        meta: { title: 'Warehouse' }
      },
      {
        path: 'warehouse/analysis',
        name: 'WarehouseOrderAnalysis',
        component: () => import('@/views/warehouse/OrderAnalysisView.vue'),
        meta: { title: 'Order Analysis' }
      },
      {
        path: 'warehouse/debug',
        name: 'WarehouseDebug',
        component: () => import('@/views/warehouse/WarehouseDebugView.vue'),
        meta: { title: 'Warehouse Debug' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/system/NotFoundView.vue'),
    meta: { title: 'Not Found' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore(pinia)
  const requiresAuth = to.matched.some((record) => Boolean(record.meta?.requiresAuth))

  if (to.path === '/login') {
    await userStore.ensureSession()
    if (userStore.isLoggedIn) {
      return { path: '/dashboard' }
    }
    return true
  }

  if (!requiresAuth) {
    return true
  }

  await userStore.ensureSession()
  if (userStore.isLoggedIn) {
    return true
  }

  return {
    path: '/login',
    query: {
      redirect: to.fullPath
    }
  }
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${String(to.meta.title)} - Bestwo Data Platform`
  }
})

export default router
