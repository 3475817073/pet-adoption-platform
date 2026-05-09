<template>
  <div>

    <!-- 用户信息卡片：展示用户名、角色标签及统计数据 -->
    <el-card style="max-width: 1200px; margin: 20px auto; border-radius: 16px">
      <div class="user-profile">
        <div class="avatar-section">
          <div class="avatar-circle">{{ user?.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
          <h2>{{ user?.username || '未登录' }}</h2>
          <el-tag :type="getRoleColor(user?.role)" size="large">{{ getRoleText(user?.role) }}</el-tag>
        </div>
        <div class="stats-section">
          <div class="stat-item">
            <div class="stat-number">{{ publishedPets.length }}</div>
            <div class="stat-label">我的发布</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ myApplications.length }}</div>
            <div class="stat-label">领养申请</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ favoritesCount }}</div>
            <div class="stat-label">我的收藏</div>
          </div>
          <div class="stat-item" @click="showFollowersDialog = true" style="cursor: pointer;">
            <div class="stat-number">{{ followersCount }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item" @click="showFollowingDialog = true" style="cursor: pointer;">
            <div class="stat-number">{{ followingCount }}</div>
            <div class="stat-label">关注</div>
          </div>

        </div>
      </div>
    </el-card>

    <!-- Tab 切换：我的动态、我发布的宠物、我的申请、我的收藏 -->
    <el-tabs v-model="activeTab" type="border-card" style="max-width: 1200px; margin: 20px auto" @tab-change="handleTabChange">


    <!-- 我的动态：展示用户发布的所有内容时间线 -->
      <el-tab-pane label="我的动态" name="activity">
        <!-- 加载状态 -->
        <div v-if="activityLoading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 动态时间线 -->
        <el-timeline v-else-if="activities.length > 0" class="activity-timeline">
          <el-timeline-item
              v-for="activity in activities"
              :key="activity.id"
              :timestamp="formatDateTime(activity.createTime)"
              placement="top"
              :type="getActivityType(activity.type)"
          >
            <el-card class="activity-card" shadow="hover" :class="{'rejected-card': isRejected(activity)}">
              <!-- 宠物发布动态 -->
              <div v-if="activity.type === 'PET'" class="activity-content">
                <div class="activity-header">
                  <span class="activity-text">发布了新宠物</span>
                  <!-- 审核状态标签 -->
                  <el-tag
                      v-if="activity.detail?.reviewStatus"
                      :type="getReviewStatusType(activity.detail.reviewStatus)"
                      size="small"                      style="margin-left: 10px">
                    {{ getReviewStatusText(activity.detail.reviewStatus) }}
                  </el-tag>
                </div>
                <div class="pet-preview" @click="goToPetDetail(activity.targetId)">
                  <img v-if="getPetImage(activity.detail)" :src="getPetImage(activity.detail)" class="pet-thumb" />
                  <div v-else class="pet-thumb placeholder"></div>
                  <div class="pet-info">
                    <h4>{{ activity.detail.name }}</h4>
                    <p>{{ activity.detail.type }} · {{ activity.detail.age }}岁</p>
                    <el-tag size="small" :type="activity.detail.status === 'ADOPTED' ? 'danger' : 'success'">
                      {{ activity.detail.status === 'ADOPTED' ? '已被领养' : '待领养' }}
                    </el-tag>
                  </div>
                </div>
                <div class="activity-actions">
                  <el-button size="small" type="primary" @click="goToPetDetail(activity.targetId)">查看详情</el-button>
                  <el-button size="small" @click="editPet(activity.detail)">编辑</el-button>
                </div>

                <!-- 显示拒绝理由 -->
                <div v-if="activity.detail?.reviewStatus === 'REJECTED' && activity.detail?.rejectReason" class="reject-reason-box">
                  <div class="reject-header">
                    <span class="reject-icon">️</span>
                    <strong>管理员回复</strong>
                  </div>
                  <div class="reject-content">{{ activity.detail.rejectReason }}</div>
                </div>
              </div>

              <!-- 帖子发布动态 -->
              <div v-else-if="activity.type === 'POST'" class="activity-content">
                <div class="activity-header">
                  <span class="activity-text">发布了互助帖</span>
                  <!-- 审核状态标签 -->
                  <el-tag
                      v-if="activity.detail?.status"
                      :type="getReviewStatusType(activity.detail.status)"
                      size="small"                      style="margin-left: 10px">
                    {{ getReviewStatusText(activity.detail.status) }}
                  </el-tag>
                </div>
                <div class="post-preview" @click="goToPostDetail(activity.targetId)">
                  <h4>{{ activity.detail.title }}</h4>
                  <p class="post-excerpt">{{ truncateContent(activity.detail.content, 150) }}</p>
                  <div class="post-meta">
                    <el-tag size="small">{{ activity.detail.category }}</el-tag>
                  </div>
                </div>
                <div class="activity-actions">
                  <el-button size="small" type="primary" @click="goToPostDetail(activity.targetId)">查看详情</el-button>
                  <el-button size="small" @click="editPost(activity.detail)">编辑</el-button>
                </div>

                <!-- 显示拒绝理由 -->
                <div v-if="activity.detail?.status === 'REJECTED' && activity.detail?.rejectReason" class="reject-reason-box">
                  <div class="reject-header">
                    <span class="reject-icon">️</span>
                    <strong>管理员回复</strong>
                  </div>
                  <div class="reject-content">{{ activity.detail.rejectReason }}</div>
                </div>
              </div>


            </el-card>
          </el-timeline-item>
        </el-timeline>

        <!-- 空状态 -->
        <el-empty v-else description="暂无动态记录" :image-size="150" />
      </el-tab-pane>

      <!-- 我发布的宠物：展示用户已发布的宠物卡片列表 -->
      <el-tab-pane label="我发布的宠物" name="pets">
        <el-row :gutter="20" v-if="publishedPets.length > 0">
          <el-col v-for="pet in publishedPets" :key="pet.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 20px">
            <el-card shadow="hover" class="pet-card" :class="{'rejected-card': pet.reviewStatus === 'REJECTED'}">
              <!-- 审核状态标签 -->
              <div class="review-status">
                <el-tag
                    v-if="pet.reviewStatus"
                    :type="getReviewStatusType(pet.reviewStatus)"
                    size="small">
                  {{ getReviewStatusText(pet.reviewStatus) }}
                </el-tag>
              </div>

              <img v-if="getPetImageUrl(pet)" :src="getPetImageUrl(pet)" class="pet-image" />
              <div v-else class="no-image"></div>
              <div class="pet-info">
                <h4>{{ pet.name }}</h4>
                <p class="description">{{ pet.description }}</p>
              </div>
              <div class="status-actions">
                <el-tag :type="pet.status === 'ADOPTED' ? 'danger' : 'success'" size="small">
                  {{ pet.status === 'ADOPTED' ? '已被领养' : '待领养' }}
                </el-tag>
                <div class="action-buttons" v-if="pet.status === 'AVAILABLE' && pet.reviewStatus === 'APPROVED'">
                  <el-button type="primary" size="small" @click.stop="editPet(pet)">编辑</el-button>
                  <el-button type="danger" size="small" @click.stop="deletePet(pet.id)">删除</el-button>
                </div>
              </div>

              <!-- 显示拒绝理由 -->
              <div v-if="pet.reviewStatus === 'REJECTED' && pet.rejectReason" class="reject-reason-box">
                <div class="reject-header">
                  <span class="reject-icon">️</span>
                  <strong>管理员回复</strong>
                </div>
                <div class="reject-content">{{ pet.rejectReason }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="还没有发布过宠物哦~" :image-size="150" />

        <!-- 我的发布分页 -->
        <div class="pagination-wrapper" v-if="publishedPets.length > 0">
          <el-pagination
              v-model:current-page="petsCurrentPage"
              v-model:page-size="petsPageSize"
              :page-sizes="[4, 8, 16, 32]"
              :total="petsTotal"
              layout="total, prev, pager, next, sizes, jumper"
              :pager-count="7"
              @size-change="handlePetsSizeChange"
              @current-change="handlePetsPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

      <!-- 我的申请：展示用户提交的领养申请记录 -->
      <el-tab-pane label="我的申请" name="applications">
        <el-timeline v-if="myApplications.length > 0">
          <el-timeline-item
              v-for="app in myApplications"
              :key="app.id"
              :timestamp="formatDate(app.applyTime)"
              placement="top"
              :type="app.status === 'APPROVED' ? 'success' : app.status === 'REJECTED' ? 'danger' : 'warning'">
            <el-card shadow="hover" :class="{'rejected-card': app.status === 'REJECTED'}">
              <div class="application-item">
                <div class="app-pet-info">
                  <h4>{{ app.pet?.name || '未知宠物' }}</h4>
                  <el-tag :type="getStatusColor(app.status)" size="large">
                    {{ getStatusText(app.status) }}
                  </el-tag>
                </div>
                <p><strong>申请理由：</strong>{{ app.reason }}</p>
                <p><strong>联系方式：</strong>{{ app.contact }}</p>
                <p v-if="app.reviewTime"><strong>审核时间：</strong>{{ formatDate(app.reviewTime) }}</p>

                <!-- 显示拒绝理由 -->
                <div v-if="app.status === 'REJECTED' && app.rejectReason" class="reject-reason-box">
                  <div class="reject-header">
                    <span class="reject-icon">️</span>
                    <strong>管理员回复</strong>
                  </div>
                  <div class="reject-content">{{ app.rejectReason }}</div>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="还没有提交过领养申请哦~" :image-size="150" />

        <!-- 我的申请分页 -->
        <div class="pagination-wrapper" v-if="myApplications.length > 0">
          <el-pagination
              v-model:current-page="appsCurrentPage"
              v-model:page-size="appsPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="appsTotal"
              layout="total, prev, pager, next, sizes, jumper"
              :pager-count="7"
              @size-change="handleAppsSizeChange"
              @current-change="handleAppsPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

      <!-- 我的收藏：展示用户收藏的宠物和帖子 -->
      <el-tab-pane label="⭐ 我的收藏" name="favorites">
        <!-- 收藏筛选 -->
        <div class="favorites-filter">
          <el-radio-group v-model="favoriteFilter" size="small" @change="loadFavorites">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="PET">宠物</el-radio-button>
            <el-radio-button label="POST">帖子</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 加载状态 -->
        <div v-if="favoritesLoading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 收藏网格 -->
        <el-row v-else-if="filteredFavorites.length > 0" :gutter="20" class="favorites-grid">
          <el-col v-for="fav in filteredFavorites" :key="fav.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom: 20px">
            <el-card shadow="hover" class="favorite-card" @click="goToFavDetail(fav)">
              <div class="favorite-image">
                <img v-if="getFavImage(fav)" :src="getFavImage(fav)" />
                <div v-else class="favorite-placeholder">
                  {{ fav.targetType === 'PET' ? '🐾' : '💬' }}
                </div>
                <button class="unfavorite-btn" @click.stop="removeFavorite(fav)" title="取消收藏">
                  ✕
                </button>
              </div>
              <div class="favorite-content">
                <el-tag size="small" :type="fav.targetType === 'PET' ? 'success' : 'primary'" class="fav-type-tag">
                  {{ fav.targetType === 'PET' ? '宠物' : '帖子' }}
                </el-tag>
                <h4 class="favorite-title">{{ getFavTitle(fav) }}</h4>
                <p class="favorite-desc">{{ getFavDesc(fav) }}</p>
                <div class="favorite-meta">
                  <span class="collect-time">收藏于 {{ formatShortDate(fav.createTime) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 空状态 -->
        <el-empty v-else description="还没有收藏任何内容" :image-size="150">
          <el-button type="primary" @click="goToExplore">去逛逛</el-button>
        </el-empty>

        <!-- 分页 -->
        <div v-if="favorites.length > 0" class="pagination-wrapper">
          <el-pagination
              v-model:current-page="favCurrentPage"
              v-model:page-size="favPageSize"
              :page-sizes="[8, 16, 32]"
              :total="favTotal"
              layout="total, prev, pager, next, sizes, jumper"
              @size-change="handleFavSizeChange"
              @current-change="handleFavPageChange"
              background
              class="custom-pagination"
          />
        </div>
      </el-tab-pane>

    </el-tabs>

    <!-- 粉丝列表对话框 -->
    <el-dialog v-model="showFollowersDialog" title="我的粉丝" width="600px">
      <div v-if="followersLoading" class="loading-text">加载中...</div>
      <div v-else-if="followers.length === 0" class="empty-text">暂无粉丝</div>
      <div v-else class="user-list">
        <div
            v-for="item in followers"
            :key="item.id"
            class="user-list-item"
            @click="goToUserProfile(item.user.username)"
        >
          <div class="user-avatar">{{ item.user.username.charAt(0).toUpperCase() }}</div>
          <div class="user-name">{{ item.user.username }}</div>
          <el-tag :type="getRoleColor(item.user.role)" size="small">{{ getRoleText(item.user.role) }}</el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- 关注列表对话框 -->
    <el-dialog v-model="showFollowingDialog" title="我的关注" width="600px">
      <div v-if="followingLoading" class="loading-text">加载中...</div>
      <div v-else-if="following.length === 0" class="empty-text">暂无关注</div>
      <div v-else class="user-list">
        <div
            v-for="item in following"
            :key="item.id"
            class="user-list-item"
            @click="goToUserProfile(item.user.username)"
        >
          <div class="user-avatar">{{ item.user.username.charAt(0).toUpperCase() }}</div>
          <div class="user-name">{{ item.user.username }}</div>
          <el-tag :type="getRoleColor(item.user.role)" size="small">{{ getRoleText(item.user.role) }}</el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑帖子弹窗：提供帖子信息编辑表单 -->
    <el-dialog v-model="editPostDialogVisible" :title="`编辑帖子 - ${editingPost?.title || ''}`" width="900px" class="edit-post-dialog">
      <div v-if="editingPost" class="edit-post-container">
        <el-row :gutter="30">
          <!-- 左侧：照片上传 -->
          <el-col :span="10">
            <div class="post-image-section">
              <h4 class="section-title"> 帖子图片</h4>
              <div class="upload-area">
                <el-upload
                    action="http://localhost:8080/api/upload/image"
                    :on-success="handlePostUploadSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    list-type="picture-card"
                    :file-list="postImageList"
                    :limit="5"
                    accept="image/*"
                    class="edit-upload">
                  <div class="upload-placeholder">
                    <span class="upload-icon">📷</span>
                    <p>点击上传</p>
                  </div>
                </el-upload>
              </div>
              <p class="upload-tip">最多5张图片，支持jpg/png，单张≤5MB</p>
            </div>
          </el-col>

          <!-- 右侧：表单编辑 -->
          <el-col :span="14">
            <el-form :model="editPostForm" label-width="90px" class="edit-post-form">
              <el-form-item label="标题" required>
                <el-input v-model="editPostForm.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit>
                  <template #prefix>
                    <span class="input-icon">📝</span>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="分类" required>
                <el-select v-model="editPostForm.category" placeholder="请选择分类" style="width: 100%">
                  <el-option label="物资共享" value="物资共享" />
                  <el-option label="医疗咨询" value="医疗咨询" />
                  <el-option label="经验分享" value="经验分享" />
                  <el-option label="寻宠启事" value="寻宠启事" />
                  <el-option label="领养回访" value="领养回访" />
                  <el-option label="求助信息" value="求助信息" />
                  <el-option label="活动聚会" value="活动聚会" />
                  <el-option label="闲置转让" value="闲置转让" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>

              <el-form-item label="所在城市">
                <el-cascader
                    v-model="editPostForm.location"
                    :options="regionOptions"
                    :props="cascaderProps"
                    placeholder="请选择省/市/区"
                    clearable
                    filterable                    style="width: 100%"
                />
              </el-form-item>

              <el-form-item label="内容" required>
                <el-input
                    type="textarea"
                    v-model="editPostForm.content"
                    rows="6"
                    maxlength="2000"
                    show-word-limit
                    placeholder="请详细描述您的内容..." />
              </el-form-item>

              <div class="edit-post-actions">
                <el-button @click="editPostDialogVisible = false" size="large" class="cancel-btn">
                  <span class="btn-icon">✕</span> 取消
                </el-button>
                <el-button type="primary" @click="saveEditPost" :loading="savingPost" size="large" class="save-btn">
                  <span class="btn-icon">{{ savingPost ? '⏳' : '✓' }}</span>
                  {{ savingPost ? '保存中...' : '保存修改' }}
                </el-button>
              </div>
            </el-form>
          </el-col>
        </el-row>
      </div>
    </el-dialog>


    <!-- 编辑宠物弹窗：提供宠物信息编辑表单 -->
    <el-dialog v-model="editDialogVisible" :title="`编辑宠物 - ${editingPet?.name || ''}`" width="900px" class="edit-pet-dialog">
      <div v-if="editingPet" class="edit-container">
        <!-- 左侧：宠物照片预览与上传 -->
        <div class="edit-image-section">
          <img v-if="getPetImageUrl(editingPet)" :src="getPetImageUrl(editingPet)" class="edit-main-image" />
          <div v-else class="edit-no-image"></div>

          <!-- 照片上传区域 -->
          <div class="upload-area">
            <el-upload
                action="http://localhost:8080/api/upload/image"
                :on-success="handlePetUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                list-type="picture-card"
                :file-list="petImageList"
                :limit="3"
                accept="image/*"
                class="edit-upload">
              <div class="upload-placeholder">
                <span class="upload-icon">📷</span>
                <p>点击上传照片</p>
              </div>
            </el-upload>
          </div>

          <div class="image-tips">
            <p class="tip-text">💡 提示</p>
            <p class="tip-desc">修改宠物信息后将重新进入审核流程</p>
          </div>
        </div>

        <!-- 右侧：编辑表单 -->
        <div class="edit-form-section">
          <el-form :model="editForm" label-width="90px" class="edit-form">
            <el-form-item label="名字" required>
              <el-input v-model="editForm.name" placeholder="请输入宠物名字" maxlength="20" show-word-limit>
                <template #prefix>
                  <span class="input-icon"></span>
                </template>
              </el-input>
            </el-form-item>

            <el-row :gutter="15">
              <el-col :span="12">
                <el-form-item label="种类" required>
                  <el-select v-model="editForm.type" placeholder="请选择种类" style="width: 100%">
                    <el-option label="猫" value="猫" />
                    <el-option label="狗" value="狗" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别">
                  <el-select v-model="editForm.gender" placeholder="请选择性别" style="width: 100%">
                    <el-option label="♂ 公" value="公" />
                    <el-option label="♀ 母" value="母" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="15">
              <el-col :span="12">
                <el-form-item label="年龄">
                  <el-input-number v-model="editForm.age" :min="0" :max="30" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="健康状况">
                  <el-tag size="small" :type="editingPet.vaccinated ? 'success' : 'info'" style="margin-right: 5px">
                    {{ editingPet.vaccinated ? '✓ 已疫苗' : '○ 未疫苗' }}
                  </el-tag>
                  <el-tag size="small" :type="editingPet.neutered ? 'success' : 'info'" style="margin-right: 5px">
                    {{ editingPet.neutered ? '✓ 已绝育' : '○ 未绝育' }}
                  </el-tag>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="描述" required>
              <el-input
                  type="textarea"
                  v-model="editForm.description"
                  rows="4"
                  maxlength="500"
                  show-word-limit
                  placeholder="请详细描述宠物的性格、健康状况、生活习惯等信息..." />
            </el-form-item>

            <el-form-item label="标签">
              <el-input
                  v-if="editingPet.tags"
                  type="text"
                  :value="formatTagsForEdit(editingPet.tags)"
                  disabled
                  placeholder="暂无标签" />
              <p v-else class="no-tags-tip">暂无标签，可在发布时设置</p>
            </el-form-item>

            <div class="edit-actions">
              <el-button @click="editDialogVisible = false" size="large" class="cancel-btn">
                <span class="btn-icon">✕</span> 取消
              </el-button>
              <el-button type="primary" @click="saveEdit" :loading="saving" size="large" class="save-btn">
                <span class="btn-icon">{{ saving ? '⏳' : '✓' }}</span>
                {{ saving ? '保存中...' : '保存修改' }}
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 个人中心页面组件
 * 展示用户基本信息、已发布宠物列表、领养申请记录、我的动态、我的收藏，支持宠物编辑与删除操作
 */
import { ref, computed, onMounted, inject, watch } from 'vue'
import {useRoute, useRouter} from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { get, put, del, post } from '../utils/request.js'
import { success, warning, error } from '../utils/message.js'
import { regionData, codeToText } from 'element-china-area-data'

const router = useRouter()
const route = useRoute()
const triggerLogin = inject('triggerLogin')

// 省市区数据
const regionOptions = regionData
const cascaderProps = {
  checkStrictly: true,
  emitPath: true,
  expandTrigger: 'hover'
}

const user = ref(null)
const activeTab = ref('activity')
const publishedPets = ref([])
const myApplications = ref([])
const editDialogVisible = ref(false)
const editingPet = ref(null)
const editForm = ref({
  name: '',
  type: '',
  gender: '',
  age: 1,
  description: ''
})
const saving = ref(false)
const petImageList = ref([])

// 帖子编辑相关
const editPostDialogVisible = ref(false)
const editingPost = ref(null)
const editPostForm = ref({
  title: '',
  content: '',
  category: '',
  city: '',
  location: []
})
const savingPost = ref(false)
const postImageList = ref([])

// 动态相关
const activities = ref([])
const activityLoading = ref(false)

// 收藏相关
const favorites = ref([])
const favoritesLoading = ref(false)
const favoritesCount = ref(0)
const favoriteFilter = ref('all')
const favCurrentPage = ref(1)
const favPageSize = ref(8)
const favTotal = ref(0)

/** 宠物列表当前页码 */
const petsCurrentPage = ref(1)
/** 宠物列表每页条数 */
const petsPageSize = ref(8)
/** 宠物列表总条数 */
const petsTotal = ref(0)
/** 申请列表当前页码 */
const appsCurrentPage = ref(1)
/** 申请列表每页条数 */
const appsPageSize = ref(10)
/** 申请列表总条数 */
const appsTotal = ref(0)

// 粉丝和关注相关
const showFollowersDialog = ref(false)
const showFollowingDialog = ref(false)
const followers = ref([])
const following = ref([])
const followersLoading = ref(false)
const followingLoading = ref(false)
const followersCount = ref(0)
const followingCount = ref(0)

/**
 * 获取用户角色对应的标签颜色类型
 */
const getRoleColor = (role) => {
  const colors = { ADMIN: 'danger', USER: 'info' }
  return colors[role] || ''
}

/**
 * 将用户角色标识转换为中文显示
 */
const getRoleText = (role) => {
  const texts = { ADMIN: '管理员', USER: '普通用户' }
  return texts[role] || '未知'
}

/**
 * 获取申请状态对应的标签颜色类型
 */
const getStatusColor = (status) => {
  const colors = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return colors[status] || ''
}

/**
 * 将申请状态码转换为中文显示
 */
const getStatusText = (status) => {
  const texts = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
  return texts[status] || '未知'
}

/**
 * 格式化日期时间为本地系统格式
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 格式化短日期
 */
const formatShortDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

/**
 * 格式化日期时间（用于动态）
 */
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 截断内容
 */
const truncateContent = (content, maxLength) => {
  if (!content) return ''
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

/**
 * 获取活动类型对应的颜色
 */
/**
 * 获取活动类型对应的颜色
 */
const getActivityType = (type) => {
  const typeMap = {
    'PET': 'success',
    'POST': 'primary'
  }
  return typeMap[type] || ''
}

/**
 * 获取审核状态对应的标签颜色类型
 */
const getReviewStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || ''
}

/**
 * 获取审核状态对应的中文文本
 */
const getReviewStatusText = (status) => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || '未知'
}

/**
 * 判断动态是否为已拒绝状态
 */
const isRejected = (activity) => {
  if (activity.type === 'PET') {
    return activity.detail?.reviewStatus === 'REJECTED'
  } else if (activity.type === 'POST') {
    return activity.detail?.status === 'REJECTED'
  }
  return false
}

/**
 * 获取宠物首张图片 URL（优先使用 photoUrl，其次解析 photoUrls JSON 数组）
 */
const getPetImageUrl = (pet) => {
  if (pet.photoUrl) {
    return pet.photoUrl.startsWith('http') ? pet.photoUrl : 'http://localhost:8080' + pet.photoUrl
  }
  if (pet.photoUrls) {
    try {
      const photos = JSON.parse(pet.photoUrls)
      if (photos && photos.length > 0) {
        const url = photos[0]
        return url.startsWith('http') ? url : 'http://localhost:8080' + url
      }
    } catch {
      return null
    }
  }
  return null
}

/**
 * 获取宠物图片（用于动态）
 */
const getPetImage = (pet) => {
  return getPetImageUrl(pet)
}

/**
 * 获取收藏项的图片
 */
const getFavImage = (fav) => {
  if (!fav.detail) return null

  if (fav.targetType === 'PET') {
    return getPetImageUrl(fav.detail)
  } else if (fav.targetType === 'POST') {
    const post = fav.detail
    if (post.photoUrls) {
      try {
        const photos = JSON.parse(post.photoUrls)
        if (photos && photos.length > 0) {
          const url = photos[0]
          return url.startsWith('http') ? url : 'http://localhost:8080' + url
        }
      } catch {}
    }
  }
  return null
}

/**
 * 获取收藏项的标题
 */
const getFavTitle = (fav) => {
  if (!fav.detail) return '未知'
  if (fav.targetType === 'PET') {
    return fav.detail.name
  } else {
    return fav.detail.title
  }
}

/**
 * 获取收藏项的描述
 */
const getFavDesc = (fav) => {
  if (!fav.detail) return ''
  if (fav.targetType === 'PET') {
    const pet = fav.detail
    return `${pet.type} · ${pet.age}岁 · ${pet.gender}`
  } else {
    const post = fav.detail
    const content = post.content || ''
    return content.length > 80 ? content.substring(0, 80) + '...' : content
  }
}

/**
 * 根据筛选条件过滤收藏
 */
const filteredFavorites = computed(() => {
  if (favoriteFilter.value === 'all') {
    return favorites.value
  }
  return favorites.value.filter(fav => fav.targetType === favoriteFilter.value)
})

/**
 * 从后端加载用户数据：包括发布的宠物列表与提交的领养申请记录
 * 支持分页加载，根据当前页码与每页条数请求对应数据
 */
const loadUserData = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    warning('请先登录')
    triggerLogin()
    return
  }

  try {
    user.value = JSON.parse(userStr)
    const username = user.value.username

    // 加载我的发布（分页）
    const petsData = await get('/api/pet/my-pets', {
      username,
      page: petsCurrentPage.value - 1,
      size: petsPageSize.value
    })
    publishedPets.value = petsData.content
    petsTotal.value = petsData.totalElements

    // 加载我的申请（分页）
    const appsData = await get('/api/adoption/my-applications', {
      username,
      page: appsCurrentPage.value - 1,
      size: appsPageSize.value
    })
    myApplications.value = appsData.content
    appsTotal.value = appsData.totalElements

    // 加载粉丝和关注数量
    await loadFollowCounts()

  } catch (err) {
    error(err.message || '加载数据失败')
  }
}

/**
 * 加载粉丝列表
 */
const loadFollowers = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return

  const user = JSON.parse(userStr)
  followersLoading.value = true

  try {
    const data = await get('/api/follow/followers', {
      username: user.username,
      page: 0,
      size: 50
    })
    followers.value = data.content
  } catch (err) {
    console.error('加载粉丝失败:', err)
  } finally {
    followersLoading.value = false
  }
}

