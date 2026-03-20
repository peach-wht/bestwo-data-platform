<script setup lang="ts">
import { Monitor, Box, Connection, DataLine, Fold, Expand, SwitchButton } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

import { useAppStore, useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.clearUser()
  router.push('/login')
}
</script>

<template>
  <el-container class="main-layout">
    <el-aside :width="appStore.menuCollapsed ? '72px' : '220px'" class="sidebar">
      <div class="brand">
        <span class="brand-mark">BW</span>
        <span v-if="!appStore.menuCollapsed" class="brand-text">Bestwo Admin</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="appStore.menuCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>Dashboard</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Box /></el-icon>
          <span>Orders</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><Connection /></el-icon>
          <span>Tasks</span>
        </el-menu-item>
        <el-menu-item index="/warehouse/debug">
          <el-icon><DataLine /></el-icon>
          <span>Warehouse</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-button text @click="appStore.toggleMenu()">
            <el-icon>
              <Fold v-if="!appStore.menuCollapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <div>
            <h1 class="header-title">{{ appStore.systemTitle }}</h1>
            <p class="header-subtitle">WeChat order ingestion and warehouse analytics platform</p>
          </div>
        </div>

        <div class="header-right">
          <span class="header-user">{{ userStore.username || 'Guest' }}</span>
          <el-button link type="primary" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            Logout
          </el-button>
        </div>
      </el-header>

      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
