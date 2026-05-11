<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- 待审核申请列表 -->
      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pendingList" style="width: 100%" v-loading="pendingLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物" width="150">
            <template #default="{ row }">
              {{ row.pet.name }}
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="150">
            <template #default="{ row }">
              {{ row.adopter.username }}
            </template>
          </el-table-column>
          <el-table-column label="居住类型" width="100">
            <template #default="{ row }">{{ row.residenceType || '-' }}</template>
          </el-table-column>
          <el-table-column label="住房面积" width="100">
            <template #default="{ row }">{{ row.housingArea ? row.housingArea + '㎡' : '-' }}</template>
          </el-table-column>
          <el-table-column label="养宠经验" width="120">
            <template #default="{ row }">{{ row.petExperience || '-' }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="领养理由" />
          <el-table-column prop="contact" label="联系方式" width="150" />
          <el-table-column prop="applyTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
              <el-button type="success" size="small" @click="review(row.id, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              :pager-count="7"
              v-model:current-page="pendingPage"
              v-model:page-size="pendingPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="pendingTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePendingSizeChange"
              @current-change="handlePendingPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

      <!-- 已通过申请列表 -->
      <el-tab-pane label="已通过" name="approved">
        <el-table :data="approvedList" style="width: 100%" v-loading="approvedLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物" width="150">
            <template #default="{ row }">
              {{ row.pet.name }}
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="150">
            <template #default="{ row }">
              {{ row.adopter.username }}
            </template>
          </el-table-column>
          <el-table-column label="联系方式" width="150">
            <template #default="{ row }">{{ row.contact || '-' }}</template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="reviewTime" label="审核时间" width="180">
            <template #default="{ row }">
              {{ row.reviewTime ? formatTime(row.reviewTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="回访" width="240">
            <template #default="{ row }">
              <div style="display: flex; gap: 8px">
                <el-button
                    type="primary"
                    size="small"
                    @click="showVisitDialog(row)"
                    style="background: #E07A5F; border-color: #E07A5F"
                >
                  添加回访
                </el-button>
                <el-button
                    type="info"
                    size="small"
                    @click="viewVisitHistory(row)"
                    style="background: linear-gradient(135deg, #81B29A 0%, #F2CC8F 100%); border: none; color: white"
                >
                  回访历史
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="approvedPage"
              v-model:page-size="approvedPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="approvedTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleApprovedSizeChange"
              @current-change="handleApprovedPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

      <!-- 已拒绝申请列表 -->
      <el-tab-pane label="已拒绝" name="rejected">
        <el-table :data="rejectedList" style="width: 100%" v-loading="rejectedLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物" width="150">
            <template #default="{ row }">
              {{ row.pet.name }}
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="150">
            <template #default="{ row }">
              {{ row.adopter.username }}
            </template>
          </el-table-column>
          <el-table-column label="联系方式" width="150">
            <template #default="{ row }">{{ row.contact || '-' }}</template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="reviewTime" label="审核时间" width="180">
            <template #default="{ row }">
              {{ row.reviewTime ? formatTime(row.reviewTime) : '-' }}
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="rejectedPage"
              v-model:page-size="rejectedPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="rejectedTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRejectedSizeChange"
              @current-change="handleRejectedPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="⏰ 待回访" name="pending-visits">
        <el-alert
            title="以下领养申请已通过但尚未进行首次回访，请及时跟进"
            type="warning"
            :closable="false"            style="margin-bottom: 20px"
        />
        <el-table :data="pendingVisitsList" style="width: 100%" v-loading="pendingVisitsLoading">
          <el-table-column prop="id" label="申请ID" width="100" />
          <el-table-column label="宠物" width="150">
            <template #default="{ row }">
              {{ row.pet?.name || '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="领养人" width="150">
            <template #default="{ row }">
              {{ row.adopter?.username || '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="联系方式" width="150">
            <template #default="{ row }">{{ row.contact || '-' }}</template>
          </el-table-column>
          <el-table-column label="领养通过时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.reviewTime) }}
            </template>
          </el-table-column>
          <el-table-column label="已过去天数" width="120">
            <template #default="{ row }">
              <el-tag :type="getDaysSinceApproval(row.reviewTime) > 7 ? 'danger' : 'warning'">
                {{ getDaysSinceApproval(row.reviewTime) }} 天
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button
                  type="primary"
                  size="small"
                  @click="showVisitDialog(row)"                  style="background: #E07A5F; border-color: #E07A5F"
              >
                首次回访
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="pendingVisitsList.length === 0" description="所有领养申请都已回访" :image-size="150" />
      </el-tab-pane>

      <!--- 所有回访列表 -->
      <el-tab-pane label="📋 回访记录" name="all-visits">
        <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center">
          <el-alert
              title="所有回访记录总览，包括管理员回访和领养人反馈"
              type="info"
              :closable="false"              style="flex: 1; margin-right: 20px"
          />
          <el-button
              type="primary"
              @click="loadAllVisits"
              :icon="Refresh"
              :loading="allVisitsLoading"
          >
            刷新
          </el-button>
        </div>

        <el-table :data="allVisitsList" style="width: 100%" v-loading="allVisitsLoading" max-height="600">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="宠物" width="150">
            <template #default="{ row }">
              {{ row.application?.pet?.name || '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="领养人" width="120">
            <template #default="{ row }">
              {{ row.application?.adopter?.username || '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="回访类型" width="130">
            <template #default="{ row }">
              <el-tag
                  :type="getVisitTypeTag(row.visitType)"
                  size="small"
                  :effect="row.visitType === 'USER_FEEDBACK' ? 'dark' : 'light'"
              >
                {{ getVisitTypeText(row.visitType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="宠物状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getPetStatusTag(row.petStatus)" size="small">
                {{ getPetStatusText(row.petStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回访人" width="120">
            <template #default="{ row }">
              {{ row.visitor?.username || '系统' }}
            </template>
          </el-table-column>
          <el-table-column label="回访时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.visitTime) }}
            </template>
          </el-table-column>
          <el-table-column label="需要跟进" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.needFollowUp" type="danger" size="small">需要</el-tag>
              <el-tag v-else type="info" size="small">无需</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewVisitRecordDetail(row)">详情</el-button>
              <el-button v-if="row.visitType !== 'USER_FEEDBACK'" type="danger" size="small" @click="deleteVisitRecord(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="allVisitsList.length === 0 && !allVisitsLoading" description="暂无回访记录" :image-size="150" />
      </el-tab-pane>

    </el-tabs>

    <!-- 申请详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="领养申请详细信息" width="800px">
      <div v-if="currentApplication" class="application-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <h4 style="margin-bottom: 15px; color: #666"> 申请人信息</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">{{ currentApplication.adopter.username }}</el-descriptions-item>
              <el-descriptions-item label="联系方式">{{ currentApplication.contact }}</el-descriptions-item>
              <el-descriptions-item label="居住类型">{{ currentApplication.residenceType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="住房面积">{{ currentApplication.housingArea ? currentApplication.housingArea + '㎡' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="养宠经验">{{ currentApplication.petExperience || '-' }}</el-descriptions-item>
              <el-descriptions-item label="是否有其他宠物">{{ currentApplication.hasOtherPets ? '是' : '否' }}</el-descriptions-item>
              <el-descriptions-item v-if="currentApplication.otherPetsInfo" label="其他宠物信息">{{ currentApplication.otherPetsInfo }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <h4 style="margin-bottom: 15px; color: #666"> 宠物信息</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="宠物名称">{{ currentApplication.pet.name }}</el-descriptions-item>
              <el-descriptions-item label="宠物种类">{{ currentApplication.pet.type }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ currentApplication.pet.gender || '未知' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ currentApplication.pet.age }}岁</el-descriptions-item>
              <el-descriptions-item label="健康状态">
                <el-tag v-if="currentApplication.pet.vaccinated" type="success" size="small">已疫苗</el-tag>
                <el-tag v-else type="info" size="small">未疫苗</el-tag>
                <el-tag v-if="currentApplication.pet.neutered" type="success" size="small" style="margin-left: 8px">已绝育</el-tag>
                <el-tag v-else type="info" size="small" style="margin-left: 8px">未绝育</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="发布者">{{ currentApplication.pet.rescuer?.username }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <div style="margin-top: 20px">
          <h4 style="margin-bottom: 10px; color: #666"> 领养理由</h4>
          <div style="padding: 15px; background-color: #f9f9f9; border-radius: 8px; line-height: 1.8">
            {{ currentApplication.reason }}
          </div>
        </div>

        <div style="margin-top: 15px">
          <h4 style="margin-bottom: 10px; color: #666"> 家庭情况</h4>
          <div style="padding: 15px; background-color: #f9f9f9; border-radius: 8px; line-height: 1.8">
            {{ currentApplication.familySituation || '未填写' }}
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝领养申请" width="500px">
      <el-form label-width="100px">
        <el-form-item label="申请人">
          <span>{{ currentApplication?.adopter?.username }}</span>
        </el-form-item>
        <el-form-item label="申请宠物">
          <span>{{ currentApplication?.pet?.name }}</span>
        </el-form-item>
        <el-form-item label="拒绝理由" required>
          <el-input
              v-model="rejectReason"
              type="textarea"
              :rows="4"
              placeholder="请填写拒绝理由，该理由将展示给用户..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :disabled="!rejectReason.trim()">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 添加回访记录弹窗 -->
    <el-dialog v-model="visitDialogVisible" :title="`添加回访记录 - ${currentApplication?.pet?.name || ''}`" width="650px">
      <el-form :model="visitForm" label-width="120px" class="visit-form">
        <el-form-item label="回访方式" required>
          <el-radio-group v-model="visitForm.visitType">
            <el-radio label="PHONE"> 电话回访</el-radio>
            <el-radio label="ON_SITE"> 上门回访</el-radio>
            <el-radio label="ONLINE"> 在线回访</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="回访时间" required>
          <el-date-picker
              v-model="visitForm.visitTime"
              type="datetime"
              placeholder="选择回访时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="宠物状态" required>
          <el-radio-group v-model="visitForm.petStatus">
            <el-radio label="HEALTHY">✅ 适应良好</el-radio>
            <el-radio label="ADAPTING">️ 适应中</el-radio>
            <el-radio label="ISSUES"> 存在问题</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="回访内容">
          <el-input
              v-model="visitForm.content"
              type="textarea"
              rows="4"
              placeholder="记录回访的详细情况..."
          />
        </el-form-item>

        <el-form-item label="领养者反馈">
          <el-input
              v-model="visitForm.feedback"
              type="textarea"
              rows="3"
              placeholder="领养者的意见或建议..."
          />
        </el-form-item>

        <el-form-item label="后续回访">
          <el-switch v-model="visitForm.needFollowUp" />
          <span style="margin-left: 10px; color: #666">{{ visitForm.needFollowUp ? '需要后续回访' : '无需后续回访' }}</span>
        </el-form-item>

        <el-form-item v-if="visitForm.needFollowUp" label="下次回访时间">
          <el-date-picker
              v-model="visitForm.nextVisitTime"
              type="date"
              placeholder="选择下次回访日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="visitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitVisitRecord" :loading="submitting" style="background: #E07A5F; border-color: #E07A5F">
          提交回访记录
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看回访历史弹窗 -->
    <el-dialog v-model="historyDialogVisible" title="回访历史" width="800px">
      <div v-if="visitHistory.length === 0" style="text-align: center; padding: 40px; color: #999">
        暂无回访记录
      </div>
      <el-timeline v-else>
        <el-timeline-item
            v-for="record in visitHistory"
            :key="record.id"
            :timestamp="formatTime(record.visitTime)"
            placement="top"
        >
          <el-card shadow="hover" :class="['visit-record-card', record.visitType === 'USER_FEEDBACK' ? 'user-feedback-card' : '']">
            <div class="visit-record-header">
              <el-tag :type="getVisitTypeTag(record.visitType)" size="small">
                {{ getVisitTypeText(record.visitType) }}
              </el-tag>

              <el-tag :type="getPetStatusTag(record.petStatus)" size="small" style="margin-left: 10px">
                {{ getPetStatusText(record.petStatus) }}
              </el-tag>
              <span style="margin-left: auto; color: #999; font-size: 12px">
                回访人：{{ record.visitor?.username || '管理员' }}
              </span>
            </div>

            <!-- 用户反馈特殊提示 -->
            <div v-if="record.visitType === 'USER_FEEDBACK'" class="user-feedback-notice">
              <span> 此反馈由领养人主动提交</span>
              <el-tag v-if="record.needFollowUp" type="danger" size="small" style="margin-left: 10px">
                需要帮助
              </el-tag>
            </div>

            <div v-if="record.content" style="margin-top: 12px">
              <div style="color: #666; font-size: 13px; margin-bottom: 5px">📝 回访内容：</div>
              <div style="color: #333; line-height: 1.6">{{ record.content }}</div>
            </div>
            <div v-if="record.feedback" style="margin-top: 12px">
              <div style="color: #666; font-size: 13px; margin-bottom: 5px">💬 领养者反馈：</div>
              <div style="color: #333; line-height: 1.6">{{ record.feedback }}</div>
            </div>
            <div v-if="record.needFollowUp && record.nextVisitTime" style="margin-top: 12px; padding-top: 12px; border-top: 1px dashed #ddd">
              <span style="color: #E07A5F; font-size: 13px">
                📅 下次回访：{{ formatDate(record.nextVisitTime) }}
              </span>
            </div>
            <div v-if="record.visitType !== 'USER_FEEDBACK'" style="margin-top: 12px; text-align: right">
              <el-button type="danger" size="small" text @click="deleteVisitRecord(record.id)">删除</el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { get, post, del } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'

const activeTab = ref('pending')
const pendingList = ref([])
const approvedList = ref([])
const rejectedList = ref([])
const pendingVisitsList = ref([])
const allVisitsList = ref([])
const currentUser = ref(null)

const pendingLoading = ref(false)
const approvedLoading = ref(false)
const rejectedLoading = ref(false)
const pendingVisitsLoading = ref(false)
const allVisitsLoading = ref(false)

const pendingPage = ref(1)
const pendingPageSize = ref(10)
const pendingTotal = ref(0)

const approvedPage = ref(1)
const approvedPageSize = ref(10)
const approvedTotal = ref(0)

const rejectedPage = ref(1)
const rejectedPageSize = ref(10)
const rejectedTotal = ref(0)

const visitDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const currentApplication = ref(null)
const visitHistory = ref([])
const submitting = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectingApplication = ref(null)

const visitForm = ref({
  visitType: 'PHONE',
  visitTime: '',
  petStatus: 'HEALTHY',
  content: '',
  feedback: '',
  needFollowUp: false,
  nextVisitTime: ''
})

/**
 * 计算距离领养通过的天数
 */
const getDaysSinceApproval = (reviewTime) => {
  if (!reviewTime) return 0
  const reviewDate = new Date(reviewTime)
  const now = new Date()
  const diffTime = Math.abs(now - reviewDate)
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

/**
 * 加载所有回访记录
 */
const loadAllVisits = async () => {
  allVisitsLoading.value = true
  try {
    const data = await get('/api/visit/all-records', {
      username: currentUser.value.username
    })
    allVisitsList.value = data
  } catch (err) {
    console.error('加载所有回访记录失败:', err)
  } finally {
    allVisitsLoading.value = false
  }
}

/**
 * 查看回访记录详情
 */
const viewVisitRecordDetail = (record) => {
  currentApplication.value = record.application
  visitHistory.value = [record]
  historyDialogVisible.value = true
}

/**
 * 加载待回访列表
 */
const loadPendingVisits = async () => {
  pendingVisitsLoading.value = true
  try {
    const data = await get('/api/visit/pending-visits', {
      username: currentUser.value.username
    })
    pendingVisitsList.value = data
  } catch (err) {
    console.error('加载待回访列表失败:', err)
  } finally {
    pendingVisitsLoading.value = false
  }
}

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try { return JSON.parse(userStr) } catch { return null }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const formatDate = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN')
}

const getVisitTypeText = (type) => {
  const map = {
    'USER_FEEDBACK': '用户反馈',
    'PHONE': ' 电话回访',
    'ON_SITE': ' 上门回访',
    'ONLINE': ' 在线回访',
    'PLANNED': ' 计划回访'
  }
  return map[type] || type
}

const getVisitTypeTag = (type) => {
  const map = {
    'USER_FEEDBACK': 'primary',
    'PHONE': 'success',
    'ON_SITE': 'warning',
    'ONLINE': 'info',
    'PLANNED': ''
  }
  return map[type] || ''
}

const getPetStatusText = (status) => {
  const map = {
    'HEALTHY': '✅ 适应良好',
    'ADAPTING': '️ 适应中',
    'ISSUES': ' 存在问题'
  }
  return map[status] || status
}

const getPetStatusTag = (status) => {
  const map = {
    'HEALTHY': 'success',
    'ADAPTING': 'warning',
    'ISSUES': 'danger'
  }
  return map[status] || ''
}

const loadPending = async () => {
  pendingLoading.value = true
  try {
    const data = await get('/api/adoption/pending', {
      username: currentUser.value.username,
      page: pendingPage.value - 1,
      size: pendingPageSize.value
    })
    pendingList.value = data.content
    pendingTotal.value = data.totalElements
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    pendingLoading.value = false
  }
}

const loadApproved = async () => {
  approvedLoading.value = true
  try {
    const data = await get('/api/adoption/approved', {
      username: currentUser.value.username,
      page: approvedPage.value - 1,
      size: approvedPageSize.value
    })
    approvedList.value = data.content
    approvedTotal.value = data.totalElements
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    approvedLoading.value = false
  }
}

const loadRejected = async () => {
  rejectedLoading.value = true
  try {
    const data = await get('/api/adoption/rejected', {
      username: currentUser.value.username,
      page: rejectedPage.value - 1,
      size: rejectedPageSize.value
    })
    rejectedList.value = data.content
    rejectedTotal.value = data.totalElements
  } catch (err) {
    error(err.message || '加载失败')
  } finally {
    rejectedLoading.value = false
  }
}

const handlePendingPageChange = (page) => {
  pendingPage.value = page
  loadPending()
}

const handlePendingSizeChange = (size) => {
  pendingPageSize.value = size
  pendingPage.value = 1
  loadPending()
}

const handleApprovedPageChange = (page) => {
  approvedPage.value = page
  loadApproved()
}

const handleApprovedSizeChange = (size) => {
  approvedPageSize.value = size
  approvedPage.value = 1
  loadApproved()
}

const handleRejectedPageChange = (page) => {
  rejectedPage.value = page
  loadRejected()
}

const handleRejectedSizeChange = (size) => {
  rejectedPageSize.value = size
  rejectedPage.value = 1
  loadRejected()
}

const viewDetail = (application) => {
  currentApplication.value = application
  detailDialogVisible.value = true
}

const review = (applicationId, action) => {
  const text = action === 'approve' ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${text}这条申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const url = `/api/adoption/review/${applicationId}?username=${currentUser.value.username}&action=${action}`
      await post(url, {})
      success(`${text}成功`)
      loadPending()
      loadApproved()
      loadRejected()
    } catch (err) {
      error(err.message || '操作失败')
    }
  }).catch(() => {})
}

const handleReject = (application) => {
  rejectingApplication.value = application
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    warning('请填写拒绝理由')
    return
  }
  try {
    const url = `/api/adoption/review/${rejectingApplication.value.id}?username=${currentUser.value.username}&action=reject&reason=${encodeURIComponent(rejectReason.value)}`
    await post(url, {})
    success('已拒绝该申请')
    rejectDialogVisible.value = false
    loadPending()
    loadApproved()
    loadRejected()
  } catch (err) {
    error(err.message || '操作失败')
  }
}

const showVisitDialog = async (application) => {
  currentApplication.value = application
  visitForm.value = {
    visitType: 'PHONE',
    visitTime: new Date().toISOString().slice(0, 19),
    petStatus: 'HEALTHY',
    content: '',
    feedback: '',
    needFollowUp: false,
    nextVisitTime: ''
  }
  try {
    const history = await get(`/api/visit/list/${application.id}`)
    if (history && history.length > 0) {
      const lastVisit = history[0]
      if (lastVisit.nextVisitTime) {
        visitForm.value.nextVisitTime = lastVisit.nextVisitTime
      }
    }
  } catch (err) {
    console.error('加载回访历史失败', err)
  }
  visitDialogVisible.value = true
}

const submitVisitRecord = async () => {
  if (!visitForm.value.visitTime || !visitForm.value.petStatus) {
    warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    await post('/api/visit/add', {
      username: currentUser.value.username,
      applicationId: currentApplication.value.id,
      visitType: visitForm.value.visitType,
      visitTime: visitForm.value.visitTime,
      petStatus: visitForm.value.petStatus,
      content: visitForm.value.content,
      feedback: visitForm.value.feedback,
      needFollowUp: visitForm.value.needFollowUp,
      nextVisitTime: visitForm.value.nextVisitTime
    })
    success('回访记录添加成功')
    visitDialogVisible.value = false
    loadApproved()
  } catch (err) {
    error(err.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

const viewVisitHistory = async (application) => {
  currentApplication.value = application
  try {
    visitHistory.value = await get(`/api/visit/list/${application.id}`)
    historyDialogVisible.value = true
  } catch (err) {
    error(err.message || '加载回访历史失败')
  }
}

const deleteVisitRecord = (visitId) => {
  ElMessageBox.confirm('确定要删除这条回访记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await del(`/api/visit/${visitId}`, { username: currentUser.value.username })
      success('删除成功')
      if (currentApplication.value) {
        viewVisitHistory(currentApplication.value)
      }
      // 刷新所有相关列表
      loadApproved()
      loadPendingVisits()
      loadAllVisits()
    } catch (err) {
      error(err.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  currentUser.value = getCurrentUser()
  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    error('无权限访问，请使用管理员账号登录')
    return
  }
  loadPending()
  loadApproved()
  loadRejected()
  loadPendingVisits()
  loadAllVisits()
})

watch(activeTab, (newTab) => {
  if (newTab === 'pending') loadPending()
  else if (newTab === 'approved') loadApproved()
  else if (newTab === 'rejected') loadRejected()
  else if (newTab === 'pending-visits') loadPendingVisits()
  else if (newTab === 'all-visits') loadAllVisits()
})
</script>


<style scoped>
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.custom-pagination :deep(.el-pagination__total) {
  font-size: 14px;
  color: #6B7280;
  margin-right: 20px;
}

.custom-pagination :deep(.el-pagination__sizes) {
  margin: 0 15px;
}

.custom-pagination :deep(.el-pagination__jump) {
  margin-left: 20px;
}

.custom-pagination :deep(.el-pager li) {
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  border-radius: 8px;
  margin: 0 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.custom-pagination :deep(.el-pager li.active) {
  background: linear-gradient(135deg, #E07A5F 0%, #F2CC8F 100%);
  color: white;
  border: none;
  font-weight: 600;
}

.custom-pagination :deep(.el-pager li:hover:not(.active)) {
  background-color: #FDF8F3;
  color: #E07A5F;
}

.custom-pagination :deep(.btn-prev),
.custom-pagination :deep(.btn-next) {
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  border-radius: 8px;
  padding: 0;
  border: 1px solid #E5E7EB;
  background: white;
  transition: all 0.3s;
}

.custom-pagination :deep(.btn-prev:hover),
.custom-pagination :deep(.btn-next:hover) {
  color: #E07A5F;
  border-color: #E07A5F;
}

.custom-pagination :deep(.el-select) {
  --el-select-border-radius-hover: 8px;
}

.custom-pagination :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.visit-form {
  margin-top: 20px;
}

.visit-record-card {
  border-radius: 12px;
  border: 1px solid #E5E7EB;
}

.visit-record-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.application-detail {
  padding: 10px;
}

.user-feedback-card {
  border-left: 4px solid #409EFF;
  background: linear-gradient(135deg, #F0F7FF 0%, #FFFFFF 100%);
}

.user-feedback-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px;
  background: #E6F7FF;
  border-radius: 6px;
  color: #1890FF;
  font-size: 13px;
}
</style>