/**
 * 加载关注列表
 */
const loadFollowing = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return

  const user = JSON.parse(userStr)
  followingLoading.value = true

  try {
    const data = await get('/api/follow/following', {
      username: user.username,
      page: 0,
      size: 50
    })
    following.value = data.content
  } catch (err) {
    console.error('加载关注失败:', err)
  } finally {
    followingLoading.value = false
  }
}

/**
 * 加载粉丝和关注数量
 */
const loadFollowCounts = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return

  const user = JSON.parse(userStr)

  try {
    const followersRes = await get('/api/follow/followers/count', { username: user.username })
    followersCount.value = followersRes.count

    const followingRes = await get('/api/follow/following/count', { username: user.username })
    followingCount.value = followingRes.count
  } catch (err) {
    console.error('加载关注数量失败:', err)
  }
}

/**
 * 跳转到用户主页
 */
const goToUserProfile = (username) => {
  router.push(`/user/${username}`)
}

/**
 * 加载动态数据
 */
const loadActivities = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return

  const user = JSON.parse(userStr)
  activityLoading.value = true

  try {
    const [petsData, postsData] = await Promise.all([
      get('/api/pet/my-pets', {
        username: user.username,
        page: 0,
        size: 50
      }),
      get('/api/help/my-posts', {
        username: user.username,
        page: 0,
        size: 50
      })
    ])

    // 组装动态数据
    const petsActivities = petsData.content.map(pet => ({
      id: `pet_${pet.id}`,
      type: 'PET',
      targetId: pet.id,
      detail: pet,
      createTime: pet.createTime
    }))

    const postsActivities = postsData.content.map(post => ({
      id: `post_${post.id}`,
      type: 'POST',
      targetId: post.id,
      detail: post,
      createTime: post.createTime
    }))

    // 合并并按时间排序
    activities.value = [...petsActivities, ...postsActivities]
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } catch (err) {
    console.error('加载动态失败:', err)
  } finally {
    activityLoading.value = false
  }
}

