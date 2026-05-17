<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-title">{{ isAdmin ? '系统管理后台' : '负责人工作台' }}</div>
      <el-menu background-color="#0f172a" text-color="#dbeafe" active-text-color="#ffffff" :default-active="active" @select="active = $event">
        <el-menu-item v-for="item in menus" :key="item.key" :index="item.key">{{ item.label }}</el-menu-item>
      </el-menu>
      <el-button style="width:100%;margin-top:18px" @click="$router.push('/')">返回首页</el-button>
      <el-button style="width:100%;margin-top:10px" @click="logout">退出登录</el-button>
    </aside>

    <main class="content">
      <section v-if="active === 'overview'">
        <div class="stat-grid">
          <div class="stat">用户数<strong>{{ overview.userCount || 0 }}</strong></div>
          <div class="stat">小组数<strong>{{ overview.groupCount || 0 }}</strong></div>
          <div class="stat">活动数<strong>{{ overview.activityCount || 0 }}</strong></div>
          <div class="stat">待审申请<strong>{{ overview.pendingApplyCount || 0 }}</strong></div>
        </div>
      </section>

      <section v-if="active === 'groups'">
        <div class="toolbar">
          <h2>{{ isAdmin ? '小组审核与管理' : '我的小组' }}</h2>
          <el-button type="primary" @click="openGroup()">申请创建小组</el-button>
        </div>
        <el-table :data="groupPage.records">
          <el-table-column prop="name" label="小组名称" min-width="180" />
          <el-table-column prop="location" label="地点" />
          <el-table-column label="人数" width="110">
            <template #default="{ row }">{{ row.currentMembers }}/{{ row.maxMembers }}</template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }"><el-tag :type="auditType(row.auditStatus)">{{ auditText(row.auditStatus) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="启用状态" width="110">
            <template #default="{ row }"><el-tag :type="enableType(row.status)">{{ enableText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column width="320">
            <template #default="{ row }">
              <el-button size="small" @click="openGroup(row)">编辑</el-button>
              <el-button v-if="isAdmin && row.auditStatus === 0" size="small" type="success" @click="approveGroup(row.id)">通过</el-button>
              <el-button v-if="isAdmin && row.auditStatus === 0" size="small" type="danger" @click="rejectGroup(row.id)">拒绝</el-button>
              <el-button size="small" @click="openMembers(row)">成员</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleGroup(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="removeGroup(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="active === 'applies'">
        <div class="toolbar">
          <h2>加入申请审核</h2>
          <el-select v-model="applyGroupId" placeholder="选择小组" style="width:240px" @change="loadApplies">
            <el-option v-for="g in groupPage.records" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </div>
        <el-table :data="applyList" empty-text="暂无加入申请">
          <el-table-column prop="userName" label="申请学生" width="140" />
          <el-table-column prop="reason" label="申请理由" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }"><el-tag :type="auditType(row.status)">{{ auditText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column width="180">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" type="success" size="small" @click="review(row.id, 1)">通过</el-button>
              <el-button v-if="row.status === 0" type="danger" size="small" @click="review(row.id, 2)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="active === 'activities'">
        <div class="toolbar">
          <h2>{{ isAdmin ? '全部活动管理' : '我的小组活动' }}</h2>
          <el-button v-if="canCreateActivity" type="primary" @click="openActivity()">发布活动</el-button>
        </div>
        <el-table :data="activityPage.records">
          <el-table-column prop="title" label="活动标题" min-width="180" />
          <el-table-column label="所属小组" min-width="140">
            <template #default="{ row }">{{ groupName(row.groupId) }}</template>
          </el-table-column>
          <el-table-column label="开始时间">
            <template #default="{ row }">{{ formatDate(row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="报名" width="120">
            <template #default="{ row }">{{ row.currentParticipants }}/{{ row.maxParticipants }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="enableType(row.status)">{{ row.status === 1 ? '正常' : '已取消' }}</el-tag></template>
          </el-table-column>
          <el-table-column width="280">
            <template #default="{ row }">
              <el-button size="small" @click="openActivity(row)">编辑</el-button>
              <el-button size="small" type="primary" @click="openSignupReview(row)">报名审核</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleActivity(row)">
                {{ row.status === 1 ? '取消' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="removeActivity(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="active === 'notices'">
        <div class="toolbar">
          <h2>{{ isAdmin ? '系统公告管理' : '小组公告管理' }}</h2>
          <el-button type="primary" @click="openNotice">发布公告</el-button>
        </div>
        <el-table :data="noticeList" empty-text="暂无公告">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="noticeType" label="类型" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="enableType(row.status)">{{ enableText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="发布时间">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column width="230">
            <template #default="{ row }">
              <el-button size="small" @click="openNotice(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleNotice(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="removeNotice(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="active === 'categories'">
        <div class="toolbar"><h2>分类管理</h2><el-button type="primary" @click="openCategory()">新增分类</el-button></div>
        <el-table :data="categoryList">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="description" label="说明" />
          <el-table-column prop="sortOrder" label="排序" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="enableType(row.status)">{{ enableText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column width="230">
            <template #default="{ row }">
              <el-button size="small" @click="openCategory(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleCategory(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="removeCategory(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="active === 'users'">
        <div class="toolbar"><h2>用户管理</h2><el-button @click="loadUsers">刷新</el-button></div>
        <el-table :data="userList">
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="realName" label="姓名" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }"><el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="changeStatus(row)" /></template>
          </el-table-column>
        </el-table>
      </section>
    </main>
  </div>

  <el-dialog v-model="memberDialog" :title="`${currentGroup?.name || ''} 成员管理`" width="680px">
    <el-table :data="memberList" empty-text="暂无成员">
      <el-table-column prop="userName" label="成员" min-width="140" />
      <el-table-column prop="memberRole" label="角色" width="120" />
      <el-table-column label="加入时间">
        <template #default="{ row }">{{ formatDate(row.joinTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '已移除' }}</el-tag></template>
      </el-table-column>
      <el-table-column width="120">
        <template #default="{ row }">
          <el-button v-if="row.memberRole !== 'LEADER' && row.status === 1" size="small" type="danger" @click="kickMember(row)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="signupDialog" :title="`${currentActivity?.title || ''} 报名审核`" width="760px">
    <el-table :data="signupList" empty-text="暂无报名申请">
      <el-table-column prop="userName" label="报名学生" width="140" />
      <el-table-column label="提交时间">
        <template #default="{ row }">{{ formatDate(row.signupTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }"><el-tag :type="auditType(row.status)">{{ auditText(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="reviewRemark" label="审核备注" />
      <el-table-column width="180">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" size="small" @click="reviewSignup(row.id, 1)">通过</el-button>
          <el-button v-if="row.status === 0" type="danger" size="small" @click="reviewSignup(row.id, 2)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="groupDialog" title="兴趣小组" width="560px">
    <el-form :model="groupForm" label-width="90px">
      <el-form-item label="名称"><el-input v-model="groupForm.name" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="groupForm.categoryId" style="width:100%">
          <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="地点"><el-input v-model="groupForm.location" /></el-form-item>
      <el-form-item label="人数上限"><el-input-number v-model="groupForm.maxMembers" :min="1" /></el-form-item>
      <el-form-item label="封面">
        <div class="cover-uploader">
          <img v-if="groupForm.coverUrl" class="cover-preview" :src="groupForm.coverUrl" />
          <el-upload :show-file-list="false" :http-request="uploadGroupCover" accept="image/*">
            <el-button type="primary">上传封面</el-button>
          </el-upload>
          <el-input v-model="groupForm.coverUrl" placeholder="也可以粘贴图片 URL" />
        </div>
      </el-form-item>
      <el-form-item label="介绍"><el-input v-model="groupForm.description" type="textarea" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="groupDialog=false">取消</el-button><el-button type="primary" @click="submitGroup">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="activityDialog" title="活动" width="560px">
    <el-form :model="activityForm" label-width="90px">
      <el-form-item label="小组">
        <el-select v-model="activityForm.groupId" style="width:100%">
          <el-option v-for="g in approvedGroups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题"><el-input v-model="activityForm.title" /></el-form-item>
      <el-form-item label="地点"><el-input v-model="activityForm.location" /></el-form-item>
      <el-form-item label="开始"><el-date-picker v-model="activityForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
      <el-form-item label="结束"><el-date-picker v-model="activityForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
      <el-form-item label="名额"><el-input-number v-model="activityForm.maxParticipants" :min="1" /></el-form-item>
      <el-form-item label="内容"><el-input v-model="activityForm.content" type="textarea" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="activityDialog=false">取消</el-button><el-button type="primary" @click="submitActivity">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="noticeDialog" :title="noticeForm.id ? '编辑公告' : '发布公告'" width="520px">
    <el-form :model="noticeForm" label-width="90px">
      <el-form-item label="标题"><el-input v-model="noticeForm.title" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="noticeForm.noticeType" :disabled="!isAdmin" style="width:100%">
          <el-option v-if="isAdmin" label="系统公告" value="SYSTEM" />
          <el-option label="小组公告" value="GROUP" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="noticeForm.noticeType === 'GROUP'" label="小组">
        <el-select v-model="noticeForm.groupId" style="width:100%">
          <el-option v-for="g in approvedGroups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="置顶"><el-switch v-model="noticeForm.topFlag" :active-value="1" :inactive-value="0" /></el-form-item>
      <el-form-item label="内容"><el-input v-model="noticeForm.content" type="textarea" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="noticeDialog=false">取消</el-button><el-button type="primary" @click="submitNotice">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="categoryDialog" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="460px">
    <el-form :model="categoryForm" label-width="80px">
      <el-form-item label="名称"><el-input v-model="categoryForm.name" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="categoryForm.description" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="categoryForm.sortOrder" :min="0" /></el-form-item>
      <el-form-item label="状态"><el-switch v-model="categoryForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="categoryDialog=false">取消</el-button><el-button type="primary" @click="submitCategory">保存</el-button></template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { activities, activitySignups, allNotices, categories, deleteActivity, deleteCategory, deleteGroup, deleteNotice, groupApplies, groupMembers, groups, managedNotices, removeMember, reviewActivitySignup, reviewApply, reviewGroup, saveActivity, saveCategory, saveGroup, saveNotice, setUserStatus, stats, uploadImage, users } from '../api'
import { formatDate } from '../utils/format'

const router = useRouter()
const roles = JSON.parse(localStorage.getItem('roles') || '[]')
const isAdmin = roles.includes('ADMIN')
const isLeader = roles.includes('LEADER')
const menus = computed(() => isAdmin
  ? [
      { key: 'overview', label: '数据统计' },
      { key: 'groups', label: '小组审核与管理' },
      { key: 'activities', label: '全部活动管理' },
      { key: 'notices', label: '系统公告管理' },
      { key: 'categories', label: '分类管理' },
      { key: 'users', label: '用户管理' }
    ]
  : [
      { key: 'groups', label: '我的小组' },
      { key: 'applies', label: '加入申请审核' },
      { key: 'activities', label: '活动管理' },
      { key: 'notices', label: '小组公告' }
    ])
const active = ref(menus.value[0]?.key || 'groups')
const overview = ref({})
const groupPage = ref({ records: [] })
const activityPage = ref({ records: [] })
const categoryList = ref([])
const userList = ref([])
const noticeList = ref([])
const applyList = ref([])
const memberList = ref([])
const signupList = ref([])
const applyGroupId = ref(null)
const groupDialog = ref(false)
const activityDialog = ref(false)
const noticeDialog = ref(false)
const categoryDialog = ref(false)
const memberDialog = ref(false)
const signupDialog = ref(false)
const currentGroup = ref(null)
const currentActivity = ref(null)
const groupForm = reactive({})
const activityForm = reactive({})
const noticeForm = reactive({ noticeType: isAdmin ? 'SYSTEM' : 'GROUP', topFlag: 0 })
const categoryForm = reactive({ name: '', description: '', sortOrder: 0, status: 1 })

const approvedGroups = computed(() => groupPage.value.records.filter(group => group.auditStatus === 1))
const canCreateActivity = computed(() => isAdmin || approvedGroups.value.length > 0)
const groupName = id => groupPage.value.records.find(group => group.id === id)?.name || '未知小组'
const auditText = status => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[status] || '未知')
const auditType = status => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const enableText = status => status === 1 ? '已启用' : '已禁用'
const enableType = status => status === 1 ? 'success' : 'info'
const loadOverview = async () => { if (isAdmin) overview.value = await stats() }
const loadGroups = async () => {
  groupPage.value = await groups({ page: 1, size: 100 })
  if (!applyGroupId.value && groupPage.value.records.length > 0) applyGroupId.value = groupPage.value.records[0].id
}
const loadActivities = async () => { activityPage.value = await activities({ page: 1, size: 100 }) }
const loadCategories = async () => { categoryList.value = await categories() }
const loadUsers = async () => { if (isAdmin) userList.value = await users() }
const loadNotices = async () => { noticeList.value = isAdmin ? await allNotices() : await managedNotices() }
const loadApplies = async () => {
  if (!applyGroupId.value) {
    applyList.value = []
    return
  }
  applyList.value = await groupApplies(applyGroupId.value)
}
const openGroup = row => {
  Object.assign(groupForm, row || { name: '', categoryId: categoryList.value[0]?.id, maxMembers: 50, location: '', description: '', coverUrl: '' })
  groupDialog.value = true
}
const submitGroup = async () => {
  await saveGroup(groupForm)
  ElMessage.success(isAdmin ? '保存成功' : '建组申请已提交，请等待管理员审核')
  groupDialog.value = false
  await loadGroups()
}
const uploadGroupCover = async ({ file }) => {
  const data = await uploadImage(file)
  groupForm.coverUrl = data.url
  ElMessage.success('封面上传成功')
}
const approveGroup = async id => { await reviewGroup(id, 1); ElMessage.success('审核通过，申请人已成为负责人'); await loadGroups() }
const rejectGroup = async id => { await reviewGroup(id, 2); ElMessage.success('已拒绝'); await loadGroups() }
const toggleGroup = async row => {
  await saveGroup({ ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success(row.status === 1 ? '小组已停用' : '小组已启用')
  await loadGroups()
}
const removeGroup = async row => {
  await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '删除小组', { type: 'warning' })
  await deleteGroup(row.id)
  ElMessage.success('小组已删除')
  await loadGroups()
}
const openMembers = async group => {
  currentGroup.value = group
  memberList.value = await groupMembers(group.id)
  memberDialog.value = true
}
const kickMember = async row => {
  await removeMember(currentGroup.value.id, row.userId)
  ElMessage.success('成员已移除')
  memberList.value = await groupMembers(currentGroup.value.id)
  await loadGroups()
}
const review = async (id, status) => {
  await reviewApply(id, { status, remark: status === 1 ? '审核通过' : '暂不通过' })
  ElMessage.success('处理成功')
  await loadApplies()
  await loadGroups()
}
const openActivity = row => {
  Object.assign(activityForm, row || { groupId: approvedGroups.value[0]?.id, title: '', location: '', maxParticipants: 30, content: '' })
  activityDialog.value = true
}
const submitActivity = async () => { await saveActivity(activityForm); ElMessage.success('保存成功'); activityDialog.value = false; await loadActivities() }
const toggleActivity = async row => {
  await saveActivity({ ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success(row.status === 1 ? '活动已取消' : '活动已启用')
  await loadActivities()
}
const removeActivity = async row => {
  await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除活动', { type: 'warning' })
  await deleteActivity(row.id)
  ElMessage.success('活动已删除')
  await loadActivities()
}
const openSignupReview = async activity => {
  currentActivity.value = activity
  signupList.value = await activitySignups(activity.id)
  signupDialog.value = true
}
const reviewSignup = async (id, status) => {
  await reviewActivitySignup(id, { status, remark: status === 1 ? '报名通过' : '报名未通过' })
  ElMessage.success('报名申请已处理')
  signupList.value = await activitySignups(currentActivity.value.id)
  await loadActivities()
}
const openNotice = row => {
  Object.assign(noticeForm, row || { id: null, title: '', content: '', noticeType: isAdmin ? 'SYSTEM' : 'GROUP', groupId: approvedGroups.value[0]?.id, topFlag: 0, status: 1 })
  noticeDialog.value = true
}
const submitNotice = async () => { await saveNotice(noticeForm); ElMessage.success('保存成功'); noticeDialog.value = false; await loadNotices() }
const toggleNotice = async row => {
  await saveNotice({ ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success(row.status === 1 ? '公告已禁用' : '公告已启用')
  await loadNotices()
}
const removeNotice = async row => {
  await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除公告', { type: 'warning' })
  await deleteNotice(row.id)
  ElMessage.success('公告已删除')
  await loadNotices()
}
const openCategory = row => {
  Object.assign(categoryForm, row || { id: null, name: '', description: '', sortOrder: 0, status: 1 })
  categoryDialog.value = true
}
const submitCategory = async () => { await saveCategory(categoryForm); ElMessage.success('保存成功'); categoryDialog.value = false; await loadCategories() }
const toggleCategory = async row => {
  await saveCategory({ ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success(row.status === 1 ? '分类已禁用' : '分类已启用')
  await loadCategories()
}
const removeCategory = async row => {
  await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '删除分类', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('分类已删除')
  await loadCategories()
}
const changeStatus = async row => { await setUserStatus(row.id, row.status); ElMessage.success('状态已更新') }
const logout = () => { localStorage.clear(); router.push('/login') }

watch(active, async key => {
  if (key === 'overview') await loadOverview()
  if (key === 'groups') await loadGroups()
  if (key === 'activities') await loadActivities()
  if (key === 'notices') await loadNotices()
  if (key === 'categories') await loadCategories()
  if (key === 'users') await loadUsers()
  if (key === 'applies') { await loadGroups(); await loadApplies() }
})

onMounted(async () => {
  if (!isAdmin && !isLeader) {
    router.push('/')
    return
  }
  await loadCategories()
  await loadGroups()
  if (active.value === 'overview') await loadOverview()
  if (active.value === 'activities') await loadActivities()
})
</script>
