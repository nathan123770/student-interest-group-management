<template>
  <div class="page-shell">
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark"><el-icon><Collection /></el-icon></span>
        <span>我的小组</span>
      </div>
      <div class="nav">
        <el-button text :icon="HomeFilled" @click="$router.push('/')">首页</el-button>
        <el-button text :icon="User" @click="$router.push('/student')">个人中心</el-button>
        <el-button v-if="canManage" text :icon="Setting" @click="$router.push('/admin')">管理后台</el-button>
        <div class="user-chip">
          <el-avatar :size="32" :src="currentUser.avatar">{{ avatarText }}</el-avatar>
          <span>{{ displayName }}</span>
        </div>
        <el-button :icon="SwitchButton" @click="logout">退出</el-button>
      </div>
    </header>

    <main class="section my-groups-page">
      <section class="my-groups-hero">
        <div>
          <span class="eyebrow"><el-icon><Collection /></el-icon>个人范围</span>
          <h1>我的小组</h1>
          <p class="section-desc">这里集中展示你已经加入或负责的小组，首页只保留公开浏览入口。</p>
        </div>
        <el-button type="primary" :icon="Compass" @click="$router.push('/')">发现更多小组</el-button>
      </section>

      <el-empty v-if="groupList.length === 0" description="你还没有加入任何小组">
        <el-button type="primary" :icon="Plus" @click="$router.push('/')">去申请加入</el-button>
      </el-empty>

      <div v-else class="joined-grid">
        <article v-for="group in groupList" :key="group.id" class="joined-card">
          <img class="joined-cover" :src="group.coverUrl || defaultCover" :alt="`${group.name} 封面`" />
          <div class="joined-body">
            <div class="club-title-row">
              <h3>{{ group.name }}</h3>
              <el-tag :type="group.memberRole === 'LEADER' ? 'warning' : 'success'" size="small">
                {{ roleText(group.memberRole) }}
              </el-tag>
            </div>
            <p class="muted">{{ group.location || '地点待定' }} · {{ group.currentMembers }}/{{ group.maxMembers }} 人</p>
            <p class="club-desc">{{ group.description || '暂无小组介绍' }}</p>
            <p class="muted">加入时间：{{ formatDate(group.joinTime) }}</p>
            <div class="card-actions">
              <el-button :icon="View" @click="openDetail(group)">查看详情</el-button>
              <el-button :icon="Calendar" @click="openActivities(group)">查看活动</el-button>
              <el-button v-if="group.memberRole === 'LEADER'" type="primary" :icon="Setting" @click="$router.push('/admin')">进入管理后台</el-button>
              <el-button v-else type="danger" plain :icon="Close" @click="quit(group)">退出小组</el-button>
            </div>
          </div>
        </article>
      </div>
    </main>

    <el-dialog v-model="detailDialog" title="小组详情" width="680px">
      <div v-if="selectedGroup" class="dialog-detail">
        <img class="dialog-cover" :src="selectedGroup.coverUrl || defaultCover" :alt="`${selectedGroup.name} 封面`" />
        <div>
          <el-tag>{{ roleText(selectedGroup.memberRole) }}</el-tag>
          <h2>{{ selectedGroup.name }}</h2>
          <p class="muted">{{ selectedGroup.location || '地点待定' }} · {{ selectedGroup.currentMembers }}/{{ selectedGroup.maxMembers }} 人</p>
          <p>{{ selectedGroup.description || '暂无详细介绍' }}</p>
          <div class="detail-meta">
            <span>负责人：{{ selectedGroup.leaderName || '待分配' }}</span>
            <span>加入时间：{{ formatDate(selectedGroup.joinTime) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="activityDialog" :title="selectedGroup ? `${selectedGroup.name} · 活动` : '小组活动'" width="760px">
      <el-table :data="activityPage.records" empty-text="暂无活动">
        <el-table-column prop="title" label="活动" min-width="180" />
        <el-table-column prop="location" label="地点" min-width="120" />
        <el-table-column label="开始时间" min-width="150">
          <template #default="{ row }">{{ formatDate(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="名额" width="100">
          <template #default="{ row }">{{ row.currentParticipants }}/{{ row.maxParticipants }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Close, Collection, Compass, HomeFilled, Plus, Setting, SwitchButton, User, View } from '@element-plus/icons-vue'
import { joinedGroups, me, publicActivities, quitGroup } from '../api'
import { formatDate } from '../utils/format'

const defaultCover = 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f'
const currentUser = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
const groupList = ref([])
const selectedGroup = ref(null)
const detailDialog = ref(false)
const activityDialog = ref(false)
const activityPage = ref({ records: [] })

const canManage = computed(() => roles.value.includes('ADMIN') || roles.value.includes('LEADER'))
const displayName = computed(() => currentUser.value.realName || currentUser.value.username || '已登录用户')
const avatarText = computed(() => displayName.value.slice(0, 1))
const roleText = role => role === 'LEADER' ? '负责人' : '成员'

const refreshSession = async () => {
  const data = await me()
  currentUser.value = data.user || {}
  roles.value = data.roles || []
  localStorage.setItem('token', data.token)
  localStorage.setItem('user', JSON.stringify(currentUser.value))
  localStorage.setItem('roles', JSON.stringify(roles.value))
}
const loadGroups = async () => {
  groupList.value = await joinedGroups()
}
const openDetail = group => {
  selectedGroup.value = group
  detailDialog.value = true
}
const openActivities = async group => {
  selectedGroup.value = group
  activityPage.value = await publicActivities({ page: 1, size: 20, groupId: group.id })
  activityDialog.value = true
}
const quit = async group => {
  await ElMessageBox.confirm(`确定退出「${group.name}」吗？`, '退出小组', { type: 'warning' })
  await quitGroup(group.id)
  ElMessage.success('已退出小组')
  await loadGroups()
}
const logout = () => {
  localStorage.clear()
  location.href = '/login'
}

onMounted(async () => {
  await refreshSession()
  await loadGroups()
})
</script>