/**
 * 加载收藏列表
 */
const loadFavorites = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return

  const user = JSON.parse(userStr)
  favoritesLoading.value = true

  try {
    const data = await get('/api/interaction/favorites', {
      username: user.username,
      page: favCurrentPage.value - 1,
      size: favPageSize.value
    })

    favorites.value = data.content
    favTotal.value = data.totalElements

    // 更新收藏总数
    if (favCurrentPage.value === 1) {
      favoritesCount.value = data.totalElements
    }
  } catch (err) {
    console.error('加载收藏失败:', err)
  } finally {
    favoritesLoading.value = false
  }
}

/**
 * 取消收藏
 */
const removeFavorite = async (fav) => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    warning('请先登录')
    return
  }

  const user = JSON.parse(userStr)

  try {
    await post('/api/interaction/favorite/toggle', {
      username: user.username,
      targetType: fav.targetType,
      targetId: fav.targetId
    })

    success('已取消收藏')
    await loadFavorites()
    await loadUserData() // 更新统计
  } catch (err) {
    error(err.message || '操作失败')
  }
}

/**
 * 跳转到宠物详情页面，携带当前分页参数
 */
const goToPetDetail = (petId) => {
  router.push(`/pet/${petId}`)
}

/**
 * 跳转到帖子详情页面
 */
