import { createRouter, createWebHistory } from 'vue-router'
import PetList from '../components/PetList.vue'
import PublishPet from '../components/PublishPet.vue'
import PetDetail from '../components/PetDetail.vue'
import HelpPost from '../components/HelpPost.vue'
import PostDetail from '../components/PostDetail.vue'
import MyCenter from '../components/MyCenter.vue'
import UserProfile from '../components/UserProfile.vue'
import AdminApplyManagement from '../components/AdminApplyManagement.vue'
import AdminPetReview from '../components/AdminPetReview.vue'
import AdminPostReview from '../components/AdminPostReview.vue'
import AdminUserManagement from '../components/AdminUserManagement.vue'


const routes = [
    {
        path: '/',
        redirect: '/pets'
    },
    {
        path: '/pets',
        component: PetList,
        meta: { title: '宠物列表', keepAlive: true }
    },
    {
        path: '/pet/:id',
        component: PetDetail,
        meta: { title: '宠物详情' }
    },
    {
        path: '/publish',
        component: PublishPet,
        meta: { title: '发布宠物' }
    },
    {
        path: '/help',
        component: HelpPost,
        meta: { title: '社区互助' }
    },
    {
        path: '/post/:id',
        component: PostDetail,
        meta: { title: '帖子详情' }
    },
    {
        path: '/user/:username',
        component: UserProfile,
        meta: { title: '用户主页' }
    },
    {
        path: '/center',
        component: MyCenter,
        meta: { title: '个人中心' }
    },
    {
        path: '/admin/applications',
        component: AdminApplyManagement,
        meta: { title: '申请审核', requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/admin/pets',
        component: AdminPetReview,
        meta: { title: '宠物审核', requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/admin/posts',
        component: AdminPostReview,
        meta: { title: '帖子审核', requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/admin/users',
        component: AdminUserManagement,
        meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
    },
]


const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(to, from, savedPosition) {
        // 如果是从详情页返回列表页，禁止路由层级重置滚动条
        if ((from.path.startsWith('/pet/') || from.path.startsWith('/post/')) && (to.path === '/pets' || to.path === '/help')) {
            return false
        }

        if (savedPosition) {
            return savedPosition
        }
        return { top: 0 }
    }
})


// 路由守卫：检查权限
router.beforeEach((to, from, next) => {
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    if (to.meta.requiresAuth && !user) {
        import('element-plus').then(({ ElMessage }) => {
            ElMessage.warning('请先登录')
        })
        next('/pets')
        return
    }

    if (to.meta.requiresAdmin && user?.role !== 'ADMIN') {
        import('element-plus').then(({ ElMessage }) => {
            ElMessage.warning('无权限访问')
        })
        next('/pets')
        return
    }

    next()
})

export default router
