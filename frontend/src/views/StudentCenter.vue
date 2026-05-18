<template>
  <header class="topbar">
    <div class="brand">
      <span class="brand-mark"><el-icon><User /></el-icon></span>
      <span>个人中心</span>
    </div>
    <div class="nav">
      <el-button text :icon="HomeFilled" @click="$router.push('/')">返回首页</el-button>
      <el-button v-if="canManage" text :icon="Setting" @click="$router.push('/admin')">管理后台</el-button>
      <div class="user-chip">
        <el-avatar :size="32" :src="user.avatar">{{ avatarText }}</el-avatar>
        <span>{{ displayName }}</span>
      </div>
      <el-button :icon="SwitchButton" @click="logout">退出登录</el-button>
    </div>
  </header>

  <main class="section profile-page">
    <section class="profile-hero">
      <div class="profile-main">
        <el-avatar :size="72" :src="user.avatar">{{ avatarText }}</el-avatar>
        <div>
          <span class="eyebrow"><el-icon><User /></el-icon>学生工作台</span>
          <h1>{{ displayName }}</h1>
          <p>{{ roleNames }} · {{ user.studentNo || '暂无学号' }}</p>
        </div>
      </div>
      <div class="profile-meta">
        <span>用户名：{{ user.username || '-' }}</span>
        <span>电话：{{ user.phone || '-' }}</span>
        <span>邮箱：{{ user.email || '-' }}</span>
        <el-button type="primary" plain :icon="Collection" @click="$router.push('/my-groups')">去我的小组</el-button>
      </div>
    </section>

    <section class="profile-overview">
      <article class="overview-card">
        <div class="overview-title">
          <span>我的身份</span>
          <el-tag :type="canManage ? 'success' : 'primary'">{{ primaryRole }}</el-tag>
        </div>
        <p>{{ identityDesc }}</p>
        <div class="overview-badges">
          <el-tag effect="plain">{{ canManage ? '可进入管理后台' : '学生端权限' }}</el-tag>
          <el-tag effect="plain" type="warning">可申请创建小组</el-tag>
        </div>
      </article>

      <article class="overview-card">
        <div class="overview-title">
          <span>待办提醒</span>
          <el-tag v-if="todoItems.length" type="warning">{{ todoItems.length }} 项</el-tag>
        </div>
        <div v-if="todoItems.length" class="todo-list">
          <div v-for="item in todoItems" :key="item.label" class="todo-item">
            <strong>{{ item.count }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
        <p v-else class="muted">暂无待处理事项，可以去发现新的兴趣小组。</p>
      </article>

      <article class="overview-card">
        <div class="overview-title">
          <span>快捷操作</span>
        </div>
        <div class="quick-actions">
          <el-button type="primary" :icon="Collection" @click="$router.push('/my-groups')">我的小组</el-button>
          <el-button :icon="Plus" @click="openCreateGroup">申请创建小组</el-button>
          <el-button :icon="Bell" @click="activeTab = 'notices'">查看公告</el-button>
          <el-button :icon="HomeFilled" @click="$router.push('/')">返回首页</el-button>
        </div>
      </article>
    </section>

    <section class="recent-feed">
      <div class="toolbar">
        <h2>最近动态</h2>
        <span class="muted">公告和申请状态会优先展示在这里</span>
      </div>
      <div v-if="recentItems.length" class="recent-list">
        <div v-for="item in recentItems" :key="`${item.type}-${item.id}`" class="recent-item">
          <el-tag size="small" :type="item.tagType">{{ item.typeText }}</el-tag>
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else :image-size="80" description="暂无最近动态" />
    </section>

    <section class="profile-content">
      <div class="toolbar">
        <h2>我的校园兴趣档案</h2>
        <el-button type="primary" :icon="Plus" @click="openCreateGroup">申请创建小组</el-button>
      </div>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="加入申请流程" name="applies">
          <div class="flow-list" v-if="applyRows.length">
            <article v-for="item in applyRows" :key="item.id" class="flow-card">
              <div class="flow-head">
                <strong>{{ item.groupName }}</strong>
                <el-tag :type="statusType(item.status)">{{ statusText(item.status) }}</el-tag>
              </div>
              <el-steps :active="stepActive(item.status)" finish-status="success" align-center>
                <el-step title="已提交" />
                <el-step title="待审核" />
                <el-step :title="item.status === 2 ? '已拒绝' : '已通过'" :status="item.status === 2 ? 'error' : undefined" />
              </el-steps>
              <p class="muted">申请理由：{{ item.reason || '-' }}</p>
              <p class="muted">审核备注：{{ item.reviewRemark || '-' }}</p>
            </article>
          </div>
          <el-empty v-else description="暂无加入申请" />
        </el-tab-pane>

        <el-tab-pane label="活动报名申请" name="signups">
          <div class="flow-list" v-if="signupRows.length">
            <article v-for="item in signupRows" :key="item.id" class="flow-card">
              <div class="flow-head">
                <strong>{{ item.activityName }}</strong>
                <el-tag :type="statusType(item.status)">{{ statusText(item.status) }}</el-tag>
              </div>
              <el-steps :active="stepActive(item.status)" finish-status="success" align-center>
                <el-step title="已提交" />
                <el-step title="待审核" />
                <el-step :title="item.status === 2 ? '已拒绝' : '已通过'" :status="item.status === 2 ? 'error' : undefined" />
              </el-steps>
              <p class="muted">提交时间：{{ formatDate(item.signupTime) }}</p>
              <p class="muted">审核备注：{{ item.reviewRemark || '-' }}</p>
            </article>
          </div>
          <el-empty v-else description="暂无活动报名申请" />
        </el-tab-pane>

        <el-tab-pane label="我的建组申请" name="groups">
          <el-table :data="myGroupPage.records" empty-text="暂无建组申请">
            <el-table-column prop="name" label="小组名称" min-width="180" />
            <el-table-column prop="location" label="活动地点" />
            <el-table-column label="人数上限" width="110">
              <template #default="{ row }">{{ row.maxMembers }} 人</template>
            </el-table-column>
            <el-table-column label="审核状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusType(row.auditStatus)">{{ statusText(row.auditStatus) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="公告通知" name="notices">
          <el-empty v-if="noticeList.length === 0" description="暂无公告" />
          <el-timeline v-else>
            <el-timeline-item v-for="n in noticeList" :key="n.id" :timestamp="formatDate(n.createTime)">
              <div class="notice-item">
                <strong>{{ n.title }}</strong>
                <el-tag size="small" :type="n.noticeType === 'SYSTEM' ? 'primary' : 'success'">
                  {{ n.noticeType === 'SYSTEM' ? '系统公告' : '小组公告' }}
                </el-tag>
                <p>{{ n.content }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </section>
  </main>

  <el-dialog v-model="groupDialog" title="申请创建兴趣小组" width="560px">
    <el-form :model="groupForm" label-width="96px">
      <el-form-item label="小组名称"><el-input v-model="groupForm.name" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="groupForm.categoryId" style="width:100%">
          <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="活动地点"><el-input v-model="groupForm.location" /></el-form-item>
      <el-form-item label="人数上限"><el-input-number v-model="groupForm.maxMembers" :min="1" /></el-form-item>
      <el-form-item label="封面">
        <div class="cover-uploader">
          <img v-if="groupForm.coverUrl" class="cover-preview" :src="groupForm.coverUrl" alt="小组封面预览" />
          <el-upload :show-file-list="false" :http-request="uploadGroupCover" accept="image/*">
            <el-button type="primary">上传封面</el-button>
          </el-upload>
          <el-input v-model="groupForm.coverUrl" placeholder="也可以粘贴图片 URL" />
        </div>
      </el-form-item>
      <el-form-item label="小组介绍"><el-input v-model="groupForm.description" type="textarea" :rows="4" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="groupDialog=false">取消</el-button>
      <el-button type="primary" @click="submitGroup">提交审核</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Collection, HomeFilled, Plus, Setting, SwitchButton, User } from '@element-plus/icons-vue'
import { categories, groups, me, myActivitySignups, myApplies, notices, publicActivities, publicGroups, saveGroup, uploadImage } from '../api'
import { formatDate } from '../utils/format'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
const applyList = ref([])
const signupList = ref([])
const noticeList = ref([])
const categoryList = ref([])
const publicGroupPage = ref({ records: [] })
const publicActivityPage = ref({ records: [] })
const myGroupPage = ref({ records: [] })
const activeTab = ref('applies')
const groupDialog = ref(false)
const groupForm = reactive({ name: '', categoryId: null, location: '', maxMembers: 30, coverUrl: '', description: '' })

const displayName = computed(() => user.value.realName || user.value.username || '当前用户')
const avatarText = computed(() => displayName.value.slice(0, 1))
const canManage = computed(() => roles.value.includes('ADMIN') || roles.value.includes('LEADER'))
const roleNames = computed(() => {
  const map = { ADMIN: '系统管理员', LEADER: '小组负责人', STUDENT: '学生' }
  return roles.value.map(role => map[role] || role).join(' / ') || '学生'
})
const primaryRole = computed(() => {
  if (roles.value.includes('ADMIN')) return '系统管理员'
  if (roles.value.includes('LEADER')) return '小组负责人'
  return '学生'
})
const identityDesc = computed(() => {
  if (roles.value.includes('ADMIN')) return '你可以进入管理后台处理系统级小组、用户、分类和公告。'
  if (roles.value.includes('LEADER')) return '你可以管理自己负责的小组，也可以继续以学生身份参与活动。'
  return '你可以浏览兴趣小组、提交加入申请、报名活动，也可以申请创建新小组。'
})
const pendingApplyCount = computed(() => applyList.value.filter(item => item.status === 0).length)
const pendingSignupCount = computed(() => signupList.value.filter(item => item.status === 0).length)
const pendingGroupCount = computed(() => myGroupPage.value.records.filter(item => item.auditStatus === 0).length)
const todoItems = computed(() => [
  { label: '个加入申请待审核', count: pendingApplyCount.value },
  { label: '个活动报名待审核', count: pendingSignupCount.value },
  { label: '个建组申请待审核', count: pendingGroupCount.value }
].filter(item => item.count > 0))
const groupNameMap = computed(() => {
  const map = {}
  publicGroupPage.value.records.forEach(group => { map[group.id] = group.name })
  myGroupPage.value.records.forEach(group => { map[group.id] = group.name })
  return map
})
const activityNameMap = computed(() => {
  const map = {}
  publicActivityPage.value.records.forEach(activity => { map[activity.id] = activity.title })
  return map
})
const applyRows = computed(() => applyList.value.map(item => ({ ...item, groupName: item.groupName || groupNameMap.value[item.groupId] || '未知小组' })))
const signupRows = computed(() => signupList.value.map(item => ({ ...item, activityName: item.activityTitle || activityNameMap.value[item.activityId] || '未知活动' })))
const recentItems = computed(() => {
  const noticesFeed = noticeList.value.slice(0, 3).map(item => ({
    id: item.id,
    type: 'notice',
    typeText: item.noticeType === 'SYSTEM' ? '系统公告' : '小组公告',
    tagType: item.noticeType === 'SYSTEM' ? 'primary' : 'success',
    title: item.title,
    desc: item.content || formatDate(item.createTime)
  }))
  const applyFeed = applyRows.value.slice(0, 3).map(item => ({
    id: item.id,
    type: 'apply',
    typeText: '加入申请',
    tagType: statusType(item.status),
    title: item.groupName,
    desc: `状态：${statusText(item.status)}${item.reviewRemark ? `，${item.reviewRemark}` : ''}`
  }))
  return [...noticesFeed, ...applyFeed].slice(0, 3)
})
const statusText = status => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[status] || '未知')
const statusType = status => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const stepActive = status => status === 0 ? 2 : 3

const openCreateGroup = () => {
  Object.assign(groupForm, { name: '', categoryId: categoryList.value[0]?.id, location: '', maxMembers: 30, coverUrl: '', description: '' })
  groupDialog.value = true
}
const submitGroup = async () => {
  await saveGroup(groupForm)
  ElMessage.success('建组申请已提交，请等待管理员审核')
  groupDialog.value = false
  await loadMyGroups()
}
const uploadGroupCover = async ({ file }) => {
  const data = await uploadImage(file)
  groupForm.coverUrl = data.url
  ElMessage.success('封面上传成功')
}
const loadMyGroups = async () => { myGroupPage.value = await groups({ page: 1, size: 50 }) }
const refreshSession = async () => {
  const data = await me()
  user.value = data.user || {}
  roles.value = data.roles || []
  localStorage.setItem('token', data.token)
  localStorage.setItem('user', JSON.stringify(user.value))
  localStorage.setItem('roles', JSON.stringify(roles.value))
}
const logout = () => {
  localStorage.clear()
  router.push('/login')
}

onMounted(async () => {
  await refreshSession()
  categoryList.value = await categories()
  publicGroupPage.value = await publicGroups({ page: 1, size: 200 })
  publicActivityPage.value = await publicActivities({ page: 1, size: 200 })
  await loadMyGroups()
  applyList.value = await myApplies()
  signupList.value = await myActivitySignups()
  noticeList.value = await notices()
})
</script>