const goToPostDetail = (postId) => {
  router.push(`/post/${postId}`)
}

/**
 * 打开编辑帖子弹窗，初始化表单数据
 */
const editPost = (post) => {
  editingPost.value = post
  editPostForm.value = {
    title: post.title,
    content: post.content,
    category: post.category,
    city: post.city || '',
    location: post.city ? parseCityToLocation(post.city) : []
  }

  // 初始化图片列表
  postImageList.value = []
  if (post.photoUrls) {
    try {
      const photos = JSON.parse(post.photoUrls)
      if (Array.isArray(photos)) {
        photos.forEach((url, index) => {
          postImageList.value.push({
            name: `图片${index + 1}`,
            url: url.startsWith('http') ? url : 'http://localhost:8080' + url
          })
        })
      }
    } catch (e) {
      console.error('解析照片URL失败:', e)
    }
  }

  editPostDialogVisible.value = true
}

/**
 * 解析城市字符串为级联选择器格式
 */
const parseCityToLocation = (cityStr) => {
  if (!cityStr) return []
  // 简单处理：如果是"北京市/市辖区/东城区"格式，转换为数组
  return cityStr.split('/').filter(Boolean)
}

/**
 * 保存帖子信息修改
 */
const saveEditPost = async () => {
  if (!editPostForm.value.title || !editPostForm.value.content || !editPostForm.value.category) {
    warning('请填写完整信息')
    return
  }

  savingPost.value = true
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    warning('请先登录')
    savingPost.value = false
    triggerLogin()
    return
  }

  const user = JSON.parse(userStr)

  try {
    // 将location数组转换为城市字符串（使用codeToText转换为文字）
    let city = ''
    if (editPostForm.value.location && editPostForm.value.location.length > 0) {
      city = editPostForm.value.location.map(code => codeToText[code] || code).join('/')
    }

    await put(`/api/help/post/${editingPost.value.id}`, {
      username: user.username,
      title: editPostForm.value.title,
      content: editPostForm.value.content,
      category: editPostForm.value.category,
      city: city,
      photoUrls: editPostForm.value.photoUrls || editingPost.value.photoUrls
    })

    success('修改成功')
    editPostDialogVisible.value = false
    await loadActivities()
  } catch (err) {
    error(err.message || '修改失败')
  } finally {
    savingPost.value = false
  }
}

