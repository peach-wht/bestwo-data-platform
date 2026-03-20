import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import MainLayout from '@/layout/MainLayout.vue'

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
        redirect: '/warehouse/debug',
        meta: { title: 'Warehouse' }
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

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${String(to.meta.title)} - Bestwo Data Platform`
  }
})

export default router
