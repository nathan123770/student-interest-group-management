<template>
  <div id="top" class="home-page">
    <header class="topbar">
      <div class="brand">学生兴趣小组</div>
      <div class="nav">
        <el-button text @click="scrollToTop">首页</el-button>
        <el-button text @click="scrollToGroups">兴趣小组</el-button>
        <el-button text @click="goMyGroups">我的小组</el-button>
        <el-button text @click="$router.push('/student')">个人中心</el-button>
        <el-button v-if="canManage" text @click="$router.push('/admin')">管理后台</el-button>
        <template v-if="isLogin">
          <div class="user-chip" @click="$router.push('/student')">
            <el-avatar :size="32" :src="currentUser.avatar">{{ avatarText }}</el-avatar>
            <span>{{ displayName }}</span>
          </div>
          <el-button @click="logout">退出</el-button>
        </template>
        <el-button v-else type="primary" @click="$router.push('/login')">登录 / 注册</el-button>
      </div>
    </header>

    <el-carousel class="hero-carousel" height="390px" indicator-position="outside">
      <el-carousel-item v-for="item in heroSlides" :key="item.title">
        <section class="hero-slide" :style="{ background: item.background }">
          <div class="hero-mask">
            <div class="hero-inner">
              <el-tag effect="dark" round>{{ item.tag }}</el-tag>
              <h1>{{ item.title }}</h1>
              <p>{{ item.desc }}</p>
              <div class="hero-actions">
                <el-button type="primary" size="large" @click="scrollToGroups">浏览小组</el-button>
                <el-button size="large" @click="goMyGroups">我的小组</el-button>
              </div>
            </div>
          </div>
        </section>
      </el-carousel-item>
    </el-carousel>

    <main class="section">
      <div id="groups" class="section-anchor section-head">
        <div>
          <el-tag type="primary" effect="plain">发现兴趣</el-tag>
          <h2>兴趣小组</h2>
          <p class="section-desc">按名称或分类查找小组，选择感兴趣的方向申请加入。</p>
        </div>
        <div class="nav searchbar">
          <el-input v-model="query.name" placeholder="搜索小组名称" clearable />
          <el-select v-model="query.categoryId" placeholder="分类" clearable style="width:160px">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-button type="primary" @click="loadGroups">搜索</el-button>
        </div>
      </div>

      <el-empty v-if="groupPage.records.length === 0" description="暂无已开放的小组" />
      <div v-else class="grid">
        <article v-for="g in groupPage.records" :key="g.id" class="club-card">
          <img class="club-cover" :src="g.coverUrl || defaultCover" />
          <div class="club-body">
            <div class="club-title-row">
              <h3>{{ g.name }}</h3>
              <el-tag size="small" effect="plain">{{ categoryName(g.categoryId) }}</el-tag>
            </div>
            <p class="muted">{{ g.location || '地点待定' }} · {{ g.currentMembers }}/{{ g.maxMembers }} 人</p>
            <p class="club-desc">{{ g.description || '负责人还没有填写小组介绍。' }}</p>
            <div class="card-actions">
              <el-button type="primary" @click="apply(g)">申请加入</el-button>
              <el-button @click="openDetail(g)">查看详情</el-button>
            </div>
          </div>
        </article>
      </div>
    </main>

    <el-dialog v-model="detailDialog" title="小组详情" width="680px">
      <div v-if="selectedGroup" class="dialog-detail">
        <img class="dialog-cover" :src="selectedGroup.coverUrl || defaultCover" />
        <div>
          <div class="detail-kicker">
            <el-tag>{{ categoryName(selectedGroup.categoryId) }}</el-tag>
            <span>已开放</span>
          </div>
          <h2>{{ selectedGroup.name }}</h2>
          <p class="muted">{{ selectedGroup.location || '地点待定' }} · {{ selectedGroup.currentMembers }}/{{ selectedGroup.maxMembers }} 人</p>
          <p>{{ selectedGroup.description || '暂无详细介绍' }}</p>
          <div class="detail-meta">
            <span>容量：{{ selectedGroup.maxMembers }} 人</span>
            <span>负责人：{{ selectedGroup.leaderName || '待分配' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
        <el-button v-if="selectedGroup" type="primary" @click="apply(selectedGroup)">申请加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applyJoin, categories, me, publicGroups } from '../api'

const router = useRouter()
const defaultCover = 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f'
const query = reactive({ page: 1, size: 12, name: '', categoryId: null })
const groupPage = ref({ records: [] })
const categoryList = ref([])
const detailDialog = ref(false)
const selectedGroup = ref(null)

const currentUser = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
const isLogin = computed(() => Boolean(localStorage.getItem('token')))
const canManage = computed(() => roles.value.includes('ADMIN') || roles.value.includes('LEADER'))
const displayName = computed(() => currentUser.value.realName || currentUser.value.username || '已登录用户')
const avatarText = computed(() => displayName.value.slice(0, 1))

const heroSlides = computed(() => {
  const groupSlides = groupPage.value.records.slice(0, 3).map(group => ({
    tag: '推荐小组',
    title: group.name,
    desc: group.description || '发现兴趣方向，和同学一起参与校园社团。',
    background: `linear-gradient(120deg, rgba(29,78,216,.74), rgba(15,118,110,.70)), url("${group.coverUrl || defaultCover}") center/cover`
  }))
  return groupSlides.length > 0 ? groupSlides : [
    {
      tag: '校园社团',
      title: '找到热爱的方向，和同学一起把兴趣做成成果',
      desc: '浏览兴趣小组，在线申请加入，让校园生活更有组织感。',
      background: 'linear-gradient(120deg, rgba(29,78,216,.86), rgba(20,184,166,.76)), url("https://images.unsplash.com/photo-1523050854058-8df90110c9f1") center/cover'
    }
  ]
})

const categoryName = id => categoryList.value.find(item => item.id === id)?.name || '未分类'

const refreshSession = async () => {
  if (!isLogin.value) return
  const data = await me()
  currentUser.value = data.user || {}
  roles.value = data.roles || []
  localStorage.setItem('token', data.token)
  localStorage.setItem('user', JSON.stringify(currentUser.value))
  localStorage.setItem('roles', JSON.stringify(roles.value))
}
const loadGroups = async () => {
  groupPage.value = await publicGroups(query)
}
const apply = async group => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const { value } = await ElMessageBox.prompt(`申请加入「${group.name}」`, '加入申请', { inputPlaceholder: '写一句申请理由' })
  await applyJoin({ groupId: group.id, reason: value })
  ElMessage.success('申请已提交')
}
const openDetail = group => {
  selectedGroup.value = group
  detailDialog.value = true
}
const goMyGroups = () => {
  router.push(isLogin.value ? '/my-groups' : '/login')
}
const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })
const scrollToGroups = () => document.querySelector('#groups')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
const logout = () => {
  localStorage.clear()
  location.href = '/login'
}

onMounted(async () => {
  await refreshSession()
  categoryList.value = await categories()
  await loadGroups()
})
</script>