/**
 * 跳转到收藏项详情
 */
const goToFavDetail = (fav) => {
  if (fav.targetType === 'PET') {
    router.push({
      path: `/pet/${fav.targetId}`,
      query: { fromCenter: 'favorites' }
    })
  } else if (fav.targetType === 'POST') {
    router.push({
      path: `/post/${fav.targetId}`,
      query: { fromCenter: 'favorites' }
    })
  }
}

/**
 * 去逛逛
 */
const goToExplore = () => {
  router.push('/pets')
}

/**
 * 宠物列表页码变化处理
 */
const handlePetsPageChange = (page) => {
  petsCurrentPage.value = page
  loadUserData()
}

/**
 * 宠物列表每页条数变化处理
 */
const handlePetsSizeChange = (size) => {
  petsPageSize.value = size
  petsCurrentPage.value = 1
  loadUserData()
}

/**
 * 申请列表页码变化处理
 */
const handleAppsPageChange = (page) => {
  appsCurrentPage.value = page
  loadUserData()
}

/**
 * 申请列表每页条数变化处理
 */
const handleAppsSizeChange = (size) => {
  appsPageSize.value = size
  appsCurrentPage.value = 1
  loadUserData()
}

/**
 * 收藏列表页码变化处理
 */
const handleFavPageChange = (page) => {
  favCurrentPage.value = page
  loadFavorites()
}

