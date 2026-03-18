import { defineStore, createPinia } from 'pinia'
import { computed, ref } from 'vue'

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
  const token = ref(localStorage.getItem('bestwo_token') || '')
  const username = ref(localStorage.getItem('bestwo_username') || '管理员')

  const isLoggedIn = computed(() => Boolean(token.value))

  const setUser = (nextToken: string, nextUsername: string) => {
    token.value = nextToken
    username.value = nextUsername
    localStorage.setItem('bestwo_token', nextToken)
    localStorage.setItem('bestwo_username', nextUsername)
  }

  const clearUser = () => {
    token.value = ''
    username.value = ''
    localStorage.removeItem('bestwo_token')
    localStorage.removeItem('bestwo_username')
  }

  return {
    token,
    username,
    isLoggedIn,
    setUser,
    clearUser
  }
})

export default pinia
