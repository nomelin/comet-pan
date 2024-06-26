import Vue from 'vue'
import VueRouter from 'vue-router'
import {getItemWithExpiry} from "@/App"

Vue.use(VueRouter)

// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err)
}

const routes = [
    {
        path: '/manager',
        name: 'Manager',
        component: () => import('../views/Manager.vue'),
        redirect: '/home',  // 重定向到主页
        children: [
            {path: '403', name: 'NoAuth', meta: {name: '无权限'}, component: () => import('../views/manager/403')},
            {path: 'home', name: 'Home', meta: {name: '系统首页'}, component: () => import('../views/manager/Home')},
            {
                path: 'admin',
                name: 'Admin',
                meta: {name: '管理员信息'},
                component: () => import('../views/manager/Admin')
            },
            {
                path: 'adminPerson',
                name: 'AdminPerson',
                meta: {name: '个人信息'},
                component: () => import('../views/manager/AdminPerson')
            },
            {
                path: 'password',
                name: 'Password',
                meta: {name: '修改密码'},
                component: () => import('../views/manager/Password')
            },
            {
                path: 'notice',
                name: 'Notice',
                meta: {name: '公告信息'},
                component: () => import('../views/manager/Notice')
            },
        ]
    },
    {
        path: '/',
        name: 'Front',
        component: () => import('../views/Front.vue'),
        // redirect: '/files',  // 重定向到主页
        children: [
            // { path: 'home', name: 'Home', meta: { name: '系统首页' }, component: () => import('../views/front/Home') },
            {
                path: 'person',
                name: 'Person',
                meta: {name: '个人信息'},
                component: () => import('../views/front/Person')
            },
            {path: 'files', name: 'files', meta: {name: '网盘文件'}, component: () => import('../views/front/files')},
            {path: 'trash', name: 'trash', meta: {name: '回收站'}, component: () => import('../views/front/trash')},
            // { path: 'upload', name: 'upload', meta: { name: '上传文件' }, component: () => import('../views/front/uploader') },
            {path: 'share', name: 'share', meta: {name: '我的分享'}, component: () => import('../views/front/share')},
            {path: 'group', name: 'group', meta: {name: '我的群组'}, component: () => import('../views/front/group')},

        ]
    },
    {path: '/login', name: 'Login', meta: {name: '登录'}, component: () => import('../views/Login.vue')},
    {path: '/register', name: 'Register', meta: {name: '注册'}, component: () => import('../views/Register.vue')},
    {
        path: '/share/**',
        name: 'public share',
        meta: {name: '分享文件'},
        component: () => import('../views/publicShare')
    },
    {path: '*', name: 'NotFound', meta: {name: '无法访问'}, component: () => import('../views/404.vue')},
]

const router = new VueRouter({
    mode: 'history',// 去掉地址栏的#号
    base: process.env.BASE_URL,// 部署到服务器后的路径
    routes
})


// 这段代码是一个路由守卫，在 Vue Router 中使用路由守卫可以实现在导航触发时进行一些特定操作，比如验证用户身份、权限验证等。
// 首先，通过 router.beforeEach 注册了一个全局前置守卫，该守卫会在路由切换之前执行。
// 在守卫函数中，通过 localStorage.getItem("user") 获取了本地存储中的用户信息，并将其解析为一个对象。
// 然后，判断要跳转的路由路径 to.path 是否为根路径 /，即判断用户是否访问了网站的根路径。
// 如果用户访问的是根路径 /，则进一步判断用户的角色（假设角色信息存储在用户信息中的 role 字段中）：
// 如果用户已登录且角色为 'USER'，则将路由重定向到前台页面的首页
// 如果用户已登录但角色不是 'USER'，则将路由重定向到后台管理页面的首页
// 如果用户未登录，则将路由重定向到登录页面 /login。
// 如果用户访问的不是根路径 /，则直接调用 next() 方法，继续执行下一个导航钩子。
router.beforeEach((to, from, next) => {
    let user = getItemWithExpiry("user");
    console.log("get user", user)
    if (to.path === '/' || to.path === '') {
        if (user && !(Object.keys(user).length === 0) && user.role) {
            if (user.role === 2) {
                next('/files')
            } else if (user.role === 1) {
                next('/manager/home')
            } else {
                this.$message.error('用户角色权限非法')
                next('/login')
            }
        } else {
            next('/login')
        }
    } else if (to.path === "/files") {
        // console.log(Object.keys(user).length===0)
        if (!user || Object.keys(user).length === 0) {
            next('/login')
        } else {
            next()
        }
    } else {
        next()
    }
})

export default router