/**
 * 收藏列表每页条数变化处理
 */
const handleFavSizeChange = (size) => {
  favPageSize.value = size
  favCurrentPage.value = 1
  loadFavorites()
}

/**
 * 打开编辑宠物弹窗，初始化表单数据
 */
const editPet = (pet) => {
  editingPet.value = pet
  editForm.value = {
    name: pet.name,
    type: pet.type,
    gender: pet.gender,
    age: pet.age,
    description: pet.description
  }

  // 初始化图片列表
  petImageList.value = []
  if (pet.photoUrl) {
    petImageList.value.push({
      name: '宠物照片',
      url: pet.photoUrl.startsWith('http') ? pet.photoUrl : 'http://localhost:8080' + pet.photoUrl
    })
  }

  editDialogVisible.value = true
}

/**
 * 宠物照片上传成功
 */
const handlePetUploadSuccess = (response) => {
  if (response && response.url) {
    editForm.value.photoUrl = response.url
    success('照片上传成功')
  }
}

/**
 * 帖子照片上传成功
 */
const handlePostUploadSuccess = (response) => {
  if (response && response.url) {
    // 收集所有上传的照片URL
    const photoUrls = postImageList.value
        .filter(img => img.response && img.response.url)
        .map(img => img.response.url)

    if (response.url) {
      photoUrls.push(response.url)
    }

    editPostForm.value.photoUrls = JSON.stringify(photoUrls)
    success('照片上传成功')
  }
}

