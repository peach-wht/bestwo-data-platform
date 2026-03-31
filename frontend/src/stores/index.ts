import { defineStore, createPinia } from 'pinia'
import { computed, ref } from 'vue'
import axios from 'axios'

import { fetchCurrentUser, login, logout, type AuthUser } from '@/api/auth'

const pinia = createPinia()

export const useAppStore = defineStore('app', () => {
  const systemTitle = ref('Bestwo Data Platform')
  const menuCollapsed = ref(false)

  const toggleMenu = () => {
    menuCollapsed.value = !menuCollapsed.value
  }

  return {
    systemTitle,
    menuCollapsed,
    toggleMenu
  }
})

export const useUserStore = defineStore('user', () => {
  const profile = ref<AuthUser | null>(null)
  const initialized = ref(false)
  const loadingProfile = ref(false)

  const isLoggedIn = computed(() => Boolean(profile.value))
  const username = computed(() => profile.value?.username || '')
  const roles = computed(() => profile.value?.roles || [])

  const refreshProfile = async () => {
    if (loadingProfile.value) {
      return profile.value
    }

    loadingProfile.value = true
    try {
      const response = await fetchCurrentUser()
      profile.value = response.data
      return profile.value
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        profile.value = null
        return null
      }
      throw error
    } finally {
      initialized.value = true
      loadingProfile.value = false
    }
  }

  const ensureSession = async () => {
    if (initialized.value) {
      return profile.value
    }
    return refreshProfile()
  }

  const loginWithPassword = async (nextUsername: string, nextPassword: string) => {
    const response = await login({
      username: nextUsername,
      password: nextPassword
    })
    profile.value = response.data
    initialized.value = true
    return response.data
  }

  const logoutCurrentUser = async () => {
    try {
      await logout()
    } finally {
      profile.value = null
      initialized.value = true
    }
  }

  return {
    profile,
    username,
    roles,
    isLoggedIn,
    initialized,
    ensureSession,
    refreshProfile,
    loginWithPassword,
    logoutCurrentUser
  }
})

export default pinia