/**
 * 上传前校验
 */
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    error('只能上传图片文件!')
  }
  if (!isLt5M) {
    error('图片大小不能超过 5MB!')
  }
  return isImage && isLt5M
}

/**
 * 上传失败处理
 */
const handleUploadError = (err) => {
  error('照片上传失败，请重试')
  console.error('Upload error:', err)
}

/**
 * 格式化标签用于显示
 */
const formatTagsForEdit = (tagsStr) => {
  if (!tagsStr) return ''
  try {
    const tags = JSON.parse(tagsStr)
    return Array.isArray(tags) ? tags.join('、') : ''
  } catch {
    return ''
  }
}

/**
 * 保存宠物信息修改
 * 校验必填项后向 PUT 接口提交更新数据
 */
const saveEdit = async () => {
  if (!editForm.value.name || !editForm.value.type || !editForm.value.description) {
    warning('请填写完整信息')
    return
  }

  saving.value = true
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    warning('请先登录')
    saving.value = false
    triggerLogin()
    return
  }

  const user = JSON.parse(userStr)

  try {
    await put(`/api/pet/${editingPet.value.id}`, {
      username: user.username,
      name: editForm.value.name,
      type: editForm.value.type,
      gender: editForm.value.gender,
      age: editForm.value.age,
      description: editForm.value.description,
      photoUrl: editForm.value.photoUrl || editingPet.value.photoUrl,
      photoUrls: editingPet.value.photoUrls
    })

    success('修改成功')
    editDialogVisible.value = false
    await loadUserData()
    await loadActivities()
  } catch (err) {
    error(err.message || '修改失败')
  } finally {
    saving.value = false
  }
}

/**
 * 删除指定宠物信息
 * 弹出确认框，校验权限后向 DELETE 接口发送请求
 */
const deletePet = (petId) => {
  ElMessageBox.confirm('确定要删除这个宠物信息吗？此操作不可恢复', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      warning('请先登录')
      triggerLogin()
      return
    }

    const user = JSON.parse(userStr)

    try {
      await del(`/api/pet/${petId}`, { username: user.username })
      success('删除成功')
      await loadUserData()
      await loadActivities() // 刷新动态
    } catch (err) {
      error(err.message || '删除失败')
    }
  }).catch(() => {})
}

/**
 * 监听Tab切换，按需加载数据
 */
const handleTabChange = (tabName) => {
  if (tabName === 'activity' && activities.value.length === 0) {
    loadActivities()
  } else if (tabName === 'favorites' && favorites.value.length === 0) {
    loadFavorites()
  }
}

// 使用watch监听activeTab变化，确保Tab切换时加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'favorites' && favorites.value.length === 0) {
    loadFavorites()
  } else if (newTab === 'activity' && activities.value.length === 0) {
    loadActivities()
  }
})

// 监听粉丝对话框打开
watch(showFollowersDialog, (newVal) => {
  if (newVal) {
    loadFollowers()
  }
})

// 监听关注对话框打开
watch(showFollowingDialog, (newVal) => {
  if (newVal) {
    loadFollowing()
  }
})


/**
 * 组件挂载时执行：加载用户数据
 */
onMounted(() => {
  loadUserData()

  // 默认加载动态数据
  loadActivities()

  //加载收藏数据
  loadFavorites()
  // 检查是否从详情页返回，恢复Tab状态
  const tab = route.query.tab
  if (tab) {
    activeTab.value = tab
  }
})
</script>

<style scoped>
.user-profile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF7E5F 0%, #6EE7B7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
  font-weight: bold;
}

.avatar-section h2 {
  margin: 0;
  color: #333;
}

.stats-section {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #FF7E5F;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

/* 用户列表样式 */
.loading-text, .empty-text {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

.user-list {
  max-height: 400px;
  overflow-y: auto;
}

.user-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-list-item:hover {
  background: #f5f6f7;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF7E5F 0%, #6EE7B7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
  flex-shrink: 0;
}

.user-name {
  flex: 1;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}


.pet-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.pet-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(255, 126, 95, 0.2);
}

.pet-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.no-image {
  width: 100%;
  height: 180px;
  background: url('@/assets/pets-banner.png') center/cover no-repeat;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pet-info {
  padding: 15px;
}

.pet-info h4 {
  margin: 0 0 8px 0;
  color: #3D405B;
  font-size: 16px;
  font-weight: 600;
}


.status-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10px 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
}


.description {
  margin: 10px 0 0 0;
  color: #666;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.application-item h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.application-item p {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

.edit-container {
  display: flex;
  gap: 30px;
  padding: 10px;
}

.edit-image-section {
  flex: 0 0 320px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.edit-main-image {
  width: 100%;
  height: 320px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.edit-main-image:hover {
  transform: scale(1.02);
}

.edit-no-image {
  width: 100%;
  height: 320px;
  background: url('@/assets/pets-banner.png') center/cover no-repeat;
  background-color: #f3f4f6;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-tips {
  background: linear-gradient(135deg, #FFF9F0 0%, #FFF5E6 100%);
  border-radius: 12px;
  padding: 15px;
  border-left: 4px solid #E07A5F;
}

.tip-text {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
  color: #E07A5F;
}

.tip-desc {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.edit-form-section {
  flex: 1;
  overflow-y: auto;
  max-height: 600px;
}

.edit-form {
  margin-top: 10px;
}

.edit-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

.edit-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 10px 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.edit-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.edit-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(224, 122, 95, 0.2);
}

.input-icon {
  font-size: 16px;
  margin-right: 5px;
}

.edit-form :deep(.el-input-number) {
  width: 100%;
}

.edit-form :deep(.el-input-number .el-input__wrapper) {
  border-radius: 10px;
  padding: 10px 14px;
}

.edit-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 12px 14px;
  resize: vertical;
  font-family: inherit;
}

.no-tags-tip {
  margin: 0;
  font-size: 13px;
  color: #999;
  font-style: italic;
}

.edit-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 25px;
  border-top: 2px solid #F3F4F6;
}

.cancel-btn {
  border-radius: 10px;
  padding: 12px 28px;
  font-weight: 600;
  border: 2px solid #E5E7EB;
  background: white;
  color: #6B7280;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  border-color: #D1D5DB;
  background: #F9FAFB;
  color: #374151;
}

.save-btn {
  background: linear-gradient(135deg, #E07A5F 0%, #D06A4F 100%) !important;
  border: none !important;
  border-radius: 10px;
  padding: 12px 28px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(224, 122, 95, 0.3);
  transition: all 0.3s ease;
}

.save-btn:hover {
  background: linear-gradient(135deg, #D06A4F 0%, #C05A3F 100%) !important;
  box-shadow: 0 6px 16px rgba(224, 122, 95, 0.4);
  transform: translateY(-2px);
}

.btn-icon {
  margin-right: 5px;
  font-size: 14px;
}

/* 编辑弹窗整体样式优化 */
.edit-pet-dialog :deep(.el-dialog__header) {
  padding: 20px 30px;
  border-bottom: 2px solid #F3F4F6;
  background: linear-gradient(135deg, #FFF9F0 0%, #FFFFFF 100%);
}

.edit-pet-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 700;
  color: #374151;
}

.edit-pet-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.edit-pet-dialog :deep(.el-dialog__footer) {
  padding: 0;
}

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

/* 动态时间线样式 */
.loading-container {
  padding: 40px;
  background: white;
  border-radius: 12px;
}

.activity-timeline {
  padding: 20px 0;
}

.activity-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.activity-content {
  padding: 10px;
}

.activity-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.activity-icon {
  font-size: 24px;
}

.activity-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 宠物预览 */
.pet-preview {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.pet-preview:hover {
  background: #f3f4f6;
}

.pet-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.pet-thumb.placeholder {
  background: url('@/assets/pets-banner.png') center/cover no-repeat;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.pet-info h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 16px;
}

.pet-info p {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
}

/* 帖子预览 */
.post-preview {
  padding: 15px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.post-preview:hover {
  background: #f3f4f6;
}

.post-preview h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.post-excerpt {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.activity-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 收藏相关样式 */
.favorites-filter {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.favorites-grid {
  margin-top: 10px;
}

.favorite-card {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
}

.favorite-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f5f5f5;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.favorite-card:hover .favorite-image img {
  transform: scale(1.05);
}

.favorite-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  background: url('@/assets/pets-banner.png') center/cover no-repeat;
  background-color: #f3f4f6;
}

.unfavorite-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  font-size: 16px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-card:hover .unfavorite-btn {
  opacity: 1;
}

.unfavorite-btn:hover {
  background: rgba(255, 0, 0, 0.8);
  transform: scale(1.1);
}

.favorite-content {
  padding: 15px;
}

.fav-type-tag {
  margin-bottom: 10px;
}

.favorite-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-desc {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}

.favorite-meta {
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.collect-time {
  font-size: 12px;
  color: #999;
}

/* 帖子编辑弹窗样式 */
.edit-post-container {
  padding: 10px;
}

.edit-post-form {
  margin-top: 10px;
}

.edit-post-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

.edit-post-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 10px 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.edit-post-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.edit-post-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(224, 122, 95, 0.2);
}

.edit-post-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 12px 14px;
  resize: vertical;
  font-family: inherit;
}

.edit-post-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 25px;
  border-top: 2px solid #F3F4F6;
}

.edit-post-dialog :deep(.el-dialog__header) {
  padding: 20px 30px;
  border-bottom: 2px solid #F3F4F6;
  background: linear-gradient(135deg, #FFF9F0 0%, #FFFFFF 100%);
}

.edit-post-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 700;
  color: #374151;
}

.edit-post-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.edit-post-dialog :deep(.el-dialog__footer) {
  padding: 0;
}
/* ===== 照片上传区域样式 ===== */
.upload-area {
  margin-top: 15px;
}

.edit-upload :deep(.el-upload--picture-card) {
  width: 100%;
  height: 120px;
  border-radius: 12px;
  border: 2px dashed #D1D5DB;
  background: #F9FAFB;
  transition: all 0.3s ease;
}

.edit-upload :deep(.el-upload--picture-card:hover) {
  border-color: #E07A5F;
  background: #FFF9F0;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.upload-icon {
  font-size: 32px;
}

.upload-placeholder p {
  margin: 0;
  font-size: 13px;
  color: #6B7280;
}

.edit-upload :deep(.el-upload-list--picture-card) {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.edit-upload :deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  overflow: hidden;
}

/* 帖子编辑左侧图片区域 */
.post-image-section {
  background: #F9FAFB;
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.upload-tip {
  margin: 10px 0 0 0;
  font-size: 12px;
  color: #9CA3AF;
  text-align: center;
}

/* ===== 拒绝理由样式 ===== */
.reject-reason-box {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #FEF3F3 0%, #FEE2E2 100%);
  border-radius: 8px;
  border-left: 4px solid #E07A5F;
}

.reject-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #DC2626;
  font-size: 14px;
}

.reject-icon {
  font-size: 18px;
}

.reject-content {
  padding: 10px;
  background: white;
  border-radius: 6px;
  color: #374151;
  line-height: 1.8;
  font-size: 14px;
}

/* 拒绝状态卡片样式 */
.rejected-card {
  border: 2px solid #FEE2E2;
  box-shadow: 0 2px 12px 0 rgba(224, 122, 95, 0.1);
}

/* 我发布的宠物卡片显示审核状态 */
.pet-card .review-status {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
}

.pet-card {
  position: relative;
  overflow: hidden;
}

.pet-card .status-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
</style>